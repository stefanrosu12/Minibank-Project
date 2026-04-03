# MiniBank API 🏦

A robust, enterprise-grade core banking REST API built from scratch. This project handles account management, SEPA-compliant transfers, real-time cross-currency conversions, and double-entry transaction ledgers while strictly enforcing complex business and concurrency rules.

---

## 🚀 Key Features & Engineering Highlights

- **Double-Entry Ledger:** Automatically records accurate `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, and `TRANSFER_OUT` transactions based on the flow of funds between user accounts and the internal System Bank.
- **Concurrency Safety:** Utilizes **Pessimistic Database Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) to prevent race conditions and lost updates during simultaneous transfers involving the same account.
- **Idempotency:** Protected against duplicate network requests via unique idempotency keys for secure transfer retries.
- **Cross-Currency Math:** Supports RON, EUR, USD, and GBP. Uses strict `BigDecimal` operations with `HALF_EVEN` (banker's rounding) for precision-safe currency exchange and balance updates.
- **Interactive Web Tester:** Includes a built-in, lightweight HTML/JS frontend to visually test API endpoints, trigger validations, and simulate transfers without needing Postman.
- **Advanced Business Rules:**
  - Enforces a dynamic **5,000 EUR equivalent daily limit** on outgoing transfers for `SAVINGS` accounts, automatically calculating daily aggregates across cross-currency transactions.
  - Real-world mathematical **IBAN validation** (MOD-97 checksum) and SEPA-member country isolation.
  - Strict overdraft protection — balances can never fall below zero.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.3 (Spring MVC, Spring Data JPA) |
| Database | H2 In-Memory Database |
| Build Tool | Maven |
| Validation | Hibernate Validator, Apache Commons Validator |
| Boilerplate Reduction | Lombok |

---

## ⚙️ Getting Started

### Prerequisites

- Java 25 installed
- Maven 3.9+ installed

### Running the Application

Clone the repository and run the application using Maven:

```bash
git clone [https://github.com/stefanrosu12/Minibank-Project.git](https://github.com/stefanrosu12/Minibank-Project.git)
cd Minibank-Project/minibank-project
mvn spring-boot:run

The application will start on `http://localhost:8080`.

---

## 🧪 Interactive Web Tester & Database Console

This project includes a built-in visual tester to easily evaluate the API's functionality and business rules.

**Open the Web Tester:** Once the app is running, open your browser and navigate to:
👉 `http://localhost:8080/tester.html`

**H2 Database Console:** View the live database tables and double-entry ledger at:
👉 `http://localhost:8080/h2-console`

> JDBC URL: `jdbc:h2:mem:minibank` | Username: `sa` | Password: *(leave blank)*

---

## 🛡️ Edge Cases Handled

The API is thoroughly protected against invalid or malicious requests, returning appropriate `400 Bad Request`, `404 Not Found`, or `409 Conflict` statuses for:

| Scenario | Behaviour |
|---|---|
| Invalid IBANs | Rejects IBANs that fail international mathematical checksums |
| Non-SEPA Transfers | Blocks transfers involving countries outside the SEPA zone |
| Duplicate Accounts | Prevents registering an IBAN that is already in use |
| Negative/Zero Transfers | Prevents malicious attempts to drain funds using negative numbers |
| Insufficient Funds | Blocks overdrafts on user accounts |
| Idempotency Collisions | Safely ignores duplicate transfer requests carrying the same `idempotencyKey` |

---

## 📡 API Reference

### Accounts

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/accounts` | Open a new bank account |
| `GET` | `/api/accounts/{accountId}` | Retrieve a specific account |
| `GET` | `/api/accounts` | List all accounts *(supports pagination)* |
| `GET` | `/api/accounts/{accountId}/transactions` | View the chronological double-entry ledger for an account |

### Transfers

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/transfers` | Execute a transfer between two SEPA accounts |
| `GET` | `/api/transfers/{transferId}` | Retrieve transfer details and exchange rates used |
| `GET` | `/api/transfers` | List transfers *(supports filtering by IBAN and date ranges)* |

### System

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/exchange-rates` | View the active currency exchange rates used for cross-currency math |
| `GET` | `/actuator/health` | Check application health status |
