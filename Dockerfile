# Estágio 1: compila o projeto com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# Estágio 2: JRE + Graphviz para rodar as demos (2-3-4, kD, rubro-negra e a Capitania) e gerar os PNGs
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends graphviz \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/barcos-belem.jar app.jar
# As demos gravam em ./saida (= /app/saida); monte um volume ali para pegar os arquivos.
# Sem argumento roda todas; para uma só, passe 234, kd, rn ou capitania ao final do docker run.
# UTF-8 explícito para os acentos não saírem como "?" no terminal do container.
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-jar", "app.jar"]
