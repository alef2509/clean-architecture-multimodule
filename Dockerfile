# syntax=docker/dockerfile:1
# Generic image for any service module. Build the jars first (./mvnw package),
# then: docker build --build-arg MODULE=order-service .

FROM eclipse-temurin:21-jre-alpine AS extract
ARG MODULE
WORKDIR /build
COPY ${MODULE}/target/${MODULE}-*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=extract /build/extracted/dependencies/ ./
COPY --from=extract /build/extracted/spring-boot-loader/ ./
COPY --from=extract /build/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/extracted/application/ ./
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
