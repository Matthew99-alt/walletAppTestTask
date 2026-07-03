# WalletApp

REST API приложение для управления балансом кошельков.
Поддерживает создание кошелька, пополнение и списание средств с корректной обработкой конкурентных запросов. Использует PostgreSQL, Spring Boot, Liquibase и Docker Compose.

## Стек технологий

- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL 16
- Liquibase
- Docker / Docker Compose
- Gradle
- Testcontainers (интеграционные тесты)
- k6 (нагрузочное тестирование)

## REST API

### Создание кошелька

`POST /api/v1/wallets`

Пример запроса:

```json
{
  "walletId": "11111111-1111-1111-1111-111111111111",
  "amount": 1000
}
```

Ответы:
- `201 Created` — кошелёк создан
- `409 Conflict` — кошелёк с таким ID уже существует
- `400 Bad Request` — ошибка валидации

### Выполнение операции (пополнение / списание)

`POST /api/v1/wallet`

Пример запроса:

```json
{
  "walletId": "11111111-1111-1111-1111-111111111111",
  "operationType": "DEPOSIT",
  "amount": 1000
}
```

`operationType`:
- `DEPOSIT` — пополнение баланса
- `WITHDRAW` — списание средств

Ответы:
- `200 OK` — операция выполнена, в теле актуальное состояние кошелька
- `404 Not Found` — кошелёк не найден
- `409 Conflict` — недостаточно средств для списания
- `400 Bad Request` — невалидный JSON или ошибка валидации

### Получение баланса

`GET /api/v1/wallets/{walletId}`

Пример ответа:

```json
{
  "walletId": "11111111-1111-1111-1111-111111111111",
  "amount": 1000
}
```

Ответы:
- `200 OK`
- `404 Not Found` — кошелёк не найден

## Запуск приложения

### Требования

- Docker
- Docker Compose

На Windows Docker Desktop должен быть запущен.

### Настройка окружения

Перед первым запуском создать файл `.env` в корне проекта (пример — в `.env.example`):

```bash
cp .env.example .env
```

и задать в нём пароль БД (`POSTGRES_PASSWORD`). Файл `.env` в git не попадает.

### Запуск через Docker Compose

В корне проекта выполнить:

```bash
docker compose up --build
```

После запуска:

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### Остановка

```bash
docker compose down
```

Полное удаление контейнеров и volume (данные БД будут удалены):

```bash
docker compose down -v
```

## Тесты

Интеграционные тесты на Testcontainers (требуется запущенный Docker):

```bash
./gradlew test
```

## Нагрузочное тестирование

Скрипт k6 шлёт конкурентные операции пополнения в один кошелёк — проверка корректности при параллельных записях:

```bash
k6 run k6/test.js
```
