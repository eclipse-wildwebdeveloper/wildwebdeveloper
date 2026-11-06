Steps to deploy:

* Build

```
podman build -t USER_NAME/fedora-gtk3-mutter-java-node .
```

* Testing the image

```
podman run USER_NAME/fedora-gtk3-mutter-java-node:latest  java -version
podman run USER_NAME/fedora-gtk3-mutter-java-node:latest  mvn --version
podman run USER_NAME/fedora-gtk3-mutter-java-node:latest  node --version
podman run USER_NAME/fedora-gtk3-mutter-java-node:latest  npm --version
podman run USER_NAME/fedora-gtk3-mutter-java-node:latest  rpm -q libatomic
```

* Assign a tag to the image

```
podman tag localhost/USER_NAME/fedora-gtk3-mutter-java-node:latest docker.io/USER_NAME/fedora-gtk3-mutter-java-node:TAG
```

* Login

```
podman login docker.io
```

* Deploy

```
podman push docker.io/USER_NAME/fedora-gtk3-mutter-java-node:TAG
```

**Note:**

`USER_NAME` is used in various Jenkins files and configs. If a new image is published by another user, this has to be changed in various existing Jenkins files and configs in order for the correct image to be taken into account.

**Packages that must not be removed:**

* `libatomic` - Node.js >= 26 binaries (bundled by Wild Web Developer) link against
  `libatomic.so.1`. Without a system wide libatomic, every Node 26 process that does
  not receive `LD_LIBRARY_PATH` fails at the dynamic linker. This notably includes
  the js-debug watchdog process spawned by the debug adapter with a hardcoded minimal
  environment: its invisible death leaves debug launches blocked forever in
  `inspector.waitForDebugger()`.

**Other notes:**

* Maven is fetched from `archive.apache.org` because `dlcdn.apache.org` only serves
  the current release; pinned versions must come from the archive.
