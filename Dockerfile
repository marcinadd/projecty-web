FROM openjdk:13-alpine
EXPOSE 8080
WORKDIR /usr/local/bin/
COPY ./build/libs/projecty-web-0.0.1-SNAPSHOT.jar projecty-web.jar
CMD ["java", "-Dspring.profiles.active=docker", "-jar", "projecty-web.jar"]
