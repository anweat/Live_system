# Audience Service Dockerfile
FROM redis:7-alpine AS redis-base

FROM eclipse-temurin:11-jre-jammy

COPY --from=redis-base /usr/local/bin/redis-server /usr/local/bin/redis-server
COPY --from=redis-base /usr/local/bin/redis-cli /usr/local/bin/redis-cli

RUN mkdir -p /data /var/log/redis && \
    useradd -r redis || true

WORKDIR /app

ENV JAVA_OPTS="-Xms512m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=production
ENV REDIS_ENABLED=true

COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8082 6379

ENTRYPOINT ["/app/docker-entrypoint.sh"]
