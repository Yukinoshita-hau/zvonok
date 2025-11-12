FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-ubi9-minimal
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_OPTS=""

RUN microdnf install -y shadow-utils \
    && microdnf clean all \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /app --shell /sbin/nologin app

COPY --from=build /app/target/zvonok-0.0.1-SNAPSHOT.jar /app/app.jar

RUN chown app:app /app/app.jar \
    && printf '#!/usr/bin/env sh\nexec java $JAVA_OPTS -jar /app/app.jar "$@"\n' > /entrypoint.sh \
    && chmod +x /entrypoint.sh \
    && chown app:app /entrypoint.sh

EXPOSE 8080

USER app

ENTRYPOINT ["/entrypoint.sh"]

