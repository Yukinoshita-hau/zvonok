# Zvonok

## 🇷🇺 Обзор

Zvonok — серверная часть голосового и текстового мессенджера, ориентированного на работу с серверами, каналами и системой друзей. Приложение построено на Spring Boot 3 и предоставляет REST API, а также WebSocket-коннекты для обмена сообщениями в режиме реального времени.

### Основные возможности
- Регистрация, аутентификация и обновление токенов по схеме access + refresh.
- Управление серверами: создание, редактирование, приглашения, выход и удаление.
- Работа с ролями, правами и разрешениями для серверов, каналов и папок.
- Текстовые сообщения, редактирование, удаление и аудит событий.
- Система друзей: запросы, подтверждения, список друзей, блокировки.
- Бан и модерация участников на уровне сервера.
- Поддержка WebSocket-хендшейков с защитой через JWT.
- Автоматически генерируемая документация API через Swagger UI.

### Технологический стек
- Java 21, Maven, Spring Boot 3.5.x.
- Модули Spring: Web, WebSocket, Security, Validation, Data JPA.
- PostgreSQL как основная СУБД, H2 в тестовом профиле.
- JWT (jjwt) для выпуска и валидации токенов.
- Lombok, Springdoc OpenAPI.

### Быстрый запуск
- Убедитесь, что установлены JDK 21+, Maven 3.8+ и PostgreSQL 13+.
- Склонируйте репозиторий и перейдите в его корень: `git clone <repo>`.
- Создайте базу данных `zvonok` и пользователя с паролем, либо скорректируйте параметры подключения.
- Обновите `src/main/resources/application.properties` значениями для `spring.datasource.*` и `app.jwt.secret`.
- Соберите и запустите приложение: `mvn spring-boot:run`.
- REST API будет доступен по адресу `http://localhost:8080/api`, Swagger UI — `http://localhost:8080/swagger-ui/index.html`.

### Запуск в Docker
- Установите Docker и Docker Compose (v2+).
- Выполните команду `docker compose up --build` из корня проекта.
- Приложение будет доступно по адресу `http://localhost:8080/api`, Swagger UI — `/swagger-ui/index.html`.
- Контейнер PostgreSQL 16 запускается с дефолтными учётными данными (`zvonok` / `zvonok`). Перед продакшеном обновите переменные окружения в `docker-compose.yml` и задайте сложный `APP_JWT_SECRET`.

### Ключевые настройки
- `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` — доступ к PostgreSQL.
- `app.jwt.secret` — секрет для подписи access-токенов.
- `app.jwt.ExpirationMs`, `app.jwt.refreshExpirationMs` — срок жизни access и refresh токенов.
- `server.servlet.context-path=/api` — базовый префикс для REST API.

### Архитектура
- Контроллеры (`src/main/java/com/zvonok/controller`) формируют REST-слой и документируются через OpenAPI.
- Сервисы (`com.zvonok.service`) инкапсулируют бизнес-логику серверов, каналов, друзей, сообщений и прав.
- Репозитории (`com.zvonok.repository`) основаны на Spring Data JPA.
- Сущности домена (`com.zvonok.model`) описывают пользователей, серверы, роли, сообщения, бан-листы и др.
- Безопасность настраивается в `SecurityConfig`, фильтры реализуют JWT-авторизацию и refresh-токены.
- WebSocket-конфигурация включает `JwtHandshakeHandler` и `JwtHandshakeInterceptor`.

### Работа с API
- Авторизация: `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/me`.
- Серверы и модерация: `/server/**`, `/server-role/**`, `/server-member-role/**`, `/server-ban/**`.
- Каналы и папки: `/channel/**`, `/channel-folder/**`, `/room/**`.
- Сообщения: `/message/**`, поддерживаются обновление и удаление.
- Друзья: `/friend/**` для заявок и управления дружбой.
- Общие операции и статусы доступны через `/common/**`.

### Тестирование и качество
- Линтеры и форматирование: используйте стандартные плагины IDE (например, IntelliJ IDEA) и правила Maven.
- Swagger UI позволяет быстро проверить корректность контрактов API.

### Дополнительные замечания
- Приложение генерирует значения invite-кодов и управляет их жизненным циклом.
- При необходимости запустите произвольные миграции БД через Liquibase/Flyway (не входят в проект).
- Для WebSocket клиентов помните о передаче JWT токена в параметрах handshейка.

## 🇬🇧 Overview

Zvonok is the backend for a voice and text messenger that revolves around servers, channels, and a social graph of friends. Built with Spring Boot 3, it exposes a REST API and WebSocket endpoints for near real-time collaboration.

### Highlights
- Registration, authentication, and token refresh using an access + refresh token pair.
- Server management: create, update, invite, join/leave, and delete.
- Granular permissions for servers, roles, channels, and folders.
- Text messaging, edits, deletions, and event broadcasting.
- Friendship workflows: requests, approvals, friend list, blocking.
- Server-level moderation tools including bans and kick actions.
- Secured WebSocket handshake guarded by JWT tokens.
- Auto-generated API documentation via Swagger UI.

### Tech Stack
- Java 21, Maven, Spring Boot 3.5.x.
- Spring modules: Web, WebSocket, Security, Validation, Data JPA.
- PostgreSQL as the primary datastore, H2 for tests.
- JWT (jjwt) for token issuance and validation.
- Lombok and Springdoc OpenAPI for productivity and docs.

### Quick Start
- Install JDK 21+, Maven 3.8+, and PostgreSQL 13+.
- Clone the repository and switch to the project root: `git clone <repo>`.
- Create the `zvonok` database and a dedicated user, or adjust the datasource settings.
- Update `src/main/resources/application.properties` with your database credentials and `app.jwt.secret`.
- Build and run: `mvn spring-boot:run`.
- REST API will be served at `http://localhost:8080/api`; Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

### Docker Launch
- Install Docker and Docker Compose (v2+).
- From the project root run `docker compose up --build`.
- The app is exposed on `http://localhost:8080/api`; Swagger UI lives at `/swagger-ui/index.html`.
- The Compose file provisions PostgreSQL 16 with default credentials (`zvonok` / `zvonok`). Update the env vars in `docker-compose.yml` for production and set a strong `APP_JWT_SECRET`.

### Configuration Essentials
- `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` — PostgreSQL connection.
- `app.jwt.secret` — signing key for access tokens.
- `app.jwt.ExpirationMs`, `app.jwt.refreshExpirationMs` — lifespan of access and refresh tokens.
- `server.servlet.context-path=/api` — base path for the REST API.

### Architecture
- Controllers (`src/main/java/com/zvonok/controller`) expose the REST endpoints documented with OpenAPI.
- Services (`com.zvonok.service`) encapsulate business rules for servers, channels, friends, messaging, and permissions.
- Repositories (`com.zvonok.repository`) rely on Spring Data JPA for persistence.
- Domain models (`com.zvonok.model`) describe users, servers, roles, messages, bans, and more.
- Security is configured in `SecurityConfig`; dedicated filters manage JWT authentication and refresh tokens.
- WebSocket support uses `JwtHandshakeHandler` and `JwtHandshakeInterceptor` to secure connections.

### Working With The API
- Auth: `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/me`.
- Servers & moderation: `/server/**`, `/server-role/**`, `/server-member-role/**`, `/server-ban/**`.
- Channels & folders: `/channel/**`, `/channel-folder/**`, `/room/**`.
- Messages: `/message/**` covering CRUD-like operations.
- Friends: `/friend/**` for requests and managing friendships.
- Shared endpoints live under `/common/**`.

### Testing & Quality
- Use your preferred IDE formatting rules or Maven plugins for linting/formatting.
- Explore Swagger UI to validate request/response contracts.

### Notes
- Invite codes are generated and rotated by the server logic.
- Database migrations (Liquibase/Flyway) can be added if needed; the project ships without them.
- WebSocket clients should pass a valid JWT token during the handshake to gain access.

