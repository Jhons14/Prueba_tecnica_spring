# Proyecto de Microservicios: Gestión de Productos e Inventario

Una completa implementación de arquitectura de microservicios que incluye gestión de productos y control de inventario con capacidades de procesamiento de compras.

## Tabla de Contenidos

- Visión general de la arquitectura
- Pila técnica
- Descripción de servicios
- Diseño del flujo de compras
- Instalación y configuración
- Documentación de la API
- Pruebas
- Supervisión y controles
- Decisiones técnicas
- Uso de herramientas de IA

## Visión general de la arquitectura

```
┌─────────────────┐    HTTP/JSON API    ┌─────────────────┐
│                 │ ◄──────────────────► │                 │
│  Products       │                      │   Inventory     │
│  Service        │                      │   Service       │
│  (Port 8080)    │                      │  (Port 8081)    │
│                 │                      │                 │
└─────────────────┘                      └─────────────────┘
        │                                          │
        │                                          │
        ▼                                          ▼
┌─────────────────┐                      ┌─────────────────┐
│   H2 Database   │                      │   H2 Database   │
│   (Products)    │                      │ (Inventory &    │
│                 │                      │  Purchases)     │
└─────────────────┘                      └─────────────────┘
```

### Service Communication Flow

```
Client Request ──► Inventory Service ──► Products Service
                         │                       │
                         ▼                       ▼
                  Purchase Processing    Product Information
                         │                       │
                         ▼                       │
                  Inventory Update ◄─────────────┘
                         │
                         ▼
                  Purchase History
```

## Technical Stack

- Marco de trabajo Spring Boot 3.2.2
- **Lenguaje**: Java 17
- Base de datos H2 (Base de datos SQL en memoria)
- **Seguridad**: Spring Security con autenticación de clave API
- **Documentación**: OpenAPI 3 (Swagger)
- **Contenedores**: Docker y Docker Compose
- **Pruebas**: JUnit 5, MockMvc, Testcontainers
- **Herramienta de compilación**: Maven
- **API estándar**: API JSON (https://jsonapi.org/)

## Descripción de Servicios

### 1. Servicio de Productos (Puerto 8080)

**Responsabilidades:**
- Creación y gestión de productos
- Recuperación de información de productos
- Mantenimiento del catálogo de productos

**End Points:**
- `POST /api/products` - Crear un nuevo producto
- `GET /api/products/{id}` - Obtener producto por ID
- `GET /api/products` - Listar todos los productos

**Model:**
```java
Product {
 Long id;
 String name;
 BigDecimal price;
 String description; // optional
}
```

### 2. Servicio de Inventario (Puerto 8081)

**Responsabilidades:**
- Gestión de la cantidad de inventario
- Procesamiento de compras (ubicación elegida)
- Seguimiento del historial de compras
- Comunicación interservicios con el Servicio de Productos

**Puntos finales:**
- `GET /api/inventory/products/{productId}` - Obtener inventario por ID de producto
- `PUT /api/inventory/products/{productId}` - Actualizar cantidad de inventario
- `POST /api/purchases` - Procesar compra (punto final principal)

**Models:**
```java
Inventory {
 Long id;
 Long productId;
 Integer quantity;
}

Purchase {
 Long id;
 Long productId;
 Integer quantity;
 BigDecimal totalPrice;
 LocalDateTime purchaseDate;
}
```

## Diseño del Flujo de Compra

### Decisión: Purchase Endpoint Location

**Ubicación elegida: Servicio de Inventario**

**Justificación:**
1. **Responsabilidad única**: El servicio de inventario es el propietario de la gestión de existencias
2. **Consistencia de los datos**: Las actualizaciones de compras e inventario se producen en la misma transacción
3. **Acoplamiento reducido**: El servicio de productos sigue centrado en la gestión de productos
4. **Alineación de la lógica empresarial**: La compra es fundamentalmente una operación de inventario

### Flujo del proceso de compra

```
1. Cliente envía solicitud de compra
 ↓
2. El Servicio de Inventario valida la existencia del producto (llama al Servicio de Productos)
 ↓
3. Comprueba disponibilidad de inventario
 ↓
4. Si hay existencias suficientes:
   a) Disminuye la cantidad de inventario
   b) Calcula el precio total
   c) Crear registro de compra
   d) Devolver confirmación de compra
 ↓
5. Si no hay existencias suficientes o no se encuentra el producto
   Devolver respuesta de error apropiada
```

### Tratamiento de errores

- Producto no encontrado (404)**: Cuando el producto no existe en el Servicio de Productos
- Inventario insuficiente (400)**: Cuando la cantidad solicitada excede el stock disponible
- Errores de validación (400)**: Datos de entrada no válidos
- Errores de comunicación del servicio (500)**: Servicio de productos no disponible

## Instalación y configuración

### Requisitos previos

- Java 17+
- Maven 3.8+
- Docker y Docker Compose

### Configuración de desarrollo local

1. **Clone el repositorio**
 ```bash
 git clone <repository-url>
 cd spring-microservices
 ```

2. **Construir los servicios**
 ```bash
   # Servicio de productos
 cd products-service
 mvn clean package
 cd ..
   
   # Inventory Service
 cd inventory-service
 mvn clean package
 cd ..
 ```

3. **Ejecutar con Docker Compose**
 ```bash
 docker-compose up --build
 ```

4. **Verificar que los servicios se están ejecutando**
   - Servicio de productos: http://localhost:8080/actuator/health
   - Servicio de inventario: http://localhost:8081/actuator/health

### Configuración Manual (Desarrollo)

1. **Iniciar el servicio de productos**
 ```bash
 cd products-service
 mvn spring-boot:run
 ```

2. 2. **Iniciar Servicio de Inventario**
 ```bash
 cd inventory-service
 mvn spring-boot:run
 ```

## Documentación API

### Documentación OpenAPI/Swagger

Una vez que los servicios se estén ejecutando, acceda a la documentación interactiva de la API:

- **Servicio de productos**: http://localhost:8080/swagger-ui/index.html
- **Servicio de inventario**: http://localhost:8081/swagger-ui/index.html

### Autenticación


Todos API endpoints requieren la autenticación de la clave de la API mediante el encabezado `X-API-Key`:

- Servicio de productos `products-service-api-key-123`.
- Servicio de inventario: clave API 456


### Cobertura de las pruebas

El proyecto incluye pruebas exhaustivas:

- **Pruebas unitarias**: Lógica de la capa de servicio, reglas de negocio
- **Pruebas de integración**: Pruebas de API de extremo a extremo
- **Pruebas de controlador**: Pruebas de la capa HTTP con MockMvc.
- **Objetivo de cobertura**: ≥ 80%.

### Categorías de pruebas

1. **Pruebas de gestión de productos**
   - Validación de la creación de productos
   - Funcionalidad de recuperación de productos
   - Escenarios de gestión de errores

2. **Pruebas de gestión de inventario**
   - Actualizaciones y consultas de inventario
   - Comprobación de la disponibilidad de existencias
   - Comunicación entre servicios

3. **Pruebas de flujo de compras**
   - Procesamiento completo de la compra
   - Gestión de existencias insuficiente
   - Escenarios de producto no encontrado
   - Pruebas de reversión de transacciones

## Monitorización y Comprobaciones de Salud

### Puntos finales de salud

- Servicio de productos: http://localhost:8080/actuator/health
- Servicio de inventario: http://localhost:8081/actuator/health

### Métricas & Monitorización

- **Puntos finales del actuador**: `/actuator/metrics`, `/actuator/info`
- **Registro estructurado**: Registros en formato JSON con ID de correlación
- **Indicadores de salud personalizados**: Comprobaciones de salud específicas del servicio

### Configuración de registros

- **Desarrollo**: Salida de consola con nivel DEBUG
- **Producción**: Registro basado en archivos con rotación
- **Formato**: Registro estructurado con marcas de tiempo y contexto de servicio

## Decisiones técnicas

### 1. Elección de base de datos: Base de datos H2 SQL



**Justificación**
- **Datos estructurados**: Modelo relacional claro (Productos ↔ Inventario ↔ Compras).
- **Cumplimiento deACID**: Esencial para las transacciones de compra
- **Simplicidad de desarrollo**: Cero configuración, pruebas sencillas
- **Preparado para producción**: Fácil migración a PostgreSQL/MySQL

### 2. Ubicación del punto final de compra: Servicio de Inventario

**Justificación**
- **Alineación de la lógica de negocio**: La compra es fundamentalmente una operación de inventario
- **Consistencia de datos**: Una única transacción para la actualización del inventario y la creación de la compra
- **Límites del servicio**: Mantiene responsabilidades de servicio claras
- **Acoplamiento reducido**: El servicio de productos se mantiene centrado en la gestión del producto

### 3. Implementación del estándar API JSON

**Justificación**
- **Estandarización**: Formato API estándar del sector
- **Coherencia**: Estructura de respuesta uniforme en todos los puntos finales
- **Gestión de errores**: Respuestas de error estructuradas
- **A prueba de futuro**: Fácil integración con frameworks frontend

### 4. Autenticación mediante clave API

**Justificación**
- **Simplicidad**: Fácil de implementar y probar
- **Servicio-a-Servicio**: Apropiado para la comunicación entre microservicios
- **Sin estado**: No requiere gestión de sesiones
- **Escalable**: Fácil de extender con autenticación más sofisticada

### 5. Estrategia de tiempo de espera y reintento

**Implementación:**
- **Tiempo de espera del cliente HTTP**: Tiempo de espera de 10 segundos para llamadas de servicio
- **Lógica de reintentos**: 3 intentos de reintento con backoff exponencial
- **Patrón de interrupción del circuito**: Listo para ser implementado si es necesario

## AI Tools Usage


### Herramientas utilizadas

1. **Código Claude (Asistente principal)**
   - **Diseño de la arquitectura**: Estructura de microservicios y patrones de comunicación
   - **Generación de código**: Implementaciones completas de servicios siguiendo las mejores prácticas
   - **Creación de pruebas**: Completas suites de pruebas unitarias y de integración
   - **Configuración**: Configuración de Docker, registro y monitorización.

2. **Métodos de verificación de la calidad del código**

   **Análisis estático**
   - Revisión del código para las mejores prácticas de Spring Boot
   - Verificación de implementación de patrones de diseño
   - Evaluación de vulnerabilidades de seguridad

   **Verificación de pruebas**
   - Análisis de cobertura de pruebas unitarias
   - Validación de escenarios de pruebas de integración
   - Corrección de la implementación simulada

   **Revisión arquitectónica:**
   - Validación de los límites del servicio
   - Comprobación de la coherencia del diseño de la API
   - Normalización del diseño de la base de datos

### Contribuciones específicas de AI

1. **Implementación de servicios**
   - Generación de controladores REST completos con conformidad API JSON
   - Implementación de la gestión integral de errores
   - Creada capa de servicio robusta con gestión de transacciones

2. **Estrategia de pruebas**
   - Generación de pruebas unitarias que cubren todos los escenarios de lógica de negocio
   - Creación de pruebas de integración para flujos de trabajo integrales
   - Implementación de constructores de datos de prueba y accesorios

3. **Configuración y DevOps**
   - Configuración de contenedores Docker
   - Configuración de registro con perfiles específicos del entorno
   - Implementación de Health Check

4. **Documentación**


   - Generación de especificaciones OpenAPI
   - Creación de diagramas de arquitectura
   - Amplia documentación README

### Proceso de garantía de calidad

1. **Revisión del código**: Revisión manual de todo el código generado para las mejores prácticas
2. **Ejecución de pruebas**: Todas las pruebas generadas ejecutadas y validadas
3. **Pruebas de integración**: Pruebas manuales de las interacciones de los servicios
4. **Validación del rendimiento**: Pruebas básicas de carga de puntos finales críticos.
5. **Revisión de seguridad**: Validación de autenticación y autorización de API

---

## Guía rápida de inicio

1. **Iniciar Servicios**: `docker-compose up --build`
2. **Crear Producto**: Usar Swagger UI en http://localhost:8080/swagger-ui/index.html
3. **Añadir Inventario**: Actualizar inventario a través de http://localhost:8081/swagger-ui/index.html
4. **Procesar Compra**: Realizar la compra a través del servicio de inventario
5. **Vigilar la salud**: Comprobar http://localhost:8080/actuator/health y http://localhost:8081/actuator/health

Para ejemplos detallados de API y configuración avanzada, consulte las secciones anteriores.
