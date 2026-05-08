FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system atlas && useradd --system --gid atlas --home-dir /app atlas

COPY --from=builder /workspace/target/atlas-*.jar app.jar

RUN chown -R atlas:atlas /app

USER atlas

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
