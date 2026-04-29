# 📦 sm4rt-stock

> REST API de gestión de inventario desarrollada con Spring Boot, MySQL y JWT.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![JWT](https://img.shields.io/badge/JWT-Auth-black?style=flat-square&logo=jsonwebtokens)
![Maven](https://img.shields.io/badge/Maven-3.9-red?style=flat-square&logo=apachemaven)

---

## 📋 Descripción

**sm4rt-stock** es una API REST para gestión de inventario de productos. Permite crear, consultar, actualizar y eliminar productos y categorías, con autenticación JWT stateless y base de datos MySQL en la nube (Aiven).

Proyecto desarrollado como parte de mi portafolio profesional para demostrar habilidades en desarrollo backend con Java y Spring Boot.

---

## 🚀 Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.x | Framework backend |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Persistencia de datos |
| Hibernate | 6.x | ORM |
| MySQL (Aiven) | 8.0 | Base de datos en la nube |
| JWT (jjwt) | 0.11.5 | Tokens de autenticación |
| Lombok | 1.18 | Reducción de código boilerplate |
| Maven | 3.9 | Gestión de dependencias |

---

## 🏗️ Arquitectura

El proyecto sigue una arquitectura en capas:

```
sm4rt-stock/
├── src/main/java/com/malcom/sm4rtstock/
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── ProductoController.java
│   │   └── CategoriaController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserDetailsServiceImpl.java
│   │   ├── ProductoService.java
│   │   └── CategoriaService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ProductoRepository.java
│   │   └── CategoriaRepository.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Producto.java
│   │   └── Categoria.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthFilter.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── ConflictException.java
│   └── SecurityConfig.java
└── src/main/resources/
    └── application.properties
```

---

## ⚙️ Instalación y configuración

### Prerrequisitos

- Java 17 o superior
- Maven 3.9+
- MySQL 8.0+ (local o en la nube)

### 1. Clonar el repositorio

```bash
git clone https://github.com/NerokxMal/sm4rt-stock.git
cd sm4rt-stock
```

### 2. Configurar variables de entorno

```bash
cp .env.example .env
```

Edita `.env` con tus credenciales:

```env
DB_URL=jdbc:mysql://TU_HOST:TU_PUERTO/TU_DB?ssl-mode=REQUIRED
DB_USER=TU_USUARIO
DB_PASSWORD=TU_PASSWORD
JWT_SECRET=TU_SECRETO_DE_AL_MENOS_64_CARACTERES
JWT_EXPIRATION=86400000
```

> ⚠️ El `JWT_SECRET` necesita **mínimo 64 caracteres** para el algoritmo HS512.
> Genera uno seguro con: `openssl rand -base64 64`

### 3. Ejecutar

```bash
./mvnw spring-boot:run
```

API disponible en `http://localhost:8080`

---

## 📡 Endpoints

### 🔓 Públicos (sin autenticación)

#### Autenticación

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/register` | Registrar nuevo usuario |
| `POST` | `/auth/login` | Iniciar sesión → devuelve JWT |

#### Productos (solo lectura)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/productos` | Listar todos |
| `GET` | `/productos/{id}` | Obtener por ID |
| `GET` | `/productos/buscar?nombre=X` | Buscar por nombre |
| `GET` | `/productos/categoria/{nombre}` | Filtrar por categoría |
| `GET` | `/productos/stock-bajo?limite=X` | Stock crítico |

#### Categorías (solo lectura)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/categorias` | Listar todas |
| `GET` | `/categorias/{id}` | Obtener por ID |

### 🔐 Protegidos — `Authorization: Bearer <token>`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/productos` | Crear producto |
| `PUT` | `/productos/{id}` | Actualizar producto |
| `DELETE` | `/productos/{id}` | Eliminar producto |
| `POST` | `/categorias` | Crear categoría |
| `PUT` | `/categorias/{id}` | Actualizar categoría |
| `DELETE` | `/categorias/{id}` | Eliminar categoría |

---

## 📝 Ejemplos de uso

### Registro / Login

```http
POST /auth/login
Content-Type: application/json

{ "username": "malcom", "password": "miPassword123" }
```

```json
{ "token": "eyJhbGciOiJIUzUxMiJ9..." }
```

### Crear producto (con token)

```http
POST /productos
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

{
  "nombre": "Laptop Lenovo",
  "descripcion": "Laptop para desarrollo",
  "precio": 2500000.00,
  "stock": 10,
  "categoria": { "id": 1 }
}
```

---

## 🔒 Seguridad

- **JWT HS512** — tokens stateless con expiración configurable (por defecto 24h)
- **BCrypt (coste 10)** — contraseñas hasheadas, nunca en texto plano
- **Spring Security** — control de acceso por ruta y método HTTP
- **Códigos HTTP semánticos** — 401, 404, 409 según el tipo de error real
- **Variables de entorno** — credenciales fuera del código fuente
- **CSRF desactivado** — apropiado para APIs REST stateless

---

## 🗄️ Modelo de datos

```
User
─────────────────────
id          BIGINT PK
username    VARCHAR UNIQUE NOT NULL
password    VARCHAR NOT NULL  ← hash BCrypt

Categoria
─────────────────────
id          BIGINT PK
nombre      VARCHAR UNIQUE NOT NULL
descripcion VARCHAR(500)

Producto
─────────────────────
id           BIGINT PK
nombre       VARCHAR NOT NULL
descripcion  VARCHAR(500)
precio       DOUBLE NOT NULL (> 0)
stock        INT NOT NULL    (≥ 0)
categoria_id BIGINT FK → Categoria (nullable)
```

> Hibernate gestiona el esquema con `ddl-auto=update`.

---

## 🌐 Frontend

🔗 [sm4rt-stock-frontend](https://github.com/NerokxMal/sm4rt-stock-frontend) — Astro + TypeScript

---

## 👨‍💻 Autor

**Malcom García**
- LinkedIn: [malcom-nk-garcia](https://www.linkedin.com/in/malcom-nk-garcia)
- GitHub: [NerokxMal](https://github.com/NerokxMal)

---

## 📄 Licencia

MIT