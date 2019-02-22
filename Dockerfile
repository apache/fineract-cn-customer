FROM openjdk:8-jdk-alpine

ARG customer_port=2024

ENV server.max-http-header-size=16384 \
    cassandra.clusterName="Test Cluster" \
    server.port=$customer_port \
    system.initialclientid=service-runner

WORKDIR /tmp
COPY customer-service-boot-0.1.0-BUILD-SNAPSHOT.jar .

CMD ["java", "-jar", "customer-service-boot-0.1.0-BUILD-SNAPSHOT.jar"]
