# 📦 sm4rt-stock

> REST API de gestión de inventario desarrollada con Spring Boot, MySQL y JWT.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![JWT](https://img.shields.io/badge/JWT-Auth-black?style=flat-square&logo=jsonwebtokens)
![Maven](https://img.shields.io/badge/Maven-3.9-red?style=flat-square&logo=apachemaven)

---

## 📋 Descripción

**sm4rt-stock** es una API REST fullstack para gestión de inventario de productos. Permite crear, consultar, actualizar y eliminar productos y categorías, con autenticación JWT y base de datos MySQL en la nube (Aiven).

Este proyecto fue desarrollado como parte de mi portafolio profesional para demostrar habilidades en desarrollo backend con Java y Spring Boot.

---

## 🚀 Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.5.13 | Framework backend |
| Spring Security | 6.5 | Autenticación y autorización |
| Spring Data JPA | 3.5 | Persistencia de datos |
| Hibernate | 6.6 | ORM |
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
│   ├── controller/          # Endpoints REST
│   │   ├── ProductoController.java
│   │   └── CategoriaController.java
│   ├── service/             # Lógica de negocio
│   │   ├── ProductoService.java
│   │   └── CategoriaService.java
│   ├── repository/          # Acceso a datos (JPA)
│   │   ├── ProductoRepository.java
│   │   └── CategoriaRepository.java
│   ├── model/               # Entidades JPA
│   │   ├── Producto.java
│   │   └── Categoria.java
│   ├── security/            # JWT y Spring Security
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthFilter.java
│   ├── exception/           # Manejo global de errores
│   │   └── GlobalExceptionHandler.java
│   └── SecurityConfig.java  # Configuración de seguridad
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

Copia el archivo de ejemplo y completa con tus datos:

```bash
cp .env.example .env
```

Edita el `.env` con tus credenciales:

```env
DB_URL=jdbc:mysql://TU_HOST:TU_PUERTO/TU_DB?ssl-mode=REQUIRED
DB_USER=TU_USUARIO
DB_PASSWORD=TU_PASSWORD
JWT_SECRET=TU_SECRETO_JWT_MUY_LARGO_Y_SEGURO
JWT_EXPIRATION=86400000
```

### 3. Ejecutar el proyecto

```bash
./mvnw spring-boot:run
```

O desde IntelliJ IDEA: **Run → Sm4rtStockApplication**

La API estará disponible en: `http://localhost:8080`

---

## 📡 Endpoints

### 🔓 Públicos (sin autenticación)

#### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/productos` | Listar todos los productos |
| `GET` | `/productos/{id}` | Obtener producto por ID |
| `GET` | `/productos/buscar?nombre=X` | Buscar por nombre |
| `GET` | `/productos/categoria/{nombre}` | Filtrar por categoría |
| `GET` | `/productos/stock-bajo?limite=X` | Productos con stock bajo |

#### Categorías

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/categorias` | Listar todas las categorías |
| `GET` | `/categorias/{id}` | Obtener categoría por ID |

### 🔐 Protegidos (requieren JWT)

#### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/productos` | Crear producto |
| `PUT` | `/productos/{id}` | Actualizar producto |
| `DELETE` | `/productos/{id}` | Eliminar producto |

#### Categorías

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/categorias` | Crear categoría |
| `PUT` | `/categorias/{id}` | Actualizar categoría |
| `DELETE` | `/categorias/{id}` | Eliminar categoría |

---

## 📝 Ejemplos de uso

### Crear un producto

```http
POST /productos
Content-Type: application/json
Authorization: Bearer {token}

{
  "nombre": "Laptop Lenovo",
  "descripcion": "Laptop para desarrollo",
  "precio": 2500000.00,
  "stock": 10,
  "categoria": {
    "id": 1
  }
}
```

**Respuesta exitosa (201 Created):**

```json
{
  "id": 1,
  "nombre": "Laptop Lenovo",
  "descripcion": "Laptop para desarrollo",
  "precio": 2500000.0,
  "stock": 10,
  "categoria": {
    "id": 1,
    "nombre": "Electrónica"
  }
}
```

### Crear una categoría

```http
POST /categorias
Content-Type: application/json
Authorization: Bearer {token}

{
  "nombre": "Electrónica",
  "descripcion": "Dispositivos electrónicos"
}
```

---

## 🔒 Seguridad

- **JWT** para autenticación stateless
- **Spring Security** para autorización por roles
- **CORS** configurado con patrones de origen
- **Bean Validation** para validación de datos de entrada
- **Variables de entorno** para protección de credenciales
- **CSRF desactivado** para APIs REST stateless

---

## 🗄️ Modelo de datos

```
Categoria
─────────────────────
id          BIGINT PK
nombre      VARCHAR(255) UNIQUE NOT NULL
descripcion VARCHAR(500)

Producto
─────────────────────
id           BIGINT PK
nombre       VARCHAR(255) NOT NULL
descripcion  VARCHAR(500)
precio       DOUBLE NOT NULL
stock        INT NOT NULL
categoria_id BIGINT FK → Categoria
```

---

## 🌐 Frontend

Este backend tiene un frontend complementario desarrollado en HTML, CSS y JavaScript vanilla:

🔗 [sm4rt-stock-frontend](https://github.com/NerokxMal/sm4rt-stock-frontend)

---

## 👨‍💻 Autor

**Malcom García**
- LinkedIn: [malcom-nk-garcia](https://www.linkedin.com/in/malcom-nk-garcia)
- GitHub: [NerokxMal](https://github.com/NerokxMal)

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT.
