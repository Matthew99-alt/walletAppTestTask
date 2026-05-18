WalletApp

REST API приложение для управления балансом кошельков.
Приложение поддерживает операции пополнения и списания средств с использованием PostgreSQL, Spring Boot, Liquibase и Docker Compose.

Стек технологий
Java 17
Spring Boot 3
Spring Data JPA
PostgreSQL 16
Liquibase
Docker / Docker Compose
Gradle
Функциональность
Операции с кошельком

Поддерживаются операции:

DEPOSIT — пополнение баланса
WITHDRAW — списание средств
REST API
Выполнение операции
POST /api/v1/wallet

Пример запроса:

{
  "valletId": "11111111-1111-1111-1111-111111111111",
  "operationType": "DEPOSIT",
  "amount": 1000
}
Получение баланса
GET /api/v1/wallets/{walletId}

Пример ответа:

{
  "valletId": "11111111-1111-1111-1111-111111111111",
  "balance": 1000
}
Запуск приложения
Требования

Установленные:

Docker
Docker Compose

На Windows Docker Desktop должен быть запущен.

Запуск через Docker Compose

В корне проекта выполнить:

docker compose up --build

После запуска:

API: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html
Остановка контейнеров
docker compose down

Полное удаление контейнеров и volume:

docker compose down -v

Swagger UI доступен по адресу:

http://localhost:8080/swagger-ui.html

OpenAPI JSON:

http://localhost:8080/v3/api-docs
