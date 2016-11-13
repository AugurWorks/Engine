FROM alpine:latest

# Gradle environment variables
ENV GRADLE_VERSION 3.0
ENV GRADLE_HOME /usr/local/gradle
ENV PATH ${PATH}:${GRADLE_HOME}/bin
ENV GRADLE_USER_HOME /gradle

RUN apk update && \
    apk add --no-cache openjdk8

# Add app files
COPY . /app

WORKDIR /usr/local

RUN apk add --no-cache wget bash libstdc++ tzdata && \

    # Change timezone
    cp /usr/share/zoneinfo/America/New_York /etc/localtime && \
    echo "America/New_York" > /etc/timezone && \

    # Install Gradle
    wget  https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip --no-check-certificate && \
    unzip gradle-$GRADLE_VERSION-bin.zip && \
    rm -f gradle-$GRADLE_VERSION-bin.zip && \
    ln -s gradle-$GRADLE_VERSION gradle && \
    echo -ne "- with Gradle $GRADLE_VERSION\n" >> /root/.built && \

    # Build WAR file
    cd /app && \
    mkdir -p /gradle && \
    mkdir -p /opt && \
    gradle bootRepackage && \

    # Copy WAR into Tomcat
    mv build/libs/engine.war /opt/app.war && \

    # Remove Gradle and working directory
    rm /usr/local/gradle && \
    rm -rf /usr/local/gradle-$GRADLE_VERSION && \
    rm -rf /usr/local/share && \
    rm -rf /gradle && \
    apk del wget bash libstdc++ tzdata && \
    rm -rf /var/cache/apk/* && \
    rm -rf /app

# Expose port and volume
EXPOSE 8080

CMD java -jar /opt/app.war