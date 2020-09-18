# Node.js Embedder from Eclipse Wild Web Developer

When a developer's system suits the supported OSs (Linux, MacOS and Win32) and architectures (x86_64) the embedded Node.js from Wild Web Developer will be used by WildWebDeveloper to run language Servers as well as for Node Debugger unless the `"org.eclipse.wildwebdeveloper.nodeJSLocation"` system property is set.

## 📥 Integration of Node.js Embedder

Developers may use the Node.js Embedder by adding the `org.eclipse.wildwebdeveloper.embedder.node.feature` into the dependencies, and calling the `getNodeJsLocation()` method of `NodeJSManager` class in order to automatic install (if it's not yet installed) and obtain the embedded version of Node.js in their products.

```java
{
  import java.io.File;
  import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
  
  ...
  // Getting Node.js executable. 
  File nodeJsRuntime = NodeJSManager.getNodeJsLocation();   
}
```

After the first call to `getNodeJsLocation()` the subsequent calls to `which(String program)` method will start searching for the specified program firstly in the embedded Node.js installation directory. This allows to use the `node`, `npm` or `npx`utilities (if a utility name according to OS is provided as an argument) to be taken from the same embedded Node.js installation.

```java
{
  import java.io.File;
  import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
  
  ...
  // Another way to get the NodeJS executable on Linux (after embedded Node.js is installed.
  File nodeJsRuntime = NodeJSManager.which("node");
  
  // Obtaining `npm` executable on Linux
  File npmExecutable = NodeJSManager.which("npm");
  
  // Obtaining `npx` executable on Linux
  File npxExecutable = NodeJSManager.which("npx");
  
  // Obtaining some other executable on Linux 
  // System path will be used to find the specified application executable
  File firefoxExecutable = NodeJSManager.which("firefox");
}
```

## ⌨️ Get involved

Community support is currently available via [GitHub issues](https://github.com/eclipse/wildwebdeveloper/issues).

Contribution of Code and Documentation are welcome as [GitHub Pull Request](https://github.com/eclipse/wildwebdeveloper/pulls).

Continuous integration is available on https://jenkins.eclipse.org/wildwebdeveloper/

Quality analysis is available on [SonarCloud](https://sonarcloud.io/dashboard?id=eclipse-wildwebdeveloper).
