FROM eclipsecbi/fedora-gtk3-mutter:42-gtk3.24

# Back to root for install
USER 0
RUN dnf -y update && dnf -y install \
	java-21-openjdk-devel git \
    gdk-pixbuf2-modules-extra procps-ng

RUN curl -L https://dlcdn.apache.org/maven/maven-3/3.9.14/binaries/apache-maven-3.9.14-bin.tar.gz | tar -xz 

RUN curl -L https://nodejs.org/dist/v24.14.0/node-v24.14.0-linux-x64.tar.xz | tar -xJ

ENV PATH=/apache-maven-3.9.14/bin:/node-v24.14.0-linux-x64/bin:/usr/lib/jvm/java-21/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-21

#Back to named user
USER 10001
