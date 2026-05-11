# 📦 sm4rt-stock (Backend)

> API REST de inventario con Spring Boot + JWT + permisos granulares por usuario.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![JWT](https://img.shields.io/badge/JWT-Auth-black?style=flat-square&logo=jsonwebtokens)
![Maven](https://img.shields.io/badge/Maven-3.9-red?style=flat-square&logo=apachemaven)

---

## 📋 Descripción

**sm4rt-stock** es una API para gestión de inventario enfocada en escenarios multiusuario:

- autenticación JWT stateless,
- permisos granulares por usuario (no solo ADMIN/USER),
- historial de movimientos,
- auditoría de acciones del sistema,
- exportación de datos (CSV/PDF),
- stock crítico por producto,
- categorías jerárquicas,
- búsqueda global agregada.

---

## 🚀 Stack

| Tecnología | Uso |
|---|---|
| Java 17 | Lenguaje principal |
| Spring Boot 3 | API REST |
| Spring Security 6 | AuthN/AuthZ |
| Spring Data JPA + Hibernate | Persistencia |
| MySQL 8 | Base de datos |
| JWT (jjwt 0.11.5) | Tokens |
| Maven Wrapper | Build/Run |

---

## ⚙️ Instalación

### 1) Clonar

```bash
git clone https://github.com/NerokxMal/sm4rt-stock.git
cd sm4rt-stock
```

### 2) Variables de entorno

Crea `.env` desde `.env.example`:

```env
DB_URL=jdbc:mysql://TU_HOST:TU_PUERTO/TU_DB?ssl-mode=REQUIRED
DB_USER=TU_USUARIO
DB_PASSWORD=TU_PASSWORD
JWT_SECRET=TU_SECRETO_DE_AL_MENOS_64_CARACTERES
JWT_EXPIRATION=86400000
```

### 3) Ejecutar

```bash
./mvnw spring-boot:run
```

API disponible en `http://localhost:8080`.

---

## 🔐 Seguridad y permisos

### Roles

- `ADMIN`
- `USER`

### Permisos granulares

- `PRODUCT_VIEW`
- `PRODUCT_CREATE`
- `PRODUCT_STOCK_EDIT`
- `PRODUCT_DELETE`
- `CATEGORY_VIEW`
- `CATEGORY_MANAGE`
- `HISTORY_VIEW`
- `DASHBOARD_VIEW`
- `DATA_EXPORT`

`ADMIN` tiene todos por defecto. `USER` recibe un set base y luego puede ajustarse desde configuración.

### Reglas importantes

- Solo `ADMIN` puede gestionar usuarios/permisos y ver auditoría.
- Cualquier endpoint protegido valida autorización en backend (Postman no evita esto).
- Respuestas típicas:
  - `401` token ausente/inválido
  - `403` token válido sin permisos

---

## 📡 Endpoints principales

### Públicos

| Método | Endpoint |
|---|---|
| `POST` | `/auth/register` |
| `POST` | `/auth/login` |

### Autenticado

| Método | Endpoint |
|---|---|
| `GET` | `/auth/me` |
| `PUT` | `/auth/password` |

### Solo ADMIN

| Método | Endpoint |
|---|---|
| `GET` | `/auth/users` |
| `POST` | `/auth/users` |
| `GET` | `/auth/permissions` |
| `PUT` | `/auth/users/{username}/role` |
| `PUT` | `/auth/users/{username}/status` |
| `PUT` | `/auth/users/{username}/permissions` |
| `GET` | `/auditoria` |

### Productos

| Método | Endpoint | Permiso |
|---|---|---|
| `GET` | `/productos`, `/productos/{id}`, `/productos/buscar`, `/productos/categoria/{nombre}`, `/productos/stock-bajo`, `/productos/stock-critico`, `/productos/stock-critico/count` | `PRODUCT_VIEW` |
| `POST` | `/productos` | `PRODUCT_CREATE` |
| `PUT` | `/productos/{id}` | `PRODUCT_STOCK_EDIT` |
| `PUT` | `/productos/{id}/ajustar-stock` | `PRODUCT_STOCK_EDIT` |
| `DELETE` | `/productos/{id}` | `PRODUCT_DELETE` |
| `GET` | `/productos/export?format=csv|pdf` | `DATA_EXPORT` |

### Categorías

| Método | Endpoint | Permiso |
|---|---|---|
| `GET` | `/categorias`, `/categorias/{id}` | `CATEGORY_VIEW` |
| `POST`/`PUT`/`DELETE` | `/categorias/**` | `CATEGORY_MANAGE` |

### Movimientos / Dashboard / Búsqueda

| Método | Endpoint | Permiso |
|---|---|---|
| `GET` | `/movimientos`, `/movimientos/producto/{id}` | `HISTORY_VIEW` |
| `GET` | `/movimientos/export?format=csv|pdf` | `DATA_EXPORT` |
| `GET` | `/dashboard/stats` | `DASHBOARD_VIEW` |
| `GET` | `/buscar?q=...` | `PRODUCT_VIEW` o `CATEGORY_VIEW` o `HISTORY_VIEW` |

---

## 🧩 Modelo (resumen)

- `users` + `user_permissions` (permisos por usuario)
- `productos` (`umbral_critico`)
- `categorias` (`parent_id` para jerarquía)
- `movimientos` (`tipo`, `motivo`, stock anterior/nuevo)
- `auditoria` (acción, usuario, IP, timestamp, entidad)

`spring.jpa.hibernate.ddl-auto=update` actualiza esquema automáticamente.

---

## ✅ Funcionalidades implementadas

- CRUD de productos y categorías con validaciones
- Ajuste manual de stock con motivo
- Historial global y por producto
- Dashboard con métricas y stock crítico
- Exportación CSV/PDF de productos y movimientos
- Auditoría del sistema
- Búsqueda global agregada
- Control de acceso por permisos granulares

---

## 🌐 Frontend

Repositorio UI (Astro):  
🔗 [sm4rt-stock-frontend](https://github.com/NerokxMal/sm4rt-stock-frontend)

---

## 👨‍💻 Autor

**Malcom García**

- LinkedIn: [malcom-nk-garcia](https://www.linkedin.com/in/malcom-nk-garcia)
- GitHub: [NerokxMal](https://github.com/NerokxMal)

---

## 📄 Licencia

MIT
