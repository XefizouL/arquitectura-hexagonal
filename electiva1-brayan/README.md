# Tienda API - Carrito de Compras y Gestión de Productos

API RESTful desarrollada con **Spring Boot** y **PostgreSQL** para la administración de un catálogo de productos y el flujo completo de compras mediante carrito de compras (pedidos y detalle de pedidos). El proyecto implementa **Arquitectura Hexagonal (Ports and Adapters)** —ver [ARQUITECTURA_HEXAGONAL.md](ARQUITECTURA_HEXAGONAL.md)—, persistencia con Spring Data JPA y Lombok en la capa de infraestructura.

---

## 1. Tecnologías Empleadas y Versiones

| Tecnología | Versión | Propósito |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | OpenJDK 21 LTS | Lenguaje base de desarrollo |
| **Spring Boot** | 4.1.x / 3.x | Framework de aplicaciones Java y servidor embebido Tomcat |
| **Spring Data JPA / Hibernate** | 7.x / 6.x | Mapeo objeto-relacional (ORM) y persistencia |
| **PostgreSQL** | 17.x / 16.x | Motor de base de datos relacional |
| **Project Lombok** | 1.18.x | Reducción de código repetitivo (getters, setters, builders, inyección) |
| **Apache Maven** | 3.9+ (mvnw wrapper) | Gestión de dependencias y construcción del proyecto |
| **Docker & Docker Compose** | 27+ | Despliegue de contenedor para base de datos y pgAdmin |

---

## 2. Instrucciones de Configuración y Ejecución

### 2.1 Requisitos Previos
- Docker y Docker Compose instalados, o PostgreSQL instalado localmente en el puerto `5432`.
- JDK 17 o 21 configurado en el entorno.

### 2.2 Despliegue de la Base de Datos con Docker Compose
El proyecto incluye un archivo `docker-compose.yml` que levanta automáticamente PostgreSQL y pgAdmin:

```bash
# Iniciar servicios de PostgreSQL y pgAdmin en segundo plano
docker compose up -d
```

Si se usa una instancia local de PostgreSQL, ingresar a la consola `psql` o a pgAdmin y crear la base de datos:
```sql
CREATE DATABASE tienda_db;
CREATE USER postgres WITH SUPERUSER PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE tienda_db TO postgres;
```

### 2.3 Configuración de Conexión (`application.properties`)
El archivo `src/main/resources/application.properties` se encuentra parametrizado con los datos estándar:

```properties
spring.application.name=api-tienda
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/tienda_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 2.4 Compilación y Ejecución
Para compilar y ejecutar el proyecto mediante el Maven Wrapper:

```bash
# Compilar y ejecutar pruebas unitarias
./mvnw clean test

# Iniciar la aplicación
./mvnw spring-boot:run
```

La aplicación arrancará sobre el puerto `8080` y Tomcat responderá en `http://localhost:8080`.

---

## 3. Modelo de Datos y Reglas de Negocio

### 3.1 Modelo Entidad-Relación

```
+-----------------------------------+
|             productos             |
+-----------------------------------+
| id : BIGINT (PK)                  |
| nombre : VARCHAR(100) NOT NULL    |
| descripcion : VARCHAR(255)        |
| precio : NUMERIC(12, 2) NOT NULL  |
| stock : INT NOT NULL              |
+-----------------------------------+
                  ^
                  |
                  | 1:N
                  |
+-----------------------------------+       1:N       +-----------------------------------+
|          detalle_pedidos          | <-------------- |              pedidos              |
+-----------------------------------+                 +-----------------------------------+
| id : BIGINT (PK)                  |                 | id : BIGINT (PK)                  |
| pedido_id : BIGINT (FK) NOT NULL  |                 | cliente : VARCHAR(100) NOT NULL   |
| producto_id : BIGINT (FK) NOT NULL|                 | fecha : TIMESTAMP                 |
| cantidad : INT NOT NULL           |                 | estado : VARCHAR(20)              |
| precio_unitario : NUMERIC(12, 2)  |                 | total : NUMERIC(12, 2)            |
| subtotal : NUMERIC(12, 2)         |                 +-----------------------------------+
+-----------------------------------+
```

- **`productos`**: Catálogo de artículos disponibles con su precio unitario y cantidad en existencia (`stock`).
- **`pedidos`**: Registro principal de la compra o carrito. Contiene el nombre del cliente, fecha, estado (`CARRITO`, `CONFIRMADO`, `CANCELADO`) y valor total acumulado.
- **`detalle_pedidos`**: Tabla intermedia que asocia cada ítem del pedido con un producto del catálogo, guardando cantidad, precio vigente y subtotal.

### 3.2 Reglas de Negocio Obligatorias
1. **Cálculo de Totales en el Servidor**: El subtotal de cada ítem (`cantidad * precioUnitario`) y el total del pedido (`sum(subtotales)`) se calculan estrictamente en el backend, nunca son provistos por el cliente.
2. **Validación de Cantidades**: Cualquier ítem con cantidad menor o igual a cero es rechazado con código `400 Bad Request`.
3. **Restricción de Estados**: Solo los pedidos en estado `CARRITO` pueden ser modificados (agregar ítems, retirar ítems, actualizar o confirmar). Cualquier intento de modificación sobre un pedido `CONFIRMADO` retorna `400 Bad Request`.
4. **Validación Transaccional de Inventario**: Al confirmar el pedido (`PATCH /api/pedidos/{id}/confirmar`), se verifica que todos los productos cuenten con stock suficiente. Si alguno no tiene existencias, la transacción se revierte íntegramente (`@Transactional`) y ningún stock se descuenta.
5. **Integridad Referencial (Conflicto 409)**: No se permite eliminar un producto que forme parte del detalle de un pedido existente. El manejador global de excepciones captura la restricción de llave foránea y responde con `409 Conflict`.

---

## 4. Catálogo de Endpoints de la API

La API expone un total de **13 endpoints** estructurados bajo el estándar RESTful:

### 4.1 Módulo de Productos (`/api/productos`)

| Método | Ruta | Código HTTP | Descripción |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/productos` | `200 OK` | Listar todos los productos registrados |
| **GET** | `/api/productos/{id}` | `200 OK` / `404 Not Found` | Consultar producto por su identificador único |
| **POST** | `/api/productos` | `201 Created` | Crear un nuevo producto en el catálogo |
| **PUT** | `/api/productos/{id}` | `200 OK` / `404 Not Found` | Actualizar los datos de un producto existente |
| **DELETE** | `/api/productos/{id}` | `204 No Content` / `404` / `409` | Eliminar un producto (409 si está en pedidos) |

#### Ejemplos de Petición y Respuesta - Productos

**POST /api/productos**
- **Cuerpo Petición:**
  ```json
  {
    "nombre": "Teclado Mecánico RGB",
    "descripcion": "Switches rojos, retroiluminado",
    "precio": 189900.00,
    "stock": 15
  }
  ```
- **Respuesta (201 Created):**
  ```json
  {
    "id": 1,
    "nombre": "Teclado Mecánico RGB",
    "descripcion": "Switches rojos, retroiluminado",
    "precio": 189900.00,
    "stock": 15
  }
  ```

---

### 4.2 Módulo de Pedidos y Carrito (`/api/pedidos`)

| Método | Ruta | Código HTTP | Descripción |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/pedidos` | `200 OK` | Listar todos los pedidos con su detalle |
| **GET** | `/api/pedidos/{id}` | `200 OK` / `404 Not Found` | Consultar pedido por ID con subtotales y total |
| **POST** | `/api/pedidos` | `201 Created` / `400 Bad Request` | Crear un carrito con cliente e ítems iniciales |
| **PUT** | `/api/pedidos/{id}` | `200 OK` / `400` / `404` | Actualizar cliente y reemplazar ítems del carrito |
| **DELETE** | `/api/pedidos/{id}` | `204 No Content` / `404` | Eliminar pedido y sus detalles en cascada |
| **POST** | `/api/pedidos/{id}/items` | `200 OK` / `400` / `404` | Agregar un ítem de producto al carrito |
| **DELETE** | `/api/pedidos/{id}/items/{itemId}` | `200 OK` / `404 Not Found` | Retirar un ítem del carrito y recalcular total |
| **PATCH** | `/api/pedidos/{id}/confirmar` | `200 OK` / `400 Bad Request` | Confirmar compra y descontar existencias |

#### Ejemplos de Petición y Respuesta - Pedidos

**POST /api/pedidos**
- **Cuerpo Petición:**
  ```json
  {
    "cliente": "Brayan Gómez",
    "items": [
      { "productoId": 1, "cantidad": 2 },
      { "productoId": 2, "cantidad": 1 }
    ]
  }
  ```
- **Respuesta (201 Created):**
  ```json
  {
    "id": 1,
    "cliente": "Brayan Gómez",
    "fecha": "2026-10-19T09:16:58.689",
    "estado": "CARRITO",
    "total": 479700.00,
    "items": [
      {
        "id": 1,
        "producto": {
          "id": 1,
          "nombre": "Teclado Mecánico RGB",
          "precio": 189900.00,
          "stock": 15
        },
        "cantidad": 2,
        "precioUnitario": 189900.00,
        "subtotal": 379800.00
      },
      {
        "id": 2,
        "producto": {
          "id": 2,
          "nombre": "Mouse Gamer",
          "precio": 79900.00,
          "stock": 25
        },
        "cantidad": 1,
        "precioUnitario": 79900.00,
        "subtotal": 79900.00
      }
    ]
  }
  ```

**POST /api/pedidos/{id}/items**
- **Cuerpo Petición:**
  ```json
  {
    "productoId": 3,
    "cantidad": 1
  }
  ```

---

## 5. Arquitectura y uso de Lombok

- El proyecto está organizado en `domain` (modelo, excepciones y puertos), `application` (casos de uso) e `infrastructure` (adaptadores REST, persistencia JPA y configuración). El detalle, el diagrama y la tabla de puertos/adaptadores están en [ARQUITECTURA_HEXAGONAL.md](ARQUITECTURA_HEXAGONAL.md).
- `domain` y `application` son Java puro: no usan Spring, JPA, Jackson ni Lombok.
- Lombok se usa únicamente en las entidades JPA de `infrastructure/adapter/output/persistence/entity` (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`). Se evitó `@Data` en entidades para prevenir conflictos con proxies Hibernate.
- Los casos de uso se ensamblan en `infrastructure/config/BeanConfig` mediante inyección por constructor.

---

## 6. Documentación Javadoc

Todas las clases y métodos públicos cuentan con comentarios Javadoc completos que describen su objetivo, parámetros (`@param`), valores de retorno (`@return`) y excepciones arrojadas (`@throws`).
Para generar el reporte HTML de Javadoc:
```bash
./mvnw javadoc:javadoc
```
Los archivos se encontrarán en el directorio `target/site/apidocs/index.html`.

---

## 7. Pruebas y Colección de Postman

El proyecto incluye el archivo `TiendaYa_API.postman_collection.json` con los 25 casos de prueba listos para importar en Postman o Thunder Client, organizados en las carpetas **Productos** y **Pedidos**.

---

## 8. Autor(es)

- **Aprendiz:** Brayan Gómez (Juan Diego Brayan)
- **Ficha / Programa:** Análisis y Desarrollo de Software / Ingeniería de Software - Electiva 1
- **Fecha:** Octubre de 2026