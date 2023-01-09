# customer
FROM openjdk:17-jdk-slim
ENV JAVA_OPTS=""
ADD target/spring-boot-customer.jar spring-boot-customer.jar
ENTRYPOINT exec java $JAVA_OPTS -jar /spring-boot-customer.jar