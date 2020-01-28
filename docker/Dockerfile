FROM eclipsecbi/fedora-gtk3-mutter:31-gtk3.24

# Back to root for install
USER 0
RUN dnf -y update && dnf -y install \
	java-latest-openjdk maven
RUN dnf -y update && dnf -y install \
	nodejs npm

#Back to named user
USER 10001
