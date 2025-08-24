# JBank - Microservices Banking System

**JBank** — это *учебная* микросервисная банковская система, построенная на базе **Spring Boot** и **Jakarta EE**.  
Система включает сервисы для управления счетами, переводами, обменом валют и другими банковскими операциями.

---

## Архитектура

### Инфраструктурные сервисы
- **Consul** (порт `8500`) — Service Discovery и Configuration Management
- **PostgreSQL** (порт `5432`) — основная база данных
- **Keycloak** (порт `8080`) — аутентификация и авторизация

### Микросервисы приложения
- **Account Service** (порт `9081`) — управление банковскими счетами
- **Blocker Service** (порт `9082`) — блокировка и валидация операций
- **Cash Service** (порт `9083`) — операции с наличными
- **Exchange Service** (порт `9084`) — обмен валют
- **Exchange Generator Service** (порт `9085`) — генерация курсов валют
- **Transfer Service** (порт `9087`) — переводы между счетами
- **Notification Service** (порт `9086`) — система уведомлений
- **Gateway Service** (порт `8090`) — API Gateway
- **Front UI Service** (порт `80`) — Web-интерфейс

---

## Требования
- Docker и Docker Compose
- Java 21+ (для локальной разработки)
- Gradle (для сборки проектов)

---

## Быстрый старт

### Запуск в Docker Compose

1. **Клонируйте репозиторий:**
   ```bash
   git clone <repository-url>
   cd jbank
   ```

2. **Соберите все сервисы:**
   ```bash
   # Linux/Mac
   ./gradlew build

   # Windows
   gradlew.bat build
   ```

3. **Запустите всю систему:**
   ```bash
   # Автоматический запуск скриптом
   ./docker-compose-run-all.ps1

   # Или вручную
   docker-compose up -d --build
   docker-compose run --rm consul-import
   ```

4. **Проверьте состояние сервисов:**
   ```bash
   docker-compose ps
   ```

---

### Доступ к сервисам
После запуска будут доступны:

- **Consul UI**: [http://localhost:8500](http://localhost:8500)
- **Keycloak Admin**: [http://localhost:8080](http://localhost:8080) (логин/пароль: `admin/admin`)
- **API Gateway**: [http://localhost:8090](http://localhost:8090)
- **Web UI**: [http://localhost:80](http://localhost:80)
- **PostgreSQL**: `localhost:5432` (`jbank_user/jbank_password`)

---

## Локальная разработка

Для запуска Java-сервисов локально (вне Docker):

### 1. Запустите инфраструктурные сервисы
```bash
# Запуск только инфраструктуры
docker-compose up -d consul postgres keycloak

# Логи Consul
docker-compose logs -f consul
```

### 2. Импортируйте конфигурацию в Consul

⚠️ **Важно:** после запуска Consul обязательно импортируйте конфигурации.

**Linux/Mac:**
```bash
./consul/import-local.sh
```

**Windows:**
```powershell
.\consul\import-local.ps1
```

### 3. Запустите Java-сервисы

Настройте переменные окружения:
```bash
export CONSUL_HOST=localhost
export CONSUL_PORT=8500
export POSTGRES_HOST=localhost
export KEYCLOAK_HOST=localhost
```

Запустите сервисы:
```bash
cd account && ./gradlew bootRun
cd blocker && ./gradlew bootRun
# и так далее...
```

---
