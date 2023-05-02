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

USER_NAME in used in various Jenkins files and configs. If new image is published by another user this has to be changed in various existing Jenkins files and configs in order the correct image to be taken into account.