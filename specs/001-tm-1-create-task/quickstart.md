# Quickstart: TM-1 Create Task

## Prerequisites

- JDK 17
- Maven 3.9+

## Run the application

```bash
cd /path/to/spring-boot-sdd-poc
./mvnw -q spring-boot:run
```

(Default port **8080** unless overridden in `application.yml`.)

## Create a task (happy path)

```bash
curl -s -X POST http://localhost:8080/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Buy milk","description":"2% organic"}' | jq .
```

Expect **HTTP 201** and a body including `id`, `title`, `description`, `status` (`PENDING`), and `createdAt`.

## Validation failure

```bash
curl -s -w '\nHTTP %{http_code}\n' -X POST http://localhost:8080/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":""}'
```

Expect **HTTP 400** and structured `errors` (field-level messages).

## Tests (TDD)

```bash
./mvnw -q test
```

During implementation, add **failing** tests first:

- **TaskServiceTest** — create succeeds with/without description; trim/blank-title behavior.
- **TaskControllerTest** — `MockMvc` POST `/v1/tasks` returns **201** and **400** shapes per [contracts/openapi.yaml](./contracts/openapi.yaml).

## Contract reference

- OpenAPI: [contracts/openapi.yaml](./contracts/openapi.yaml)
