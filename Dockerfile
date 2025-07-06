# FASE 1: Construir el JAR con Maven
# Usamos una imagen de Maven que incluye JDK 17 para compilar el proyecto.
FROM maven:3.9.6-eclipse-temurin-17 AS build

# CORRECCIÓN CLAVE: Se establece el directorio de trabajo.
# Todas las siguientes instrucciones se ejecutarán dentro de /usr/src/app.
WORKDIR /usr/src/app

# Copiamos primero el pom.xml para aprovechar el cache de capas de Docker.
COPY pom.xml .

# Copiamos el resto del código fuente del proyecto.
COPY src ./src

# Ejecutamos el comando de Maven para construir el JAR.
# Ahora se creará la carpeta 'target' dentro de /usr/src/app.
RUN mvn install -DskipTests

# FASE 2: Crear la imagen final y ligera para ejecución
# Usamos una imagen optimizada de Java 17 para ejecutar la aplicación.
FROM eclipse-temurin:17-jre-jammy

# CORRECCIÓN: Se especifica la ruta completa y correcta desde donde copiar el JAR.
# El JAR se encuentra en /usr/src/app/target/ en la fase de 'build'.
COPY --from=build /usr/src/app/target/*.jar /app.jar

# Exponemos el puerto 8080 (el que usa Spring Boot por defecto)
EXPOSE 8080

# Comando para ejecutar la aplicación cuando el contenedor se inicie
ENTRYPOINT ["java","-jar","/app.jar"]