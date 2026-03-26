#!/usr/bin/env node
/*******************************************************************************
 * Copyright (c) 2026 Aleksandar Kurtakov and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

// LSP stdio proxy that converts the ESLint 3.x diagnostic pull model
// (textDocument/diagnostic + workspace/diagnostic/refresh) into the traditional
// push model (textDocument/publishDiagnostics) that LSP4E expects.
//
// The proxy:
// 1) Removes `diagnosticProvider` from the server's initialize response so
//    the client does not try to use pull diagnostics.
// 2) After textDocument/didOpen and textDocument/didChange, sends a
//    textDocument/diagnostic request to the server and converts the result
//    into a textDocument/publishDiagnostics notification to the client.
// 3) Intercepts workspace/diagnostic/refresh requests from the server,
//    responds with success, and re-pulls diagnostics for all tracked documents.

const { spawn } = require('child_process');

if (process.argv.length < 3) {
  process.stderr.write('Usage: eslint-lsp-proxy.js <serverMain.js> [args...]\n');
  process.exit(1);
}

const serverMain = process.argv[2];
const serverArgs = process.argv.slice(3);
const child = spawn(process.execPath, [serverMain, ...serverArgs], {
  stdio: ['pipe', 'pipe', 'inherit']
});

// Track open documents for re-pulling on refresh
const openDocuments = new Map(); // uri -> version

// Request ID counter for proxy-initiated requests to the server
let nextProxyRequestId = 900000;

// Map of proxy-initiated request IDs to document URIs
const pendingPullRequests = new Map();

// --- Client → Server ---
let inBuffer = Buffer.alloc(0);
process.stdin.on('data', chunk => {
  inBuffer = Buffer.concat([inBuffer, chunk]);
  drainInbound();
});

// --- Server → Client ---
let outBuffer = Buffer.alloc(0);
child.stdout.on('data', chunk => {
  outBuffer = Buffer.concat([outBuffer, chunk]);
  drainOutbound();
});

child.on('exit', (code) => {
  try { process.stdout.end(); } catch (_e) { /* ignore */ }
  process.exitCode = code ?? 0;
});

process.stdin.on('end', () => {
  try { child.stdin.end(); } catch (_e) { /* ignore */ }
});

// ---- inbound (client → server) processing ----

function drainInbound() {
  for (;;) {
    const msg = readMessage(inBuffer);
    if (!msg) return;
    inBuffer = msg.rest;
    handleClientMessage(msg.body);
  }
}

function handleClientMessage(bodyBuf) {
  let parsed;
  try {
    parsed = JSON.parse(bodyBuf.toString('utf8'));
  } catch (_e) {
    sendToServer(bodyBuf);
    return;
  }

  const method = parsed.method;

  if (method === 'textDocument/didOpen' && parsed.params?.textDocument) {
    const uri = parsed.params.textDocument.uri;
    openDocuments.set(uri, parsed.params.textDocument.version);
    sendToServer(bodyBuf);
    schedulePull(uri);
    return;
  }

  if (method === 'textDocument/didChange' && parsed.params?.textDocument) {
    const uri = parsed.params.textDocument.uri;
    openDocuments.set(uri, parsed.params.textDocument.version);
    sendToServer(bodyBuf);
    schedulePull(uri);
    return;
  }

  if (method === 'textDocument/didClose' && parsed.params?.textDocument) {
    openDocuments.delete(parsed.params.textDocument.uri);
  }

  sendToServer(bodyBuf);
}

// ---- outbound (server → client) processing ----

function drainOutbound() {
  for (;;) {
    const msg = readMessage(outBuffer);
    if (!msg) return;
    outBuffer = msg.rest;
    handleServerMessage(msg.body);
  }
}

function handleServerMessage(bodyBuf) {
  let parsed;
  try {
    parsed = JSON.parse(bodyBuf.toString('utf8'));
  } catch (_e) {
    sendToClient(bodyBuf);
    return;
  }

  // 1) Patch initialize response: remove diagnosticProvider
  if (parsed.id !== undefined && parsed.result?.capabilities?.diagnosticProvider) {
    delete parsed.result.capabilities.diagnosticProvider;
    sendToClient(Buffer.from(JSON.stringify(parsed), 'utf8'));
    return;
  }

  // 2) Intercept workspace/diagnostic/refresh from server
  if (parsed.method === 'workspace/diagnostic/refresh') {
    // Respond with success
    sendToClient(Buffer.from(JSON.stringify({
      jsonrpc: '2.0',
      id: parsed.id,
      result: null
    }), 'utf8'));
    // Re-pull diagnostics for every open document
    for (const uri of openDocuments.keys()) {
      pullDiagnostics(uri);
    }
    return;
  }

  // 3) Handle responses to our proxy-initiated textDocument/diagnostic requests
  if (parsed.id !== undefined && pendingPullRequests.has(parsed.id)) {
    const uri = pendingPullRequests.get(parsed.id);
    pendingPullRequests.delete(parsed.id);

    if (parsed.result?.kind === 'full' && Array.isArray(parsed.result.items)) {
      sendToClient(Buffer.from(JSON.stringify({
        jsonrpc: '2.0',
        method: 'textDocument/publishDiagnostics',
        params: {
          uri: uri,
          diagnostics: parsed.result.items
        }
      }), 'utf8'));
    }
    return;
  }

  // Everything else: forward unchanged
  sendToClient(bodyBuf);
}

// ---- diagnostic pull helpers ----

const pullTimers = new Map();

function schedulePull(uri) {
  if (pullTimers.has(uri)) {
    clearTimeout(pullTimers.get(uri));
  }
  pullTimers.set(uri, setTimeout(() => {
    pullTimers.delete(uri);
    pullDiagnostics(uri);
  }, 200));
}

function pullDiagnostics(uri) {
  const id = nextProxyRequestId++;
  pendingPullRequests.set(id, uri);
  sendToServer(Buffer.from(JSON.stringify({
    jsonrpc: '2.0',
    id: id,
    method: 'textDocument/diagnostic',
    params: { textDocument: { uri: uri } }
  }), 'utf8'));
}

// ---- LSP message framing ----

function readMessage(buf) {
  const headerEnd = findDoubleNewline(buf);
  if (headerEnd === -1) return null;

  const headers = buf.slice(0, headerEnd).toString('utf8');
  const contentLength = parseContentLength(headers);
  if (contentLength == null) return null;

  const total = headerEnd + 4 + contentLength;
  if (buf.length < total) return null;

  return {
    body: buf.slice(headerEnd + 4, total),
    rest: buf.slice(total)
  };
}

function sendToServer(bodyBuf) {
  child.stdin.write(Buffer.from(`Content-Length: ${bodyBuf.length}\r\n\r\n`, 'utf8'));
  child.stdin.write(bodyBuf);
}

function sendToClient(bodyBuf) {
  process.stdout.write(Buffer.from(`Content-Length: ${bodyBuf.length}\r\n\r\n`, 'utf8'));
  process.stdout.write(bodyBuf);
}

function findDoubleNewline(buf) {
  for (let i = 0; i + 3 < buf.length; i++) {
    if (buf[i] === 13 && buf[i + 1] === 10 && buf[i + 2] === 13 && buf[i + 3] === 10) return i;
  }
  return -1;
}

function parseContentLength(headers) {
  const match = /Content-Length:\s*(\d+)/i.exec(headers);
  return match ? parseInt(match[1], 10) : null;
}