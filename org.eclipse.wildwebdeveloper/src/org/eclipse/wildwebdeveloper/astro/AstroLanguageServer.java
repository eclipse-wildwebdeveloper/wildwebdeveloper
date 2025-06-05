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
 *   Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.astro;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

/**
 * Launches the embedded Node.js based Astro language server.
 *
 * See https://github.com/withastro/language-tools/tree/main/packages/language-server
 *
 * @author Sebastian Thomschke
 */
public final class AstroLanguageServer extends ProcessStreamConnectionProvider {

   private static volatile String astroLanguageServerPath;
   private static volatile String typescriptSdkPath;

   private static Path resolveResource(String resourcePath) throws IOException {
      try {
         URL url = FileLocator.toFileURL(AstroLanguageServer.class.getResource(resourcePath));
         return Paths.get(url.toURI()).toAbsolutePath();
      } catch (URISyntaxException ex) {
         throw new IOException("Failed to resolve resource URI: " + resourcePath, ex);
      }
   }

   public AstroLanguageServer() throws IOException {
      try {
         if (astroLanguageServerPath == null || typescriptSdkPath == null) {
            astroLanguageServerPath = resolveResource("/node_modules/astro-vscode/dist/node/server.js").toString();
            typescriptSdkPath = resolveResource("/node_modules/typescript/lib").toString();
         }
         setCommands(List.of( //
            NodeJSManager.getNodeJsLocation().getAbsolutePath(), //
            astroLanguageServerPath, //
            "--stdio" //
         ));
         setWorkingDirectory(System.getProperty("user.dir"));
      } catch (IOException ex) {
         ILog.get().error(ex.getMessage(), ex);
      }
   }

   @Override
   public Map<String, Object> getInitializationOptions(final URI projectRootUri) {
      final Map<String, Object> options = new HashMap<>();
      setWorkingDirectory(projectRootUri.getRawPath());

      // see https://github.com/withastro/language-tools/blob/main/packages/vscode/src/client.ts
      // see https://github.com/withastro/language-tools/blob/main/packages/language-server/src/nodeServer.ts
      options.put("typescript", Collections.singletonMap("tsdk", typescriptSdkPath));
      options.put("contentIntellisense", true);
      return options;
   }
}
