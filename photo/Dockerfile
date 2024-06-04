FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 AS build
COPY . /root
WORKDIR /root
RUN sbt photo/assembly

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /root/photo/target/scala-2.13/project-photo-assembly-*.jar /app/photo-service.jar
COPY photo/src/main/resources/application.conf /app/conf/application.conf
CMD ["java", "-Dconfig.file=/app/conf/application.conf", "-jar", "/app/photo-service.jar"]