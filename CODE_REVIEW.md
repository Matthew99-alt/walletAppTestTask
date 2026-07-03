# Ревью проекта WalletApp

Дата: 2026-07-03. Цель проекта: портфолио, уровень middle-разработчик.
Файл фиксирует исходное состояние и план доработок.

**Общая оценка:** архитектурно проект здоровый — слои (controller/service/repository), пессимистичная блокировка, Liquibase, Testcontainers, ControllerAdvice. Это уже выше junior-уровня. Топят его опечатка `vallet`, upsert-дыра в `/saveWallet`, отладочный `pg_sleep`, пароль в git и сломанный k6-тест.

---

## Критично (чинить обязательно)

### 1. ✅ (исправлено 2026-07-03) Опечатка `vallet` по всему проекту
`valletId`, `vallet_id` — в entity (`Wallet.java:18`), DTO, схеме БД, README. Первое, что увидит ревьюер резюме. Массовый rename → `walletId` / `wallet_id` (включая Liquibase changelog).

### 2. ✅ (исправлено 2026-07-03) `POST /saveWallet` — дыра и не REST (`WalletController.java:31`)
- Глагол в URL — нарушение REST-конвенций.
- Хуже: `repository.save()` — это upsert. Запрос с существующим UUID перезапишет баланс существующего кошелька любым значением.
- Фикс: `POST /wallets`, проверка `existsById` → 409 Conflict, при успехе — 201 Created.

### 3. ✅ (исправлено 2026-07-03) Отладочный код в проде
`WalletService.loadTestHold()` + `WalletRepository.sleep()` с `pg_sleep(5)` (`WalletRepository.java:19`). Мёртвый код, из контроллера не вызывается. Удалить.

### 4. ✅ (исправлено 2026-07-03, но пароль остался в истории git — см. примечание) Пароль в git
`7890123456Upet` в `docker-compose.yml:11`. Вынести в `.env`, добавить `.env` в `.gitignore`, в репо положить `.env.example`. Red flag по security-гигиене.

**Примечание после фикса:** пароль убран из текущего кода, но остался в истории git (старые коммиты). При переписывании истории (пункт «Полировка» про git log) он уйдёт вместе с ней; сам пароль считать скомпрометированным и нигде не переиспользовать.

### 5. ✅ (исправлено 2026-07-03) README не соответствует API
- Пример ответа GET показывает поле `balance`, реально API возвращает `amount`.
- Эндпоинт `/saveWallet` не задокументирован вообще.
- Примеры с `valletId` обновить после rename.

### 6. ✅ (исправлено 2026-07-03) k6-тест сломан (`k6/test.js:9`)
`GET /wallets/' + __VU` шлёт ID `1..50`, а ключи — UUID → 100% запросов 400/404, тест ничего не измеряет. Главная фишка задачи — конкурентные записи в один кошелёк: k6 должен бить `POST /wallet` с одним UUID.

---

## Важно (уровень middle)

### 7. Обработчик `PessimisticLockingFailureException` не срабатывает (`GlobalControllerAdvice.java:92`)
Коммит "503 is not working" — про это. Причина: `PESSIMISTIC_WRITE` в Postgres ждёт блокировку бесконечно, таймаута нет — исключение не бросается, запросы висят. Фикс:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
```

либо `SELECT ... FOR UPDATE NOWAIT`.

### 8. Альтернатива блокировке — атомарный UPDATE
Для высокого RPS на один кошелёк select-for-update + save — узкое место (два round-trip, очередь на блокировке). Вариант одним запросом:

```sql
UPDATE wallets SET amount = amount + :delta
WHERE wallet_id = :id AND amount + :delta >= 0
```

`rowsUpdated == 0` → не хватает средств (либо кошелёк не найден — различать через `existsById`). Даже если оставить пессимистичную блокировку — раздел в README «как решена конкурентность и почему» превращает проект из «ещё один CRUD» в аргумент на собеседовании.

### 9. Нет теста на конкурентность
Вся суть задачи. Тест: `ExecutorService`, N параллельных withdraw с общего кошелька, ассерты — баланс не ушёл в минус, сумма сходится, нет потерянных обновлений. Сильнейший пункт для портфолио.

### 10. Слабые ассерты в тестах (`WalletControllerIT.java`)
- `shouldDepositMoney` проверяет только статус 200 — не проверяет, что баланс стал 1000.
- Хелпер `createWallet` не проверяет статус ответа — при падении создания тест падает дальше с непонятной ошибкой.
- `shouldCreateWallet` шлёт `WalletRequestDTO` в эндпоинт, ожидающий `WalletDTO` — работает случайно из-за совпадения имён полей.

### 11. Dockerfile — одна стадия, без кеша слоёв
- `COPY . .` до сборки → любое изменение кода пересобирает всё, включая скачивание зависимостей.
- JDK-образ в рантайме (~400MB лишних).
- Фикс: multi-stage — стадия сборки (сначала `COPY gradle* settings*` + прогрев зависимостей, потом исходники), рантайм на `eclipse-temurin:17-jre`.
- Добавить `.dockerignore` (`.git`, `build`, `.idea`).

### 12. Приложение не запускается без Docker
`application.yaml` требует env-переменные без дефолтов. Добавить дефолты (`${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/wallet_db}`) или профиль `local`.

---

## Полировка

- [ ] `git log`: "I did it", "503 is not working" — переписать историю (interactive rebase / squash); для портфолио история коммитов — витрина.
- [ ] `{WALLET_UUID}` в пути → `{walletId}` (`WalletController.java:21`).
- [ ] Имена методов `getAWallet` / `saveAWallet` → `getWallet` / `createWallet`.
- [ ] `throws InsufficientFundsException, EntityNotFoundException` в сигнатурах — оба unchecked, объявление лишнее.
- [ ] Явный `walletRepository.save()` внутри `@Transactional` не нужен — dirty checking сохранит сам.
- [ ] `ErrorDTO.number` → `status`; лучше — стандартный `ProblemDetail` (RFC 7807, встроен в Spring 6).
- [ ] `show-sql: true` — убрать или вынести в dev-профиль.
- [ ] Пакет `walletApp` → `walletapp` (Java-конвенция: пакеты lowercase).
- [ ] `@RequestMapping("api/v1")` → `"/api/v1"`.
- [ ] `@RequiredArgsConstructor` на маппере без полей — убрать. Ручной маппер ок, но MapStruct — плюс в стек.
- [ ] Хендлер `RuntimeException` → `Exception`, иначе checked-исключения пролетают мимо advice.
- [ ] `HELP.md` (мусор от Spring Initializr) — удалить.

---

## Чего не хватает для middle-портфолио

1. **CI** — GitHub Actions: build + тесты на PR. Полдня работы, сильный сигнал.
2. **README-раздел про конкурентность** — сравнение пессимистичной блокировки / оптимистичной с retry / атомарного UPDATE, почему выбран вариант. Об этом спросят на собеседовании.
3. **Результаты нагрузочного теста** в README (после починки k6): RPS, латентность, отсутствие потерянных обновлений.
4. **Swagger-аннотации** (`@Operation`, `@ApiResponse`) — springdoc подключен, но документация голая.

---

## Порядок работы (рекомендуемый)

1. Критичные пункты 1–6.
2. Таймаут блокировки (7) + тест на конкурентность (9).
3. Ассерты в тестах (10), Dockerfile (11), локальный запуск (12).
4. Полировка + CI + README про конкурентность.
