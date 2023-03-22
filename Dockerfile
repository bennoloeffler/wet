FROM openjdk:8-alpine

COPY target/uberjar/wet.jar /wet/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/wet/app.jar"]
