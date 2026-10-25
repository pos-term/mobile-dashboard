# Transaction history prompt

- Date: 2026-10-25
- Branch: `feature/transaction-history`
- Scope: mock-backed transaction history screen, filters, and unit tests

## Source request

> Реализуй первый вертикальный срез Android-приложения: экран «История транзакций» на мок-данных без обращения к backend. UI не должен зависеть от конкретного источника данных. Добавь состояния загрузки, контента, пустого списка и ошибки, pull-to-refresh, курсорную пагинацию, повтор запроса, стандартные фильтры, unit-тесты ViewModel и обнови документацию. Делай маленькими шагами с подробными объяснениями.

## Implementation decisions

- Added a `TransactionRepository` boundary with mock and Retrofit-backed implementations so the UI does not know which data source is active.
- Centralized source selection in `TransactionRepositoryProvider`; the current build uses the mock with a 900 ms artificial delay and an optional forced-error flag.
- Stored mock responses as JSON assets and parsed them with Gson into the same `Transaction` and `TransactionPage` models used by the REST client.
- Applied mock filtering before pagination to match server semantics and generated opaque offset cursors for subsequent pages.
- Added standard filters already defined by `infra/api/openapi.yaml`: `terminal_id`, `status`, `from`, and `to`. No `infra` contract changes were required.
- Implemented `TransactionListViewModel` with loading, content, empty, and error states, pull-to-refresh, retry, pagination, stale-request protection, and persistent filter selection.
- Built the screen with Material 3 XML Views, `RecyclerView`, `SwipeRefreshLayout`, status chips, a filter dialog, and Russian/English resources.
- Formatted minor-unit amounts with `Currency.getDefaultFractionDigits()` so currencies with zero, two, or three fraction digits are handled correctly.
- Kept dates in the API as UTC RFC 3339 values; the date-range filter uses an inclusive `from` boundary and an exclusive `to` boundary.

## Verification

- `gradlew.bat assembleDebug` — successful.
- Existing API-client tests and all transaction history, ViewModel, and filter unit tests — successful from an ASCII-only temporary checkout.
- `git diff --check` — successful before each commit series.
- The debug APK installed and the transaction history screen rendered on a Medium Phone API 37 emulator.
- The course workspace path contains Cyrillic characters, so the Gradle test worker cannot reliably load test classes directly from it on Windows; use an ASCII-only checkout path for local unit-test runs.
