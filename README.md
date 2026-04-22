# bird-care-store-api

API REST para una tienda de productos de cuidado de aves. Gestiona usuarios, categorías, productos y órdenes con autenticación JWT y roles ADMIN / CUSTOMER.

## Stack

- **Spring Boot 4.0.2** (Web MVC, Data JPA, Security, Validation)
- **Java 25** (Temurin)
- **H2** en memoria (embebida)
- **JWT** (jjwt 0.13)
- **MapStruct** + **Lombok**
- **JUnit 5** + **Mockito** + **AssertJ** (122 tests)

## Requisitos

Necesitas **una** de las dos opciones:

- **Opción recomendada:** Docker + Docker Compose. Nada más.
- **Sin Docker:** JDK 25 + Maven wrapper (incluido en el repo). Solo si quieres correr la app directo en tu máquina.

## Quick start con Docker

```bash
cp .env.example .env
# edita .env y pon un JWT_SECRET real:
#   openssl rand -base64 64
docker compose up --build
```

API en `http://localhost:8080`. H2 Console en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:birdcare_db`, user `sa`, pass según `.env`).

Para parar: `Ctrl+C` (o `docker compose down` si corre en background).

## Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| `DATASOURCE_URL` | URL JDBC | `jdbc:h2:mem:birdcare_db` |
| `DATASOURCE_USERNAME` | Usuario BD | `sa` |
| `DATASOURCE_PASSWORD` | Password BD | *(vacío)* |
| `JWT_SECRET` | Secreto HMAC para firmar JWTs (base64, ≥ 256 bits) | **requerido** |
| `JWT_EXPIRATION` | Expiración del token en ms | `3600000` (1h) |
| `HIBERNATE_DDL` | Modo DDL de Hibernate | `update` |
| `H2_CONSOLE_ENABLED` | Habilita consola H2 | `true` |

## Desarrollo local (sin Docker)

```bash
./mvnw spring-boot:run
```

Necesitas tener las env vars configuradas. Opciones:

- Exportar en tu shell (`export JWT_SECRET=...`).
- Crear `src/main/resources/variables.properties` (gitignored) con los mismos valores que `.env`.
- Usar el IDE (Run Configuration → Environment variables).

## Datos semilla

`src/main/resources/data.sql` crea al arrancar:

- 3 categorías, 6 productos.
- 1 usuario admin: `admin@birdcare.com` (password hasheado con BCrypt).

Como H2 es in-memory, los datos se reinician en cada arranque.

## Seguridad

- Passwords hasheados con **BCrypt** (work factor 10).
- JWT en header `Authorization: Bearer <token>`.
- Roles: `ADMIN` y `CUSTOMER` (ver `enums/Role.java`).
- Endpoints públicos: `/api/auth/**`, `GET /api/products/**`, `GET /api/categories/**`.
- Endpoints ADMIN: `/api/admin/**` + escrituras de productos/categorías.
- Resto requiere autenticación.

## Endpoints principales

### Auth — público
- `POST /api/auth/register` — registrar cliente
- `POST /api/auth/login` — login, devuelve JWT

### Users
- `GET /api/users/me` — mi perfil
- `PUT /api/users/me` — actualizar datos
- `PATCH /api/users/me/password` — cambiar password
- `GET|POST /api/admin/users` — listar / crear (ADMIN)
- `DELETE /api/admin/users/{id}` — deshabilitar (ADMIN)
- `PATCH /api/admin/users/{id}/enable` — reactivar (ADMIN)
- `PATCH /api/admin/users/{id}/password` — reset de password (ADMIN)

### Categories / Products
- `GET /api/categories`, `/api/products` — listados (público)
- `POST|PUT /api/categories`, `/api/products` (ADMIN)
- `PATCH /api/{products|categories}/{id}/enable` (ADMIN)
- `DELETE /api/{products|categories}/{id}` — soft-delete (ADMIN)

### Orders
- `GET /api/orders/me` — mis órdenes
- `POST /api/orders` — crear orden (descuenta stock)
- `GET /api/admin/orders/{id}` (ADMIN)
- `PUT /api/admin/orders/{id}/status?status=...` — cambiar estado (ADMIN)
  - `CANCELLED` repone el stock; no se puede cancelar una orden `DELIVERED`.

## Ejemplo de uso

```bash
# Register + login
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@test.com","password":"Secret123","name":"Ana","lastName":"Perez","phone":"999"}'

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@test.com","password":"Secret123"}' | jq -r .token)

# Crear orden
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"Av. Siempre Viva 742","items":[{"productId":3,"quantity":2}]}'
```

## Tests

```bash
./mvnw test
```

Cobertura actual: entidades (`User`, `Category`, `Product`, `Order`, `OrderItem`) y services (`UserServiceImpl`, `CategoryServiceImpl`, `ProductServiceImpl`, `OrderServiceImpl`). 122 tests en total.

## Estructura del código

```
src/main/java/pe/com/birdcare/
├── controller/   → endpoints REST
├── service/      → lógica de aplicación (interfaces + impl)
├── entity/       → modelo de dominio (factories, métodos de negocio)
├── dto/          → records para request/response
├── mapper/       → MapStruct (entity → DTO)
├── repository/   → JPA repositories
├── security/     → JWT + filters + Spring Security config
├── exception/    → excepciones + handler global
└── enums/        → Role, OrderStatus
```

Los entities siguen un patrón tipo DDD: constructores privados, factories estáticas (`User.registerCustomer`, `Product.create`), y métodos de dominio (`user.changePassword(...)`, `product.decreaseStock(...)`) en lugar de setters expuestos.

## Códigos de error esperados

| HTTP | Cuándo |
|---|---|
| 400 | Validaciones de request o `IllegalArgumentException` del dominio |
| 401 | Credenciales inválidas |
| 403 | Rol insuficiente |
| 404 | Recurso no encontrado |
| 409 | Conflicto: email duplicado, stock insuficiente, nombre de categoría existente |
