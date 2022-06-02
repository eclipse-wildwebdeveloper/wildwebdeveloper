FROM eclipsecbi/fedora-gtk3-mutter:36-gtk3.24

# Back to root for install
USER 0
RUN dnf -y update && dnf -y install \
	java-17-openjdk-devel maven git
RUN dnf -y update && dnf -y install \
	nodejs npm
RUN dnf -y install xz

RUN curl -L https://nodejs.org/dist/v16.13.0/node-v16.13.0-linux-x64.tar.xz | tar -xJ

ENV PATH=/node-v16.13.0-linux-x64/bin:/usr/lib/jvm/java-17/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-17

#Back to named user
USER 10001
