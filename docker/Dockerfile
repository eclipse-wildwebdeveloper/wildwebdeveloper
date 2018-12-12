FROM eclipsecbi/ubuntu-gtk3-metacity:18.10-gtk3.24

# Back to root for install
USER 0
RUN apt-get update && apt-get install -y --no-install-recommends \
	openjdk-8-jdk maven
RUN apt-get update && apt-get install -y --no-install-recommends \
	nodejs npm

#Back to named user
USER 10001
