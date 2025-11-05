FROM eclipse-temurin:21-jre-alpine

EXPOSE 8080

RUN addgroup --gid 1234 tierrating
RUN adduser --no-create-home --disabled-password -G tierrating --uid 1234 tierrating

RUN mkdir /app && chown -R tierrating:tierrating /app
COPY --chown=tierrating:tierrating target/tierrating-*.jar /app/tierrating.jar
COPY --chown=tierrating:tierrating src/main/resources/application.yml /app

USER tierrating

WORKDIR /app

ENTRYPOINT [ "java", "-jar", "tierrating.jar"]
