# ---------------------------------------------------------------------------
# Étape 1 : compilation du projet (Maven + JDK 21)
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Wrapper Maven + descripteur (pour profiter du cache Docker)
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw -B -q -DskipTests dependency:go-offline

# Sources
COPY src src

RUN ./mvnw -B -DskipTests clean package

# ---------------------------------------------------------------------------
# Étape 2 : image d'exécution légère (JRE uniquement)
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre

RUN addgroup --system spring \
    && adduser --system --ingroup spring spring

USER spring:spring

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]