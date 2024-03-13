FROM eclipse-temurin:17.0.4.1_1-jre as builder
WORKDIR extracted
ADD target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17.0.4.1_1-jre
WORKDIR app
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
EXPOSE 8890
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

#FROM openjdk:17-jdk-slim as build_jar
#WORKDIR app
#
#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#COPY src src
#RUN ./mvnw install -DskipTests
#
#FROM eclipse-temurin:17.0.4.1_1-jre as build_layers
#WORKDIR extracted
#COPY --from=build_jar /app/target/*.jar ./app.jar
##ADD target/*.jar app.jar
#RUN java -Djarmode=layertools -jar app.jar extract
#
#FROM eclipse-temurin:17.0.4.1_1-jre
#WORKDIR app
#COPY --from=build_layers extracted/dependencies/ ./
#COPY --from=build_layers extracted/spring-boot-loader/ ./
#COPY --from=build_layers extracted/snapshot-dependencies/ ./
#COPY --from=build_layers extracted/application/ ./
#EXPOSE 8890
#ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]