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

// Simple Markdown-it parser for LSP client `markdown/parse`.
// Accepts either a file path argument or reads from stdin and prints JSON array of tokens.

const fs = require('node:fs');

function loadMarkdownIt() {
  try {
    return require('markdown-it');
  } catch (e) {
    console.error('markdown-it is not installed. Please add it to dependencies.');
    process.exit(2);
  }
}

const MarkdownIt = loadMarkdownIt();
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: false,
});

function tokenToJSON(tok) {
  const out = {
    type: tok.type,
    tag: tok.tag,
    attrs: tok.attrs || null,
    map: tok.map || null,
    nesting: tok.nesting,
    level: tok.level,
    content: tok.content,
    markup: tok.markup,
    info: tok.info,
    meta: tok.meta || null,
    block: tok.block,
    hidden: tok.hidden,
  };
  if (tok.children && Array.isArray(tok.children)) {
    out.children = tok.children.map(tokenToJSON);
  }
  return out;
}

const argPath = process.argv[2];
if (argPath) {
  try {
    const input = fs.readFileSync(argPath, 'utf8');
    const env = {};
    const tokens = md.parse(input, env);
    const json = tokens.map(tokenToJSON);
    process.stdout.write(JSON.stringify(json));
  } catch (err) {
    console.error(String(err && err.stack ? err.stack : err));
    process.exit(1);
  }
} else {
  let input = '';
  process.stdin.setEncoding('utf8');
  process.stdin.on('data', (chunk) => (input += chunk));
  process.stdin.on('end', () => {
    try {
      const env = {};
      const tokens = md.parse(input, env);
      const json = tokens.map(tokenToJSON);
      process.stdout.write(JSON.stringify(json));
    } catch (err) {
      console.error(String(err && err.stack ? err.stack : err));
      process.exit(1);
    }
  });
}

