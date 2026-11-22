# Estágio 1: compila o projeto com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# Estágio 2: JRE + Graphviz para rodar a demo e gerar os PNGs
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends graphviz \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/barcos-belem.jar app.jar
# A demo grava em ./saida (= /app/saida); monte um volume ali para pegar os arquivos
CMD ["java", "-jar", "app.jar"]
