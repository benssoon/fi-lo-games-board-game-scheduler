FROM eclipse-temurin:25

WORKDIR /app

RUN mkdir /app/image-uploads

COPY ./target/fi-lo-games-1.0.jar ./fi-lo-games-api.jar

EXPOSE 8080

CMD ["java", "-jar", "fi-lo-games-api.jar"]