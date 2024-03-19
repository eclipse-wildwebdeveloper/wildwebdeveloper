# Contributing to Eclipse Wild Web Developer

Welcome to the Eclipse Wild Web Developer contributor land, and thanks in advance for your help in making Eclipse Wild Web Developer better and better!

## ⚖️ Legal and Eclipse Foundation terms

The project license is available at [LICENSE](LICENSE).

This Eclipse Foundation open project is governed by the Eclipse Foundation
Development Process and operates under the terms of the Eclipse IP Policy.

Before your contribution can be accepted by the project team, 
contributors must have an Eclipse Foundation account and 
must electronically sign the Eclipse Contributor Agreement (ECA).

* [http://www.eclipse.org/legal/ECA.php](http://www.eclipse.org/legal/ECA.php)

For more information, please see the Eclipse Committer Handbook:
[https://www.eclipse.org/projects/handbook/#resources-commit](https://www.eclipse.org/projects/handbook/#resources-commit).

## 💬 Get in touch with the community

Eclipse Wild Web Developer use mainly 2 channels for technical discussions:

* 🐞 View and report issues through uses GitHub Issues at https://github.com/eclipse/wildwebdeveloper/issues.
* 🗪 Technical discussions that are not yet qualified as issue are happening in GitHub Discussions at https://github.com/eclipse/wildwebdeveloper/discussions

Project committers must 📧 join the m2e-dev@eclipse.org mailing-list which must be used (according the the Eclipse Development Process) for formal project development decision such as committers and project lead elections.

## 🆕 Trying latest builds

Latest builds, for testing, can usually be found at `https://download.eclipse.org/wildwebdeveloper/snapshots/` .

## 🧑‍💻 Developer resources

### Prerequisites

Java 21 and Maven 3.9.6 (only if you want to build from the command-line), or newer.

### ⌨️ Setting up the Development Environment manually

* Clone this repository <a href="https://mickaelistria.github.io/redirctToEclipseIDECloneCommand/redirect.html"><img src="https://mickaelistria.github.io/redirctToEclipseIDECloneCommand/cloneToEclipseBadge.png" alt="Clone to Eclipse IDE"/></a>.
* Run a build via command-line as mentioned below, as some resource provisioning steps will not run in the IDE.
* _File > Open Projects from Filesystem..._ , select the path to wildwebdeveloper Git repo and the relevant children projects you want to import
* Depending on the task you're planning to work on, multiple workflows are available to configure the [target-platform](https://help.eclipse.org/2022-09/topic/org.eclipse.pde.doc.user/concepts/target.htm?p=4_1_5)
    * In many cases, this simplest workflow will be sufficient: Install latest Wild Web Developer snapshot in your target-platform (can be your current IDE) as described above, or
    * open  __target-platform/target-platform.target__ in your IDE using the Target Definition editor and  _Set as Target-Platform_  from the editor
* Open the project modules you want to work on (right-click > Open project) and their dependencies
* Happy coding!


### 🏗️ Build

Simply `mvn clean verify`, this will run the tests (`-DskipTests` to skip them) and the resulting p2 repository and specific IDE applications will be available for further manual testing in `repository/target`.

To full build and test use the following commands:
```
$ cd <WildWebDeveloper project root directory>
$ mvn clean install   
```

You can use `-DskipTests` argument to skip the JUnit tests execution:

```
$ mvn clean install -DskipTests
```

To run all the JUnit tests use the following commands:

```
$ mvn clean verify
```

Or you can run an individual JUnit test by using `-Dtest=...` argument, for example:

```
$ mvn clean verify -Dtest=TestHTML   
```

In case you need to work on tests only or repeatedly execute several tests, it's easier and faster to build everything then run only the tests:

```
$ cd <WildWebDeveloper project root directory>

# Build WWD: 
$ mvn clean install -DskipTests
  
# In order to run `org.eclipse.wildwebdeveloper.tests.TestHTML` use the following command:
$ cd org.eclipse.wildwebdeveloper.tests/
$ mvn clean verify -Dtest=TestHTML   

#`Repeat running TestHTML or run any other tests, for example:
$ mvn clean verify -Dtest=TestJSON   
```

### ⬆️ Version bump

Wild Web Developer tries to use OSGi Semantic Version (to properly expose its API contracts and breakage) and Reproducible Version Qualifiers (to minimize the avoid producing multiple equivalent artifacts for identical source). This requires the developer to manually bump version from time to time. Somes rules are that:

* Versions are bumped on a __per module grain__ (bump version of individual bundles/features one by one when necessary), __DON'T bump version of parent pom, nor of other modules you don't change__
* __Versions are bumped maximum once per release__ (don't bump versions that were already bumped since last release)
* __Don't bump versions of what you don't change__
* __Bump version of the bundles you're modifying only if it's their 1st change since last release__
* Version bump may need to be cascaded to features that *include* the artifact you just changed, and then to features that *include* such features and so on (unless the version of those features were already bumped since last release).

The delta for version bumps are:

* `+0.0.1` (next micro) for a bugfix, or an internal change that doesn't surface to APIs
* `+0.1.0` (next minor) for an API addition
* `+1.0.0` (next major) for an API breakage (needs to be discussed on the mailing-list first)
* If some "smaller" bump already took place, you can replace it with your "bigger one". Eg, if last release has org.eclipse.m2e.editor 1.16.1; and someone already bumped version to 1.16.2 (for an internal change) and you're adding a new API, then you need to change version to 1.17.0

### ➕ Submit changes

Wild Web Developer only accepts contributions via GitHub Pull Requests against [https://github.com/eclipse/wildwebdeveloper](https://github.com/eclipse/wildwebdeveloper) repository.
