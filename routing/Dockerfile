FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 AS build
COPY . /root
WORKDIR /root
RUN sbt routing/assembly

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /root/routing/target/scala-2.13/project-routing-assembly-*.jar /app/routing-service.jar
COPY routing/src/main/resources/application.conf /app/conf/application.conf
CMD ["java", "-Dconfig.file=/app/conf/application.conf", "-jar", "/app/routing-service.jar"]