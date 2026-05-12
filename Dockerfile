FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Descarga dependencias en una capa separada para aprovechar cache.
RUN ./mvnw -B -ntp -DskipTests dependency:go-offline

COPY src/ src/

# Construye y normaliza el artefacto a un nombre predecible (/app/app.jar)
# sin usar comodines en COPY entre etapas.
RUN set -eux; \
    ./mvnw -B -ntp -DskipTests package; \
    JAR_COUNT="$(find /app/target -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' ! -name '*.original' | wc -l)"; \
    [ "$JAR_COUNT" -eq 1 ]; \
    JAR_PATH="$(find /app/target -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' ! -name '*.original')"; \
    cp "$JAR_PATH" /app/app.jar

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring --create-home spring

COPY --from=builder --chown=spring:spring /app/app.jar /app/app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
