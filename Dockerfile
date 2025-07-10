# ----------------------------
# Build stage - Otimizado para cache
# ----------------------------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia apenas o pom.xml para resolver dependências e cachear
COPY pom.xml .

# Baixa as dependências no cache (não compila ainda)
RUN mvn dependency:go-offline -B

# Copia o restante do código
COPY src ./src

# Compila em paralelo, sem testes
RUN mvn clean package -DskipTests

# ----------------------------
# Runtime stage - Distroless leve
# ----------------------------
FROM gcr.io/distroless/java21-debian12

WORKDIR /app

# Copia o JAR gerado, assumindo que há apenas um na pasta target/
COPY --from=builder /app/target/*.jar app.jar

# JVM tuning para containers com poucos recursos
ENV JAVA_TOOL_OPTIONS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=80.0 \
  -XX:+UseSerialGC \
  -XX:MinHeapFreeRatio=20 \
  -XX:MaxHeapFreeRatio=40 \
  -Xss512k \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.main.lazy-initialization=true \
  -Dspring.jmx.enabled=false"

# Limites explícitos se o orchestrator não for respeitado
ENV _JAVA_OPTIONS="-XX:MaxRAM=300m"

# expõe por compatibilidade (mas nginx escuta na 9999)
EXPOSE 8080

# Executa como usuário seguro
USER nonroot

ENTRYPOINT ["java", "-jar", "app.jar"]
