# Payment API client prompt

- Date: 2026-10-10
- Branch: `feature/api-client`
- Scope: REST client and unit tests

## Source request

> Создай новую ветку и выполни третий пункт плана: добавь Retrofit/OkHttp, настраиваемые base URL и Bearer-токен, модели транзакций, терминалов и ошибок, запросы истории, одной транзакции и терминалов, а также unit-тесты с MockWebServer. Каждое логическое изменение фиксируй отдельно.

## Implementation decisions

- Kept Retrofit 3.0.0 aligned with OkHttp and MockWebServer 4.12.0, the OkHttp version used by this Retrofit release.
- Mirrored transaction, terminal, pagination, status, and RFC 9457 problem schemas from `infra/api/openapi.yaml`.
- Added runtime configuration for the base URL and Bearer token without committing credentials.
- Redacted `Authorization` from optional HTTP logs.
- Allowed cleartext HTTP only in debug builds so an emulator can access a local backend; release builds retain the secure Android default.
- Left SSE streaming outside this branch because it is a later plan item.

## Verification

- `gradlew.bat :app:compileDebugJavaWithJavac` — successful.
- Debug and release manifest processing — successful.
- Six API-client unit tests, including MockWebServer request/response tests — successful from an ASCII-only temporary worktree.
- The main course workspace contains Cyrillic characters, which causes the Gradle test worker to report `ClassNotFoundException` even though test compilation succeeds; use an ASCII-only checkout path for local unit-test runs on Windows.
