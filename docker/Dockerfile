FROM eclipsecbi/fedora-gtk3-mutter:29-gtk3.24

# Back to root for install
USER 0
RUN dnf update && dnf -y install \
	java-openjdk maven
RUN dnf update && dnf -y install \
	nodejs npm

#Back to named user
USER 10001
