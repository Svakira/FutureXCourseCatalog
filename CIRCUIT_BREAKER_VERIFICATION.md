# Guía de Verificación de Circuit Breaker

## Importante: Limitaciones de las Pruebas Unitarias

Las pruebas unitarias NO pueden probar la activación automática del Circuit Breaker.

**Razón:** Resilience4j usa AOP (Aspect-Oriented Programming) de Spring, que solo funciona en beans reales de Spring, no en objetos mock. Las pruebas unitarias con Mockito no pueden interceptar las anotaciones `@CircuitBreaker`.

**Lo que SÍ prueban las unitarias:**
- Que los métodos del controlador funcionen correctamente
- Que los métodos fallback retornen las respuestas esperadas
- Que la lógica de negocio sea correcta

**Lo que NO prueban:**
- Activación automática del circuit breaker
- Transiciones de estado (CLOSED → OPEN → HALF_OPEN)
- Conteo de fallos y umbrales

## Verificación Real del Circuit Breaker

### Requisitos Previos

1. **FutureXEurekaServer** corriendo en puerto 8761
2. **FutureXCourseService** corriendo en puerto 8080
3. **FutureXCourseCatalog** corriendo en puerto 8002

### Demostración Paso a Paso

```bash
# Terminal 1 - Eureka Server
cd ../FutureXEurekaServer
mvn spring-boot:run

# Terminal 2 - Course Service  
cd ../FutureXCourseService
mvn spring-boot:run

# Terminal 3 - Course Catalog
cd ../FutureXCourseCatalog
mvn spring-boot:run
```

### Paso 1: Verificar funcionamiento normal

```bash
curl http://localhost:8002/
curl http://localhost:8002/catalog
curl http://localhost:8002/firstcourse
```

### Paso 2: Apagar el Course Service

```bash
# En el Terminal 2, presionar Ctrl+C para detener fx-course-service
```

### Paso 3: Verificar activación de fallbacks

```bash
curl http://localhost:8002/
# Fallback: "Welcome to FutureX Course Catalog - Course service is temporarily unavailable..."

curl http://localhost:8002/catalog
# Fallback: "Our courses are currently unavailable..."

curl http://localhost:8002/firstcourse
# Fallback: "Our first course information is currently unavailable..."
```

### Paso 4: Monitorear el Circuit Breaker
```bash
curl http://localhost:8002/actuator/circuitbreakers
```

### Paso 5: Ver eventos del Circuit Breaker

```bash
curl http://localhost:8002/actuator/circuitbreakerevents
```

## Configuración que Controla la Activación

En `application.yml`:

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

**Esto significa:**
- Necesitas al menos **5 llamadas** para que el circuit breaker evalúe
- Si **50% o más fallan**, el circuito se abre
- Cuando está abierto, **todas las llamadas** usan el fallback automáticamente
- Después de **5 segundos**, intenta recuperarse (estado HALF_OPEN)

## Demostración para Presentación

1. **Mostrar servicios funcionando** → Llamar endpoints, mostrar respuestas normales
2. **Apagar course-service** → Explicar que se simula una falla del servicio
3. **Llamar endpoints repetidamente** → Después de 5 llamadas, el circuit breaker se abre
4. **Mostrar actuator/circuitbreakers** → Ver estado OPEN
5. **Reiniciar course-service** → Esperar 5 segundos
6. **Llamar de nuevo** → Mostrar recuperación automática (HALF_OPEN → CLOSED)

## Conclusión

**El Circuit Breaker solo puede probarse de manera real ejecutando la aplicación completa**. Las pruebas unitarias validan la lógica de negocio, pero Resilience4j requiere el contexto completo de Spring para funcionar.