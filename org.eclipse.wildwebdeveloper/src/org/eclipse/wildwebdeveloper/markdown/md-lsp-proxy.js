#!/usr/bin/env node
/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/

// This is an LSP stdio proxy to intercept and manipulate the communication between the LSP and the client.
// Why this is needed:
// 1) Invalid server message (server→client):
//    Problem: The Markdown server sometimes sends `window/logMessage` with a non-string `params.message`,
//             violating the LSP (which requires a string). See also
//             https://github.com/microsoft/vscode-markdown-languageserver/issues/8
//    Fix:     Sanitize these messages by stringifying `params.message` so the client accepts them.
//
// 2) Windows URI normalization mismatch (client→server) breaking pull diagnostics:
//    Problem: LSP4E emits URIs like `file:///D:/...`, while the server keys open documents by
//             `vscode-uri`'s `URI.toString()` form, which on Windows is `file:///d%3A/...` (lowercased
//             drive letter and percent-encoded colon). Because the strings differ, the server does not
//             recognize the document as open (`workspace.hasMarkdownDocument(uri)` fails) and returns
//             empty diagnostics.
//    Fix:     For text document lifecycle notifications (didOpen/didChange/didClose/willSave/didSave),
//             forward the original message and also a duplicate where the URI is normalized to the
//             server's `URI.toString()` form. This guarantees the server tracks the open document under
//             the key it uses during diagnostics, enabling `computeDiagnostics()` and `markdown/fs/*` requests.
//
// 3) Windows path suggestions (client→server) producing incorrect absolute drive paths:
//    Problem: When resolving workspace header/path completions, the current document URI and target
//             document URIs sometimes differ in Windows drive case/encoding (e.g., `file:///D:/...` vs
//             `file:///d%3A/...`). This causes relative path computation to fall back to odd absolute
//             paths such as `../../../../d:/...`.
//    Fix:     Normalize the document URI in `textDocument/completion` requests so that the server sees a
//             consistent Windows-encoded form and computes clean relative paths (e.g., `GUIDE.md#...`).
//
//    Note: On non-Windows URIs, normalization is a no-op; messages are forwarded unchanged.

const { spawn } = require('child_process');

if (process.argv.length < 3) {
  console.error('Usage: md-lsp-proxy.js <serverMain.js> [args...]');
  process.exit(1);
}

const serverMain = process.argv[2];
const serverArgs = process.argv.slice(3);
// Launch the wrapped language server; we proxy its stdio
const child = spawn(process.execPath, [serverMain, ...serverArgs], { stdio: ['pipe', 'pipe', 'inherit'] });

// Client → Server (Problem 2 & 3): normalize Windows URIs, mirror lifecycle notifications,
// and normalize completion requests
let inBuffer = Buffer.alloc(0);
process.stdin.on('data', chunk => {
  inBuffer = Buffer.concat([inBuffer, chunk]);
  processInboundBuffer();
});

// Server → Client (Problem 1): sanitize window/logMessage and forward
let buffer = Buffer.alloc(0);
child.stdout.on('data', chunk => {
  buffer = Buffer.concat([buffer, chunk]);
  processBuffer();
});

child.on('exit', (code, _signal) => {
  // forward EOF
  try { process.stdout.end(); } catch { }
  process.exitCode = code ?? 0;
});

// Server → Client processing (Problem 1)
function processBuffer() {
  for (; ;) {
    const headerEnd = indexOfHeadersEnd(buffer);
    if (headerEnd === -1) return; // need more data

    const headerBytes = buffer.slice(0, headerEnd);
    const headers = headerBytes.toString('utf8');
    const contentLength = parseContentLength(headers);
    if (contentLength == null) {
      // Cannot parse content length, flush raw and reset
      process.stdout.write(buffer);
      buffer = Buffer.alloc(0);
      return;
    }
    const total = headerEnd + 4 + contentLength;
    if (buffer.length < total) return; // wait for full body

    const body = buffer.slice(headerEnd + 4, total);
    const sanitized = sanitizeBody(body);
    const outHeaders = buildHeaders(sanitized.length);
    process.stdout.write(outHeaders);
    process.stdout.write(sanitized);

    buffer = buffer.slice(total);
  }
}

// Client → Server processing (Problem 2)
function processInboundBuffer() {
  for (; ;) {
    const headerEnd = indexOfHeadersEnd(inBuffer);
    if (headerEnd === -1) return;

    const headerBytes = inBuffer.slice(0, headerEnd);
    const headers = headerBytes.toString('utf8');
    const contentLength = parseContentLength(headers);
    if (contentLength == null) {
      // Cannot parse content length, flush raw and reset
      child.stdin.write(inBuffer);
      inBuffer = Buffer.alloc(0);
      return;
    }
    const total = headerEnd + 4 + contentLength;
    if (inBuffer.length < total) return;

    const body = inBuffer.slice(headerEnd + 4, total);
    const outbound = transformInbound(body);
    for (const msgBuf of outbound) {
      const outHeaders = buildHeaders(msgBuf.length);
      child.stdin.write(outHeaders);
      child.stdin.write(msgBuf);
    }

    inBuffer = inBuffer.slice(total);
  }
}

// Client → Server normalization (Problem 2 & 3)
function transformInbound(bodyBuf) {
  try {
    const text = bodyBuf.toString('utf8');
    const msg = JSON.parse(text);
    const method = msg && msg.method;

    // Duplicate lifecycle notifications with a normalized URI (Problem 2)
    if ((method === 'textDocument/didOpen' || method === 'textDocument/didChange' || method === 'textDocument/didClose' || method === 'textDocument/willSave' || method === 'textDocument/didSave') && msg.params && msg.params.textDocument) {
      const origUri = msg.params.textDocument.uri;
      const normUri = normalizeFileUriForServer(origUri);
      if (normUri && normUri !== origUri) {
        const dup = structuredClone(msg);
        dup.params.textDocument.uri = normUri;
        // Send original first (exact client payload), then the normalized duplicate (server-friendly)
        return [Buffer.from(JSON.stringify(msg), 'utf8'), Buffer.from(JSON.stringify(dup), 'utf8')];
      }
    }

    // Normalize completion requests so relative path suggestions are correct on Windows (Problem 3)
    if (method === 'textDocument/completion' && msg.params && msg.params.textDocument) {
      const origUri = msg.params.textDocument.uri;
      const normUri = normalizeFileUriForServer(origUri);
      if (normUri && normUri !== origUri) {
        const req = structuredClone(msg);
        req.params.textDocument.uri = normUri;
        return [Buffer.from(JSON.stringify(req), 'utf8')];
      }
    }

  } catch { }
  return [bodyBuf];
}

// Normalize Windows file URIs to match vscode-uri’s URI.toString() (Problem 2)
function normalizeFileUriForServer(uri) {
  if (typeof uri !== 'string' || !uri.startsWith('file:')) return undefined;
  // Strip scheme and leading slashes to get to drive letter
  let after = uri.slice('file:'.length);
  while (after.startsWith('/')) after = after.slice(1);
  // Example accepted forms: D:/path or d:/path
  if (/^[A-Za-z]:/.test(after)) {
    const drive = after[0].toLowerCase();
    const rest = after.slice(2); // drop ':'
    // Ensure a leading slash for the path segment
    const pathPart = rest.startsWith('/') ? rest : '/' + rest;
    return 'file:///' + drive + '%3A' + pathPart;
  }
  return undefined;
}

function indexOfHeadersEnd(buf) {
  // search for \r\n\r\n
  for (let i = 0; i + 3 < buf.length; i++) {
    if (buf[i] === 13 && buf[i + 1] === 10 && buf[i + 2] === 13 && buf[i + 3] === 10) return i;
  }
  return -1;
}

function parseContentLength(headers) {
  const match = /Content-Length:\s*(\d+)/i.exec(headers);
  if (!match) return null;
  return Number.parseInt(match[1], 10);
}

function buildHeaders(length) {
  return Buffer.from(`Content-Length: ${length}\r\n\r\n`, 'utf8');
}

// Sanitize server log messages (Problem 1)
function sanitizeBody(bodyBuf) {
  try {
    const text = bodyBuf.toString('utf8');
    const msg = JSON.parse(text);
    if (msg && msg.method === 'window/logMessage' && msg.params && typeof msg.params.message !== 'string') {
      msg.params.message = JSON.stringify(msg.params.message);
      return Buffer.from(JSON.stringify(msg), 'utf8');
    }
  } catch {
  }
  return bodyBuf;
}
