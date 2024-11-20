FROM eclipsecbi/fedora-gtk3-mutter:41-gtk3.24

# Back to root for install
USER 0
RUN dnf -y update && dnf -y install \
	java-21-openjdk-devel git
RUN dnf -y install nodejs npm
RUN dnf -y install xz
RUN dnf -y install procps-ng

RUN curl -L https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz | tar -xzv 

RUN curl -L https://nodejs.org/dist/v22.11.0/node-v22.11.0-linux-x64.tar.xz | tar -xJ

ENV PATH=/apache-maven-3.9.9/bin:/node-v22.11.0-linux-x64/bin:/usr/lib/jvm/java-21/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-21

#Back to named user
USER 10001
