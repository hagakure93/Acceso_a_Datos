# Proyecto: Procesamiento de Contratos Adjudicados

Aplicación Java que procesa un XML de contratos adjudicados de la Junta de Andalucía, los carga en una base de datos MySQL y genera un XML de salida sin el campo "TIPO DE CONTRATO".

## Requisitos

- Java 17 o superior
- Maven 3.6+
- MySQL o MariaDB
- Base de datos `contratosdb` creada

## Clonar el repositorio

```bash
git clone https://github.com/hagakure93/Acceso_a_Datos.git
cd Acceso_a_Datos
```

## Configuración

1. **Crear la base de datos:**
   ```sql
   CREATE DATABASE contratosdb;
   ```

2. **Configurar las credenciales** en `app/src/main/resources/application.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/contratosdb
   db.user=tu_usuario
   db.password=tu_contraseña
   input.xml=contratos-adjudicados-jun-25.xml
   output.xml=salida-contratos-sin-tipo.xml
   ```

## Ejecutar la aplicación

1. **Navegar al directorio `app`:**
   ```bash
   cd app
   ```

2. **Ejecutar con Maven:**
   ```bash
   mvn exec:java
   ```

3. **O compilar y ejecutar:**
   ```bash
   mvn clean package
   java -jar target/contratos-app-1.0.0-SNAPSHOT.jar
   ```

## Resultado

- Los datos se cargan en la tabla `contratos` de la base de datos `contratosdb`
- Se genera el archivo `app/salida-contratos-sin-tipo.xml` sin el campo "TIPO DE CONTRATO"

## Estructura del proyecto

```
app/
├── pom.xml                          # Configuración Maven
├── src/main/java/                   # Código fuente Java
├── src/main/resources/              # Configuración y XML de entrada
└── salida-contratos-sin-tipo.xml    # XML de salida generado
```

## Documentación

- `enunciado.md`: Enunciado del proyecto
- `resumen.md`: Documentación técnica completa
