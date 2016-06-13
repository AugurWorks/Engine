FROM alpine:latest

# Grails environment variables
ENV GRAILS_VERSION 2.4.5
ENV JAVA_HOME /usr/lib/jvm/default-jvm
ENV GRAILS_HOME /usr/local/grails
ENV PATH $GRAILS_HOME/bin:$PATH

# Tomcat environment variables
ENV TOMCAT_MAJOR 8
ENV TOMCAT_VERSION 8.0.35
ENV TOMCAT_TGZ_URL https://www.apache.org/dist/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz
ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH

RUN apk update && \
    apk add --no-cache openjdk8 curl tar tzdata && \

    # Change timezone
    cp /usr/share/zoneinfo/America/New_York /etc/localtime && \
    echo "America/New_York" > /etc/timezone && \

    # Install Tomcat
    mkdir -p "$CATALINA_HOME" && \
    cd $CATALINA_HOME && \
    set -x && \
    curl -fSL "$TOMCAT_TGZ_URL" -o tomcat.tar.gz && \
    curl -fSL "$TOMCAT_TGZ_URL.asc" -o tomcat.tar.gz.asc && \
    tar -xvf tomcat.tar.gz --strip-components=1 && \
    rm bin/*.bat && \
    rm tomcat.tar.gz* && \
    rm -rf /usr/local/tomcat/webapps/*

# Add app files
COPY . /app

WORKDIR /usr/local

RUN apk update && \
    apk add --no-cache wget && \

    # Install Grails
    wget https://github.com/grails/grails-core/releases/download/v$GRAILS_VERSION/grails-$GRAILS_VERSION.zip && \
    unzip grails-$GRAILS_VERSION.zip && \
    rm -rf grails-$GRAILS_VERSION.zip && \
    ln -s grails-$GRAILS_VERSION grails && \

    # Build WAR file
    cd /app && \
    grails war ROOT.war && \

    # Copy WAR into Tomcat
    mv ROOT.war /usr/local/tomcat/webapps/ROOT.war && \

    # Remove Grails and working directory
    cd /usr/local/tomcat && \
    rm /usr/local/grails && \
    rm -rf /root/.m2 && \
    rm -rf /usr/local/grails-$GRAILS_VERSION && \
    rm -rf /usr/local/share && \
    apk del wget tzdata && \
    rm -rf /var/cache/apk/* && \
    rm -rf /app

# Expose port
EXPOSE 8080

CMD ["catalina.sh", "run"]
