# DigITalMine

Spring Boot приложение для цифрового управления процессами (Digital Mine Platform).

## ⚙️ Установка

```bash
git clone https://github.com/gigastack-official/hackathon-spring-2025-backend
cd digitalmine
```

## 🚀 Запуск

1. Убедись, что Docker запущен:

```bash
docker ps
```

2. Запусти проект:

```bash
./gradlew bootRun
```

> Spring Boot автоматически запустит контейнеры из `compose.yaml`.


## 💡 Полезные команды

| Команда                   | Описание                          |
|--------------------------|-----------------------------------|
| `./gradlew bootRun`      | Запуск Spring Boot приложения     |
| `./gradlew clean build`  | Полная сборка                     |
| `./gradlew test`         | Запуск тестов                     |
| `docker compose up`      | Вручную запустить Docker-сервисы  |


http://127.0.0.1:8080/swagger-ui/index.html