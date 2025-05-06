# Usa una imagen base de Java 17 JRE ligera (ajusta si usas otra versión de Java)
FROM eclipse-temurin:17-jre-jammy

# Argumento para el nombre del JAR (lo pasaremos desde Maven o al construir)
ARG JAR_FILE=target/*.jar

# Establece un directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR desde la carpeta target al directorio de trabajo en el contenedor
COPY ${JAR_FILE} app.jar

# Expone el puerto en el que corre la aplicación Spring Boot (por defecto 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "/app/app.jar"]