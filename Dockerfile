# FASE 1: Construir el JAR con Maven de forma optimizada
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Establecer el directorio de trabajo
WORKDIR /usr/src/app

# Copiar solo el pom.xml para descargar dependencias y aprovechar la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Ahora copiar el resto del código fuente
COPY src ./src

# Instalar el paquete. Maven no volverá a descargar las dependencias si no han cambiado.
RUN mvn install -DskipTests

# FASE 2: Crear la imagen final y ligera para ejecución
FROM eclipse-temurin:17-jre-jammy

# Copiar el JAR construido en la fase anterior
COPY --from=build /usr/src/app/target/*.jar /app.jar

# Exponer el puerto que Render espera
EXPOSE 10000

# Comando para ejecutar la aplicación
ENTRYPOINT ["java","-jar","/app.jar"]