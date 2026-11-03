# syntax=docker/dockerfile:1

# -----------------------------------------------------------------------------
# Build stage — compile the Spring Boot fat JAR (Java 25)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk-jammy AS build

WORKDIR /workspace

# Wrapper + POM first for better layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Resolve dependencies before copying sources (cache-friendly)
RUN ./mvnw -B -DskipTests dependency:go-offline || ./mvnw -B -DskipTests dependency:resolve

COPY src src
RUN ./mvnw -B -DskipTests package \
    && JAR="$(ls target/backend-*.jar | grep -v '\.original$' | head -n1)" \
    && cp "$JAR" /workspace/application.jar

# -----------------------------------------------------------------------------
# Runtime stage — minimal JRE image
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-jammy AS runtime

WORKDIR /app

# Non-root user
RUN groupadd --system --gid 10001 matibabu \
    && useradd --system --uid 10001 --gid matibabu --home-dir /app --shell /usr/sbin/nologin matibabu \
    && mkdir -p /data \
    && chown -R matibabu:matibabu /app /data

COPY --from=build --chown=matibabu:matibabu /workspace/application.jar /app/application.jar

USER matibabu

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0" \
    SERVER_PORT=8080 \
    # SQLite under /data so a volume can persist the facility DB
    DB_URL=jdbc:sqlite:/data/matibabu.db

EXPOSE 8080

VOLUME ["/data"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/${SERVER_PORT:-8080} && printf "GET /csrf HTTP/1.0\r\nHost: localhost\r\n\r\n" >&3 && cat <&3 | head -n1 | grep -q "200\|403\|401"' || exit 1

ENTRYPOINT ["bash", "-c", "exec java $JAVA_OPTS -jar /app/application.jar"]
