# Use Maven to build the application
FROM maven:3.8.8-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy only the pom.xml to leverage caching for dependencies
# COPY pom.xml .

# Download Maven dependencies (this step will be cached as long as pom.xml is unchanged)
# RUN mvn dependency:go-offline -B

# Now copy the entire source code
COPY src ./src
COPY pom.xml .

# Build the application (generate the JAR)
#RUN mvn clean package -DskipTests
RUN mvn clean package -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# Use an official OpenJDK runtime for the final image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the generated JAR from the build stage to the runtime image
COPY --from=build /app/target/URLShortenerWithRedis-0.0.1-SNAPSHOT.jar /app/URLShortenerWithRedis-0.0.1-SNAPSHOT.jar

# Expose the port that the application will run on
EXPOSE 8999

# Command to run the JAR
ENTRYPOINT ["java", "-jar", "/app/URLShortenerWithRedis-0.0.1-SNAPSHOT.jar"]
