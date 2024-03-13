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
EXPOSE 8889
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

## Step 1: Build the project and package the service
#FROM maven:3.8.4-openjdk-17-slim AS build
#WORKDIR /app
## Copy the entire project
#COPY order_service/mvnw .
#COPY order_service/.mvn .
#COPY order_service/pom.xml .
#COPY order_service/src src
##COPY . .
## Package the specific service (adjust the path as necessary)
#RUN mvn -f ./pom.xml package -DskipTests
#
## Step 2: Create the final image with just the JAR file
#FROM eclipse-temurin:17-jre
#WORKDIR /app
## Copy the JAR from the build stage
#COPY --from=build /app/order_service/target/*.jar ./app.jar
#EXPOSE 8889
#ENTRYPOINT ["java", "-jar", "app.jar"]


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
#EXPOSE 8889
#ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

#FROM openjdk:17-jdk-slim
#VOLUME /tmp
#ARG DEPENDENCY=/workspace/app/target
#COPY --from=build ${DEPENDENCY}/*.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]