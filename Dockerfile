# Базовый образ с JDK 17 (или нужной тебе версии)
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем .jar-файл в контейнер
COPY target/obmen-1.2.3.jar app.jar

# Указываем команду запуска
ENTRYPOINT ["java", "-jar", "app.jar"]