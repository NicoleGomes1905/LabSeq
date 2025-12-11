# Labseq Service — Quarkus API + Angular UI

[![Java](https://img.shields.io/badge/Java-17-007396?logo=java)](https://adoptium.net/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.x-4695EB?logo=quarkus&logoColor=white)](https://quarkus.io/)
[![Build](https://img.shields.io/badge/Build-Maven-88171A?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Angular](https://img.shields.io/badge/Angular-16%2B-DD0031?logo=angular&logoColor=white)](https://angular.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](#license)

Quarkus REST API that computes **L(n)** (labseq) with **memoization of intermediate computations**, plus **OpenAPI/Swagger**, **Docker** containerization, and an optional **Angular UI** to invoke the service.

> **Sequence definition**  
> l(0)=0, l(1)=1, l(2)=0, l(3)=1, and for n>3: **l(n)=l(n-4)+l(n-3)**.

---

## Table of Contents

- [Quickstart — How to Run](#quickstart--how-to-run)
- [Architecture & Features](#architecture--features)
- [Stack & Requirements](#stack--requirements)
- [Project Structure](#project-structure)
- [API / OpenAPI / Swagger](#api--openapi--swagger)
  - [Endpoint](#endpoint)
  - [Examples](#examples)
  - [OpenAPI YAML](#openapi-yaml)
- [Algorithm, Performance & Assumptions](#algorithm-performance--assumptions)
- [Troubleshooting (Windows / Angular / Maven)](#troubleshooting-windows--angular--maven)


---

## Quickstart — How to Run

### 0) Pre-requisites

- **Java 17**, **Maven 3.9+**
- **Node 18+ / 20+** (only for the Angular UI)
- **Docker** (optional)

### 1) Run the Backend (Quarkus)

**Option A - Dev mode (hot reload)**
```bash
mvn quarkus:dev
# API:     http://localhost:8080
# Swagger: http://localhost:8080/q/swagger-ui
```

**Option B - Production run**
```bash
# Build (fast-jar)
mvn -DskipTests package

# Run
java -jar target/quarkus-app/quarkus-run.jar
# (optional) change port
# java -Dquarkus.http.port=8081 -jar target/quarkus-app/quarkus-run.jar
```

### 2) Run the Angular (optional)

In another terminal:
```bash
cd labseq-ui
npm install     # or: npm ci
npm start       # http://localhost:4200
```
The proxies requests from `http://localhost:4200/labseq/{n}` to the backend at `http://localhost:8080/labseq/{n}` (see `labseq-ui/proxy.conf.json`).

### 3) Test

**cURL**
```bash
curl http://localhost:8080/labseq/0
curl http://localhost:8080/labseq/10
```

**PowerShell**
```powershell
Invoke-RestMethod http://localhost:8080/labseq/10
```

---

## Architecture & Features

- **Quarkus REST**: `GET /labseq/{n}` returns the sequence value at index `n`.
- **Memoization of subproblems**: intermediate results are cached and reused (not just endpoint-level caching).
- **OpenAPI**: `/q/openapi` (JSON) and `/q/swagger-ui` (interactive docs).
- **Optional Angular UI**: a simple page with an input for `n` that calls the backend via proxy.
- **Container-ready**: multi-stage Dockerfile for building a small runnable image.

---

## Stack & Requirements

- **Java 17**
- **Quarkus 3.x** (REST, Jackson, SmallRye OpenAPI)
- **Maven 3.9+**
- **Node 18+ / 20+** (only if you run the Angular UI)
- **Docker** (optional, for container build/run)

---

## Project Structure

```text
labseq-service/
├─ src/
│  ├─ main/
│  │  ├─ java/com/nicole/labseq/
│  │  │  ├─ api/               # REST resource (/labseq/{n})
│  │  │  └─ domain/            # Business service (calculation + cache)
│  │  └─ resources/
│  │     ├─ META-INF/openapi.yaml
│  │     └─ application.properties
├─ labseq-ui/                   # Optional Angular UI
│  ├─ src/app/                  # Component with input 'n' + call to backend
│  ├─ proxy.conf.json           # Proxies /labseq/* -> http://localhost:8080
│  └─ ...
├─ pom.xml
├─ Dockerfile
└─ README.md
```

---

## API / OpenAPI / Swagger

### Endpoint

```
GET /labseq/{n}
```

**Path parameter**

- `n` (required): non-negative integer (0, 1, 2, …)

**200 OK**
```json
{
  "index": 10,
  "value": "3"
}
```
`value` is a **string** to safely represent large integers (`BigInteger` in the backend).

**Error responses**
- `400 Bad Request` → `n < 0` or not a valid non-negative integer  
- `404 Not Found` → invalid route

### Examples

**cURL**
```bash
curl http://localhost:8080/labseq/0
curl http://localhost:8080/labseq/1
curl http://localhost:8080/labseq/2
curl http://localhost:8080/labseq/3
curl http://localhost:8080/labseq/10
```

**PowerShell**
```powershell
Invoke-RestMethod http://localhost:8080/labseq/10
```

### OpenAPI YAML

Place the file below at `src/main/resources/META-INF/openapi.yaml`:

```yaml
openapi: 3.0.3
info:
  title: Labseq API
  version: "1.0.0"
  description: |
    REST API that calculates the labseq sequence value L(n).
    l(0)=0, l(1)=1, l(2)=0, l(3)=1, and for n>3: l(n)=l(n-4)+l(n-3).
servers:
  - url: http://localhost:8080
paths:
  /labseq/{n}:
    get:
      summary: Calculate L(n)
      description: Returns the labseq value for the provided non-negative index n.
      parameters:
        - name: n
          in: path
          required: true
          schema:
            type: integer
            minimum: 0
      responses:
        "200":
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  index:
                    type: integer
                    example: 10
                  value:
                    type: string
                    description: Big integer result serialized as string
                    example: "3"
        "400":
          description: Invalid index (negative or non-integer)
        "404":
          description: Not Found
```

- Swagger UI: **http://localhost:8080/q/swagger-ui**  
- OpenAPI JSON: **http://localhost:8080/q/openapi**

---

## Algorithm, Performance & Assumptions

**Algorithm**
- Bases: l(0)=0, l(1)=1, l(2)=0, l(3)=1  
- Recurrence: l(n)=l(n-4)+l(n-3) for n>3  
- Implemented **iteratively** with **memoization** (in-memory cache, e.g., `ConcurrentHashMap<Long, BigInteger>`).  
- For large `n`, the service continues from the highest computed index, amortizing the cost across calls.

**Complexity**
- Time: **O(n)** for the first computation up to a new `n`; subsequent calls ≤ that `n` are **O(1)** due to caching.  
- Space: cache grows with the largest computed index (intentional trade-off for performance).

**Assumptions**
- The result `value` is serialized as **string** to avoid overflow and preserve very large integers.  
- Input must be a non-negative integer; otherwise **400**.  
- Performance target “**l(100000) under 10s**” is met by the iterative + memoized approach (hardware dependent).

---

## Troubleshooting (Windows / Angular / Maven)

- **UTF-8 BOM in JSON/HTML causes parser errors**  
  Errors such as `Unexpected token '﻿'` (Vite/PostCSS) or `Invalid ICU message` usually mean the file has a **BOM** (e.g., `proxy.conf.json`, `package.json`, `.html`).  
  **Fix**: in VS Code, “Save with encoding” → **UTF-8 (without BOM)**.

- **Render literal braces in Angular**  
  Use `{{ '{' }}` and `{{ '}' }}` to display `{` and `}` in templates.

- **“No plugin found for prefix 'quarkus'”**  
  Ensure `pom.xml` is valid (and BOM-free), then run `mvn -U clean package`.

- **Proxy / CORS**  
  Backend must be on `http://localhost:8080` and `proxy.conf.json` should point there to avoid CORS.

