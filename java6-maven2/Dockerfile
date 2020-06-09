FROM ticketfly/java:6

ARG MAVEN_VERSION=2.0.11
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
        && apk upgrade --update \
        && apk add --update curl tar \
        && curl -sSL http://archive.apache.org/dist/maven/binaries/apache-maven-2.0.11-bin.tar.gz -o /tmp/apache-maven.tar.gz \
        && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
        && apk del curl tar \
        && rm /tmp/apache-maven.tar.gz \
        && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

COPY mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
COPY settings-docker.xml /usr/share/maven/ref

ENTRYPOINT ["/usr/local/bin/mvn-entrypoint.sh"]
CMD ["mvn"]
