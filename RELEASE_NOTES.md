# Eclipse Wild Web Developer: Release notes

This page describes the noteworthy improvements provided by each release of Eclipse Wild Web Developer.

### Next release...

## 0.8.1

📅 Release Date: Mid-January, 2020

#### Proxy honored for XML resolution

The proxy settings are now used by the XML edition assistance (eg to resolve XSD elements). The network settings of Eclipse IDE are used, and if the network settings are blank, the related System Properties of the running/host Eclipse process itself are forwarded to XML resolution. _This fixes issue #192._  

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
