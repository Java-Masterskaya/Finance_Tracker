# Finance Tracker

Приложение для контроля личных финансов.

## Минимальные требования

* Docker Desktop

## Запуск проекта

```bash
docker compose up --build
```

## Остановка проекта

```bash
docker compose down
```

## Остановка с удалением данных

```bash
docker compose down -v
```

## Используемые сервисы

| Сервис     | Порт  |
|------------|-------|
| Приложение | 8080  |
| PostgreSQL | 5432  |
| Redis      | 6379  |
| Kafka      | 29092 |

## Переменные окружения

Основные настройки задаются в файле `.env`.

Пример переменных находится в `.env.example`.

# Миграция базы данных

Осуществляется с помощью Liquibase, без использования rollback механики (для отмены предыдущего действия необходимо
создавать новый файл с изменениями)

Каждое действие с изменением базы данных описывается в отдельном `.yaml` файле в каталоге changes

Формат названия файла представляет из себя `текущая дата-действие-предмет действия`

После создания нового файла с изменениями необходимо зафиксировать имя этого файла в `changelog-master.yaml` через
команду `- include`

## Текущая структура базы данных

На данный момент база данных содержит в себе 2 таблицы `accounts` и `transactions`

| Accounts | Тип     |
|----------|---------|
| id       | INT     |
| name     | VARCHAR |
| currency | VARCHAR |
| balance  | FLOAT   |
| user_id  | INT     |

| Transactions   | Тип              |
|----------------|------------------|
| transaction_id | INT              |
| account_id     | INT              |
| type           | transaction_type |
| amount         | FLOAT            |
| category       | VARCHAR          |
| date           | DATE             |
| description    | VARCHAR          |

## Индексация

На данный момент реализованы 3 индексации для таблицы `transactions`

С помощью них осуществляется быстрый поиск по `account_id` и `date`

## Security

- Все входящие запросы проверяются на соответствие спецификации `finance_openapi.yaml`
- Используется `swagger-request-validator` от Atlassian
- Настроена в `ValidationConfig.java`

- Запросы разрешены только с доверенных источников
- Настроен в `ValidationConfig.java`

- Все пароли и ключи только из переменных окружения (`.env`)
- В коде и конфигурациях нет захардкоженных секретов

- Единый формат ошибок через `GlobalExceptionHandler.java`
- Стек-трейсы не отдаются в ответах

## Аутентификация и получение токена

**Регистрация нового пользователя**

```bash
POST http://localhost:8080/api/registration
Content-Type: application/json
{
    "email": "user@example.com",
    "password": "password1234567890",
    "firstName": "Иван"
}
```

**Войти в личный кабинет**

```bash
POST http://localhost:8080/api/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password1234567890"
}
```

Ответ

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "email": "user@example.com",
  "role": "ROLE_USER"
}
```

**Тестовый администратор**

Создаётся автоматически при первом запуске:

| Роль    | Email               | Пароль     |
|---------|---------------------|------------|
| `ADMIN` | `admin@example.com` | `admin123` |

### Использование токена

Добавьте в заголовок всех защищённых запросов:
`Authorization: Bearer <ваш_токен>`

## Роли доступа

| Аннотация                                      | Доступ             |
|------------------------------------------------|--------------------|
| `@PreAuthorize("hasRole('ADMIN')")`            | только `ADMIN`     |
| `@PreAuthorize("hasAnyRole('USER', 'ADMIN')")` | `USER` или `ADMIN` |
| `@PreAuthorize("permitAll()")`                 | открытый доступ    |

### Получение текущего пользователя в коде

```java
// ID текущего пользователя
Long userId = securityUtils.getCurrentUserId();
```

### Изоляция данных

Каждый запрос к личным данным (счета, транзакции) автоматически проверяет принадлежность сущности текущему пользователю.  
Доступ к чужим данным через подмену ID в URL или body невозможен.

**Пример проверки в коде:**

```java

@GetMapping("/{accountId}")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<Account> getAccount(@PathVariable Long accountId) {
    Long userId = securityUtils.getCurrentUserId();
    Account account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AccessDeniedException("Access denied!"));
    return ResponseEntity.ok(account);
}
```

## Основные эндпоинты (API v1)

Для всех защищенных эндпоинтов обязательны заголовки:

* `Authorization: Bearer <token>`

### Работа со счетами (/api/v1/accounts)

**Получить список счетов пользователя**

* Метод: `GET`
* Пример ответа (200 OK):

```JSON
[
  {
    "id": 1,
    "name": "Зарплатная карта",
    "currency": "RUB",
    "balance": 150500.5
  }
]
```

**Создать новый счет**

* Метод: `POST`
* Тело запроса:

```JSON
{
  "name": "string",
  "currency": "RUB",
  "initialBalance": 0
}
```

### Создание транзакции (`/api/v1/transactions`)

**Метод:** `POST`

**Заголовки:**
- `userId: {id пользователя}`
- `Authorization: Bearer {jwt_token}`

**Тело запроса:**

```json
{
  "accountId": 1,
  "type": "EXPENSE",
  "amount": 1500.00,
  "category": "Супермаркеты",
  "date": "2026-05-24",
  "description": "Покупка продуктов"
}
```

**Пример ответа (201 Created)**

```json
{
"transactionId": 1,
"type": "EXPENSE",
"amount": 1500.00,
"category": "Супермаркеты",
"date": "2026-05-24",
"description": "Покупка продуктов",
"accountId": 1,
"accountName": "Зарплатная карта",
"accountBalance": 98500.00
}
```

### Получение сводного отчета за месяц (/api/v1/reports/monthly)

* Метод: `GET`
* Заголовок `Authorization: Bearer <token>` обязателен для получения своих счетов
* Обязательные параметры `year` (от 2000 до 2100), `month`(от 1 до 12).  


* Пример запроса: `GET /api/v1/reports/monthly?year=2026&month=6`
* Пример ответа (200 OK):

```JSON
{
  "totalIncome": 200000,
  "totalExpense": 45000,
  "expenseByCategory": [
    {
      "category": "Супермаркеты",
      "totalExpense": 25000.0
    },
    {
      "category": "Транспорт",
      "totalExpense": 5000.0
    },
    {
      "category": "ЖКХ",
      "totalExpense": 15000.0
    }
  ]
}
```

### Централизованная валидация транзакций

- Создан кастомный валидатор `@ValidTransaction` для централизованной проверки входных данных.
- Проверки выполняются до записи в БД через `@Valid` в контроллере.
- Добавлена проверка валюты (совпадение с валютой счёта).

**Примеры ответов при ошибках**

400 Bad Request — сумма меньше или равна нулю
```json
{
    "timestamp": "2026-06-01T10:00:00",
    "status": 400,
    "error": "Validation Failed",
    "message": "Amount must be positive",
    "path": "/api/v1/transactions"
}

```

400 Bad Request — дата в будущем
```json
{
"timestamp": "2026-06-01T10:00:00",
"status": 400,
"error": "Validation Failed",
"message": "The date cannot be a future date",
"path": "/api/v1/transactions"
}
```
400 Bad Request — несовпадение валют
```json
{
"timestamp": "2026-06-01T10:00:00",
"status": 400,
"error": "Validation Failed",
"message": "The transaction must have the same currency as the account with ID = 1",
"path": "/api/v1/transactions"
}
```