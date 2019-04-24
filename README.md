# Eclipse Wild Web Developer : 🌐 Web dev in Eclipse IDE 🌘

Simple and rich **edition** of typical main web and configuration files (HTML, CSS, JS, TS, JSON, YAML, XML -with schema support-) and **debugging** of Node.js apps in the Eclipse IDE.

To see it in action, open the desired file with the Generic Editor that's included by default in the Eclipse IDE.

Supported file formats for edition:

* HTML
* CSS, SCSS, SASS, LESS
* JavaScript (EcmaScript 2018 - 9th edition)
* TypeScript 3.3
* JSON (including schema support)
* YAML (including schema support, Kubernetes schema built-in)
* XML (including schema support), XSL, XSD, DTD

Supported frameworks (file specialization) for edition:

* Kubernetes
* Angular (Components in TypeScript files)
* React (JSX, TSX, embedded HTML)

Supported feature for edition are

* Validation (diagnostics, markers)
* Code Completion
* Hover
* Outline
* Rename refactoring
* Jump to declaration
* Find references
* Color preview
* ... and other features part of the Language Server Protocol

Supported debugging targets

* Node.js

![screenshot](wildwebdeveloper-screenshot.png "Wild Web Developer screenshot")

## 💡 Design

Wild Web Developer is based on the Eclipse Generic Editor framework from Eclipse Platform, LSP4E and TM4E in order to provide editors based on TextMate grammars, VSCode Language Server, xml-languageserver and yaml-language servers from Red Hat; and on Eclipse Debug stack, LSP4E Debug Adapter Protocol support and VSCode Node Debug Adapter to provide debugging.

## ⬇️ Installation

Install into Eclipse IDE (pick one way or another):
* With Eclipse Marketplace Client and https://marketplace.eclipse.org/content/wild-web-developer-web-development-eclipse-ide-experimental, choose one of:
  * *Help > Eclipse Marketplace*, search `Wild Web Developer` then click `Install` or
  * drag <a href="http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=3394048" class="drag" title="Drag to your running Eclipse* workspace. *Requires Eclipse Marketplace Client"><img class="img-responsive" src="https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png" alt="Drag to your running Eclipse* workspace. *Requires Eclipse Marketplace Client" /></a> into Eclipse IDE, or
* With [Help > Install New Software...](http://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-124.htm) with p2 repo http://download.eclipse.org/wildwebdeveloper/snapshots

In an Eclipse target platform definition:
* Use http://download.eclipse.org/wildwebdeveloper/snapshots p2 repo

## ⌨️ Get involved

Community support is currently available via [GitHub issues](https://github.com/eclipse/wildwebdeveloper/issues).

Contribution of Code and Documentation are welcome as [GitHub Pull Request](https://github.com/eclipse/wildwebdeveloper/pulls).

Continuous integration is available on https://jenkins.eclipse.org/wildwebdeveloper/

Quality analysis is available on [SonarCloud](https://sonarcloud.io/dashboard?id=eclipse-wildwebdeveloper).

## 🎬 Media Library

📰 [Apr 2019] [Eclipse Wild Web Developer adds a powerful YAML editor with built-in Kubernetes support](https://developers.redhat.com/blog/2019/04/10/eclipse-wild-web-developer-adds-a-powerful-yaml-editor-with-built-in-kubernetes-support/) on Red Hat Developers blog

▶️ [Mar 2019] Demo of YAML editor support: https://youtu.be/P9ETtuHiUco

▶️ [Feb 2019] Demo of XML editor support: https://youtu.be/fikUdUZFdzg

▶️ [Apr 2017] XML, JSON, CSS, JavaScript, TypeScript demo http://www.screencast.com/t/BaC9DxHIqD
