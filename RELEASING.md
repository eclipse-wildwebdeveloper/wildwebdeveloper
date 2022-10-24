# Releasing Guide

# Committer status & ECA 

In order to perform a release, you need to have a committer status on the project and have the Eclipse Contributor Agreement signed, using the e-mail used at Github.
See https://eclipse.org/legal/ECA.php for details

# Steps to release

1. Update the `./RELEASE_NOTES.md` Release Notes document 
2. Trigger a build of `master` branch using [Build Jenkins CI job](https://ci.eclipse.org/wildwebdeveloper/job/Wildwebdeveloper/job/master/) and ensure it completes successfully
3. Invoke [Deploy Jenkins CI job](https://ci.eclipse.org/wildwebdeveloper/job/deploy-snapshots-to-release) with the target version to publish artifacts to the `/release` URL
4. Create and push a Git Tag
5. Create a release on GitHub project [Releases](https://github.com/eclipse/wildwebdeveloper/releases) using the tag as input with pre-filled release notes
6. Create a PMI record for the release at [Eclipse Wild Web Developer™](https://projects.eclipse.org/projects/tools.wildwebdeveloper) project page using [Create a new release](https://projects.eclipse.org/node/14981/create-release) link
7. Announce the release on the mailing-list, discussions and other relevant media: wildwebdeveloper-dev@eclipse.org
