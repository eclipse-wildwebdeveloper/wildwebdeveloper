FROM eclipsecbi/fedora-gtk3-mutter:36-gtk3.24

# Back to root for install
USER 0
RUN dnf -y update && dnf -y install \
	java-17-openjdk-devel maven git
RUN dnf -y install nodejs npm
RUN dnf -y install xz
RUN dnf -y install procps-ng

RUN curl -L https://dlcdn.apache.org/maven/maven-3/3.9.2/binaries/apache-maven-3.9.2-bin.tar.gz | tar -xzv 

RUN curl -L https://nodejs.org/dist/v18.16.0/node-v18.16.0-linux-x64.tar.xz | tar -xJ

ENV PATH=/apache-maven-3.9.2/bin:/node-v18.16.0-linux-x64/bin:/usr/lib/jvm/java-17/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-17

#Back to named user
USER 10001
