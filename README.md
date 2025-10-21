# FutureX Course Catalog

Microservicio que consulta información de cursos desde `fx-course-service` utilizando **Eureka Client** para descubrimiento de servicios y **Resilience4j Circuit Breaker** para manejo de fallos.

## Requisitos Previos

Este microservicio requiere que los siguientes servicios estén en ejecución:

1. **FutureXEurekaServer** (puerto 8761) - Service Registry
2. **FutureXCourseService** (puerto 8080) - Proveedor de datos de cursos

Sin estos servicios, el catalog no podrá funcionar correctamente.

## Tecnologías

- Spring Boot 3.3.3
- Spring Cloud Netflix Eureka Client
- Resilience4j Circuit Breaker
- Spring Web
- JUnit 5 + Mockito

## Arquitectura

```
FutureXCourseCatalog (8002)
    ↓ descubre via
FutureXEurekaServer (8761)
    ↓ obtiene instancia de
FutureXCourseService (8080)
    ↓ llama con
RestTemplate + Circuit Breaker
    ↓ si falla
Fallback Methods
```

## Endpoints

| Endpoint | Descripción |
|----------|-------------|
| `GET /` | Mensaje de bienvenida combinado con respuesta del course service |
| `GET /catalog` | Lista de todos los cursos disponibles |
| `GET /firstcourse` | Información detallada del primer curso |

## Configuración Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
    instances:
      courseService:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
```

**Comportamiento:**
- Monitorea las últimas 10 llamadas
- Necesita mínimo 5 llamadas para evaluar
- Si ≥50% fallan → abre el circuito
- Circuito abierto durante 5 segundos antes de reintentar

## Instalación y Ejecución

### 1. Compilar
```bash
mvn clean package
```

### 2. Ejecutar pruebas
```bash
mvn test
```

### 3. Ejecutar aplicación
```bash
mvn spring-boot:run
```

**Nota:** Se debe asegurar que Eureka Server y Course Service estén corriendo primero.

## Pruebas

**Ejecutar todas las pruebas:**
```bash
mvn test
```

o usando el script:
```bash
./run-tests.sh
```

### Qué prueban

**Pruebas Unitarias** - Validan lógica de negocio y fallback methods
**Pruebas de Integración** - Validan endpoints con MockMvc  
**Pruebas de Contexto** - Validan inicialización de Spring Boot

**Total:** 10 pruebas

**Nota:** Las pruebas unitarias NO validan la activación automática del Circuit Breaker (requiere Spring AOP en tiempo de ejecución). Para probar el Circuit Breaker en acción, se debe consultar [CIRCUIT_BREAKER_VERIFICATION.md](CIRCUIT_BREAKER_VERIFICATION.md)

## Verificación del Circuit Breaker

Para probar el Circuit Breaker en tiempo real, se debe consultar: **[CIRCUIT_BREAKER_VERIFICATION.md](CIRCUIT_BREAKER_VERIFICATION.md)**

El documento incluye:
- Pasos detallados para demostración en vivo
- Cómo observar la transición de estados del circuit breaker
- Scripts de verificación automática
- Endpoints de actuator para monitoreo

## Monitoreo

Endpoints de Actuator disponibles:

```bash
# Estado de salud general
curl http://localhost:8002/actuator/health

# Estado de circuit breakers
curl http://localhost:8002/actuator/circuitbreakers

# Eventos del circuit breaker
curl http://localhost:8002/actuator/circuitbreakerevents
```

## Resiliencia Implementada

### Service Discovery
- Registro automático en Eureka al iniciar
- Descubrimiento dinámico de instancias de fx-course-service
- Load balancing automático con `@LoadBalanced RestTemplate`

### Circuit Breaker Pattern
- Protección contra cascada de fallos
- Degradación elegante con fallback methods
- Recuperación automática del servicio

### Fallback Methods

Cada endpoint tiene un fallback que retorna mensajes informativos:

```java
@CircuitBreaker(name = "courseService", fallbackMethod = "fallbackGetCatalog")
public String getCatalog() {
    // llamada al servicio
}

public String fallbackGetCatalog(Exception ex) {
    return "Our courses are currently unavailable...";
}
```

## Estructura del Proyecto

```
src/main/java/
├── CatalogController.java      # Endpoints REST con circuit breaker
├── Course.java                  # Modelo de datos
├── AppConfig.java               # Configuración de RestTemplate
└── FutureXCourseCatalogApplication.java

src/main/resources/
└── application.yml              # Configuración de Eureka y Resilience4j

src/test/java/
├── CatalogControllerTest.java  # Pruebas unitarias
└── CatalogControllerIntegrationTest.java
```

## Notas Importantes

**Las pruebas unitarias NO prueban la activación automática del circuit breaker** - solo validan que los métodos funcionen correctamente. Para verificar que el circuit breaker se activa automáticamente cuando hay fallos, se deben realizar pruebas en tiempo real siguiendo la guía de verificación.

**Circuit Breaker requiere AOP** - Resilience4j usa aspectos de Spring, por lo que el circuit breaker solo funciona en beans de Spring, no en objetos mock.

## Proyectos Relacionados

- **FutureXEurekaServer** - Service Registry (debe estar corriendo)
- **FutureXCourseService** - Proveedor de datos (debe estar corriendo)

