# Usa una imagen base de Maven para construir la aplicación
FROM maven:3.8.5-openjdk-17 AS build

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo POM y los archivos de dependencias de Maven
COPY pom.xml .

# Descarga las dependencias de Maven necesarias (sin compilar aún)
RUN mvn dependency:go-offline

# Copia el código fuente del proyecto
COPY src ./src

# Compila la aplicación
RUN mvn clean package -DskipTests

# Usa una imagen base de OpenJDK para ejecutar la aplicación
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR desde la etapa de construcción
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto en el que se ejecuta la aplicación
EXPOSE 8080

# Configurar las variables de entorno para la conexión a la base de datos
ENV SPRING_DATASOURCE_URL=jdbc:mysql://monorail.proxy.rlwy.net:19629/railway
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=DrESrOCWerSSfCvMeTBlENLoKKVtgiZr

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
