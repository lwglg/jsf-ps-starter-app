# Etapa 1: build da aplicação com Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build

ENV JSF_APP_ROOT=companyman

WORKDIR /build

COPY ${JSF_APP_ROOT}/pom.xml .
RUN mvn -B dependency:go-offline

COPY ${JSF_APP_ROOT}/src ./src
RUN mvn -B clean package -DskipTests

# Etapa 2: runtime em Apache Tomcat 10.1 (JDK 21)
FROM tomcat:10.1-jdk21-temurin

ENV TARGET_WAR=companyman

RUN rm -rf /usr/local/tomcat/webapps/*

# Publica o WAR gerado como ROOT, expondo a aplicação na raiz do contexto
COPY --from=build /build/target/${TARGET_WAR}.war /usr/local/tomcat/webapps/ROOT.war
COPY ./infra/docker/develop/tomcat/scripts/setenv.sh /usr/local/tomcat/bin/setenv.sh

RUN chmod +x /usr/local/tomcat/bin/setenv.sh

EXPOSE 8080

CMD ["catalina.sh", "run"]
