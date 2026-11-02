# Idempotency Key Service

A Java-based REST API service that prevents duplicate request processing using idempotency keys.

## Overview

In applications such as payment systems, clients may accidentally send the same request multiple times due to network failures, retries, or repeated button clicks.

This service ensures that repeated requests with the same idempotency key do not generate duplicate processing results.

## Features

- Idempotency key-based request handling
- SHA-256 request body hashing
- Duplicate request detection
- HTTP 422 for reuse of a key with a different request body
- MySQL persistence using JDBC
- 24-hour key expiration
- Environment-based database configuration
- Basic concurrent request handling using a unique database constraint

## Technology Stack

- Java 21
- MySQL 8
- JDBC
- Java HTTP Server
- SHA-256
- HTML, CSS and jQuery

## Project Structure

```text
idempotency-service/
├── src/
│   ├── DB.java
│   ├── Hasher.java
│   ├── IdempotencyService.java
│   ├── IdempotencyStore.java
│   ├── MySqlIdempotencyStore.java
│   ├── Server.java
│   └── index.html
├── database/
│   └── schema.sql
├── lib/
├── .gitignore
└── README.md
```

## Database Setup

1. Install MySQL 8.
2. Execute `database/schema.sql` in MySQL Workbench.
3. Ensure the MySQL server is running on port `3307`, or configure your database URL accordingly.

## Configuration

Configure these environment variables before running the application:

| Variable | Description |
|---|---|
| DB_URL | MySQL JDBC connection URL |
| DB_USER | MySQL username |
| DB_PASSWORD | MySQL password |

Example:

```text
DB_URL=jdbc:mysql://localhost:3307/idempotency
DB_USER=root
DB_PASSWORD=your_password
```

Never commit actual database credentials.

## Running the Application

1. Add the MySQL Connector/J JAR to the project classpath.
2. Configure the database environment variables.
3. Run `Server.java`.
4. Open `index.html` in your browser.

The API runs at:

`http://localhost:8080/api/process`

## API Usage

**Endpoint:** `POST /api/process`

**Headers:**

```text
Content-Type: application/json
Idempotency-Key: order-001
```

**Request:**

```json
{
  "amount": 500
}
```

**Successful response:**

```json
{
  "id": "generated-uuid",
  "status": "SUCCESS"
}
```

## HTTP Responses

| Status | Meaning |
|---|---|
| 200 | Request processed or previous response returned |
| 400 | Missing idempotency key |
| 405 | Unsupported HTTP method |
| 422 | Same key reused with a different request body |
| 500 | Database or server error |

## Expiration

Idempotency records expire after 24 hours. Once an existing record expires, the same key can be used for a new request.

## Testing Performed

- New request processing
- Duplicate request with the same key
- Same key with a different request body
- Missing idempotency key
- Unsupported HTTP method
- Expired key reuse
- Repeated request after key reuse
- Basic simultaneous requests using the same key

## Limitations

This is an educational portfolio project, not a production payment-processing system. Atomic concurrency handling for expired-key replacement and production-grade transaction management require further improvements.

## Author

Devang Solanki