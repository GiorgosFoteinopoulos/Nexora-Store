# syntax=docker/dockerfile:1

# ---------- Stage 1: build the jar (JDK + Maven) ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

# The cache mount keeps the Maven repository between builds: dependencies download only once.
# Tests are skipped here: they need Docker (Testcontainers), and CI already runs them.
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -q package -DskipTests

# Split the fat jar into layers: libraries change rarely, our code changes often.
RUN cp target/nexora-store-*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# ---------- Stage 2: run it (JRE only, non-root) ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system nexora && useradd --system --gid nexora --no-create-home nexora

COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER nexora
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]