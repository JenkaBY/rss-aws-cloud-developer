FROM gradle:8.5-jdk21-alpine AS java-build

ARG APP_HOME=/home/gradle/src
WORKDIR $APP_HOME
COPY --chown=gradle:gradle . .
RUN gradle build -x test --no-daemon

ARG JAR_FILE=bff-service.jar
ARG JAR_TARGET_DIR=/build/libs
ARG JAR=$APP_HOME$JAR_TARGET_DIR/$JAR_FILE

WORKDIR $APP_HOME/layers
RUN java -Djarmode=tools -jar $JAR extract --layers --launcher

#runtime image
FROM amazoncorretto:21-alpine3.20-jdk AS runtime

RUN mkdir /app
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=java-build /home/gradle/src/layers/bff-service/dependencies/ ./
COPY --from=java-build /home/gradle/src/layers/bff-service/spring-boot-loader/ ./
COPY --from=java-build /home/gradle/src/layers/bff-service/snapshot-dependencies/ ./
COPY --from=java-build /home/gradle/src/layers/bff-service/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
