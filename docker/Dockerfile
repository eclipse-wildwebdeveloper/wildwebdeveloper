FROM eclipsecbi/fedora-gtk3-mutter:31-gtk3.24

# Back to root for install
USER 0
RUN dnf -y update && dnf -y install \
	java-11-openjdk-devel maven
RUN dnf -y update && dnf -y install \
	nodejs npm
	
ENV PATH=/usr/lib/jvm/java-11/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-11

#Back to named user
USER 10001
