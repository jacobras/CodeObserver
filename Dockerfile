FROM eclipse-temurin:25-jre-alpine

# Copy server
COPY server/build/libs/*.jar /app/server.jar

# Copy web UI
COPY web/build/dist/wasmJs/productionExecutable /app/web

EXPOSE 8080
VOLUME /data

CMD ["java", "-jar", "/app/server.jar"]