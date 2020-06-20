# Eclipse Wild Web Developer: Release notes

This page describes the noteworthy improvements provided by each release of Eclipse Wild Web Developer.

### Next release...

## 0.11.0

* 📅 Release Date (tentative): End of July 2020
* All changes: https://github.com/eclipse/wildwebdeveloper/compare/0.10.0...0.11.0

#### TypeScript program debug support

TypeScript programs can now be directly debugged using `Node program` Debug Configuration.

![TypeScript 3.8 demo](documentation-files/typescript-debug.gif)

### XML Catalogs preference page

You can now define XML catalogs via preference page.

### ⚠️ Breaking extensions ⚠️ Move to LemMinX 0.12.0

LemMinX XML language server was upgraded to 0.12.0, with a lot of improvements. However, please note that the extension API has changed in a non-backward compatible way; so to work with newer Wild Web Developer, ensure the extensions to `org.eclipse.wildwebdeveloper.xml.lemminx` are compatible with LemMinX 0.12.0.

## 0.10.0

* 📅 Release Date (tentative): Early June 2020
* All changes: https://github.com/eclipse/wildwebdeveloper/compare/0.9.1...0.10.0

#### Extension point to define initializationOptions to LemMinX XML Language Server

The extension point `org.eclipse.wildwebdeveloper.xml` now allows a new `initializationOptionsProvider` child element type, which can be used to specify some initialize options. This is typically useful for language server who should be configured according some some user preferences already configured somewhere else in the IDE, so such preferences can be propageted to the language server.

#### Extension point to define JSON Schema URLs

The extension point `org.eclipse.wildwebdeveloper.json.schema` allows record JSON schema for filename pattern. This records are registered in JSON Language Server during initialization:

```xml
<extension point="org.eclipse.wildwebdeveloper.json.schema">
      <schema pattern="composer.json" url="http://json.schemastore.org/composer" />
</extension>
```

## 0.9.1

* 📅 Release Date: May 4th 2020
* All changes: https://github.com/eclipse/wildwebdeveloper/compare/0.9.0...0.9.1

#### Support for Node.js 14

Wild Web Developer was successfully tested against node.js 14, so it's been added to the list of compatible versions, and no warning pop-up will show for users of Node.js 14.

#### Install XML support separately

A feature containing XML editor was extracted and can be installed separately, without installing all other Web tools (HTML, JS and so on).


## 0.9.0

* 📅 Release Date: April 15th, 2020
* All changes: https://github.com/eclipse/wildwebdeveloper/compare/0.8.3...0.9.0

#### Breaking changes

* Extension point `org.eclipse.wildwebdeveloper.lemminxExtension` now replaces `org.eclipse.wildwebdeveloper.xmllsExtension`
* Interface `LemminxClasspathExtensionProvider` now replaces `XMLLSClasspathExtensionProvider`
* XML Language server is now Eclipse Lemminx 0.11.0. Extensions must be built targetting this language server (package name have changed)

#### Select Chrome/Chromium instance in Debug configurations

When debugging against Chrome/Chromium, the related Debug Configurations now shows an extra tab that allow to select the browser instance to use. This is convenient when user has multiple instances of Chrome/Chromium installed and want to test against multiple ones. The default behavior didn't change and will look for a relevant instance in the PATH.

## 0.8.3

* 📅 Release Date: March 19th, 2020
* All changes: https://github.com/eclipse/wildwebdeveloper/compare/0.8.2...0.8.3

#### TypeScript 3.8 support

Wild Web Developer supports the latest release of TypeScript, 3.8

![TypeScript 3.8 demo](documentation-files/typescript38.png)


## 0.8.2

* 📅 Release Date: February 20th, 2020
* All changes: https://github.com/eclipse/wildwebdeveloper/compare/0.8.1...0.8.2

#### NPM Launch shortcut

Easily invoke NPM with the new NPM Launch shortcut under "Run As" context-menu for `package.json` file and editor, or by defining your Run Configuration.

▶️ https://www.screencast.com/t/iFg4QUTo28Rb

#### Easily define root mapping for remote node.js debugger

When debugging a remote process on a different filesystem, the related Debug Configuration now allows to easily define the root and local path to map together, so breakpoint and file references are correctly used by both client and server

▶️ https://www.screencast.com/t/oECdEsORLr6X

## 0.8.1

📅 Release Date: January 20th, 2020

#### Proxy honored for XML resolution

The proxy settings are now used by the XML edition assistance (eg to resolve XSD elements). The network settings of Eclipse IDE are used, and if the network settings are blank, the related System Properties of the running/host Eclipse process itself are forwarded to XML resolution. _This fixes issue #192._  

#### ESLint and Typescript-ESLint support

[ESLint](https://eslint.org/) is now supported in Wild Web Developer, providing both code diagnostics and quick fixes (when available) for JavaScript and Typescript files.

To enable ESLint for a project, a `.eslintrc`, `tsconfig.json` as well as the required `node_modules` must reside within the project's directory (or in a parent directory). The required `node_modules` can be installed with `npm install eslint@6.0.0 @typescript-eslint/eslint-plugin@2.7.0 @typescript-eslint/parser@2.7.0 typescript@3.6.4`.

An example `.eslintrc` can be found [here](org.eclipse.wildwebdeveloper.tests/testProjects/eslint/.eslintrc) and a example `tsconfig.json` can be found [here](org.eclipse.wildwebdeveloper.tests/testProjects/eslint/tsconfig.json)  

For more information on configuring ESLint, visit https://eslint.org/docs/user-guide/configuring.

▶️ Demo of ESLint in Wild Web Developer https://youtu.be/o-wI_niEz3E

#### Improved XML language server extensibility

Extension point to add jars to XML Language Server has been augmented to allow passing a dynamic resolution of multiple jars.

## 0.8.0

📅 Release Date: 19th November, 2019

#### XML language server extension support

XML-LS extension jars can now be integrated into Wild Web Developer through the xmllsExtension extension point, allowing additional functionality to be provided from the language server.

The extension jars must implement the **IXMLExtension interface** and must register with Java Service Provider Interface (SPI) mechanism in the **/META-INF/services/org.eclipse.lsp4xml.services.extensions.IXMLExtension** file.
To use the extension point, create an xmllsExtension extension in a plug-in project and set the path to the location of the extension jar (which must reside within the plug-in project).

Usage example:

```xml
    <extension
          point="org.eclipse.wildwebdeveloper.xml.xmllsExtension">
       <jar
             path="path/to/extension.jar">
       </jar>
    </extension>
```

## 0.7.0

📅 Release Date: October 11th, 2019

##### Angular template edition assistance (in HTML and TypeScript)

Wild Web Developer adopts new version of the ng-language-server, and includes support for rich edition
assistance (validation, completion, go to definition...) for template strings in TypeScript files and
template HTML files, among other Angular-specific features.

▶️ https://www.screencast.com/t/6GTi4jf6svR

## 0.6.0

📅 Release Date: September 27th, 2019

##### Debug with Chrome

##### Improved Debug As > Firefox/Chrome shortcuts to run on folders when they have some .html file 

Full list of changes: https://github.com/eclipse/wildwebdeveloper/compare/0.5.0...0.6.0

See also https://projects.eclipse.org/projects/tools.wildwebdeveloper/releases/0.6.0

## 0.5.0 (2019 Sep 18th)

📅 Release Date: September 18th, 2019

##### Added icons for supported file types
![icons](artwork/editorIcons.png)
##### Upgraded all language servers (many small improvements to edition features)
##### Work with Node.js 11 and 12
##### Format HTML
![HTML Format](documentation-files/html-format.gif)
##### Debugging doesn't log noisy messages in the Console any more
This messages were useful for debugging integration with debug adapter. They can still be enabled in the Launch Configuration "Debug Adapter" tab; they're simply turned off by default.
##### Change variable values during debug
![set veriable in debug](documentation-files/setVariable.gif)
##### Debug with Firefox Debug Adapter
[![Firefox debug adapter](https://img.youtube.com/vi/4Q_-CtvsEjY/0.jpg)](https://www.youtube.com/watch?v=4Q_-CtvsEjY)
##### Improve usability of the Debug launchers
TODO: demo video


Full list of changes: https://github.com/eclipse/wildwebdeveloper/compare/0.4.1...0.5.0

See also https://projects.eclipse.org/projects/tools.wildwebdeveloper/releases/0.5.0 

## Previous releases

No release notes were maintained before that.
