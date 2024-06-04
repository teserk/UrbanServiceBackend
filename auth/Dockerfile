FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 AS build
COPY . /root
WORKDIR /root
RUN sbt auth/assembly

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /root/auth/target/scala-2.13/project-auth-assembly-*.jar /app/auth-service.jar
CMD ["java", "-jar", "/app/auth-service.jar"]