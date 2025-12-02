# Solución al Problema de Conexión MySQL en MythicEconomy

## Problema Identificado

El plugin mostraba el error "No hay conexión activa con MySQL" al ejecutar comandos como `/currency set Spectrasonic 1000 money`, aunque al iniciar el plugin parecía conectarse correctamente.

## Causas del Problema

1. **Configuración inconsistente**: En `config.yml`, el tipo de base de datos estaba configurado como "FILE" pero `use-external-database` estaba en `true`, causando una inconsistencia.

2. **Falta de reconexión automática**: La conexión MySQL podía perderse por timeouts del servidor o de la base de datos, pero no había un mecanismo para reconectar automáticamente.

3. **Manejo inadecuado de errores**: Cuando una operación fallaba por pérdida de conexión, no se intentaba reconectar para operaciones futuras.

## Soluciones Implementadas

### 1. Corrección de Configuración

Se modificó `src/main/resources/config.yml` para usar correctamente MySQL:

```yaml
# Tipo de almacenamiento: FILE, MYSQL o MONGODB
type: "MYSQL"

# Usar base de datos externa (true) o sistema interno del plugin (false)
use-external-database: true
```

### 2. Mejoras en MySQLConnection.java

- **Reconexión automática**: El método `connect()` ahora verifica si hay una conexión existente y válida antes de crear una nueva.
- **Parámetros de conexión mejorados**: Se añadieron parámetros para auto-reconexión y failover:
  ```java
  String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&autoReconnect=true&failOverReadOnly=false&maxReconnects=10&initialTimeout=10",
          host, port, database, useSSL);
  ```
- **Timeout de red**: Se estableció un timeout de 30 segundos para las operaciones de red.
- **Manejo mejorado de errores**: Los métodos que realizan operaciones de base de datos ahora intentan reconectar si la conexión se pierde.

### 3. Mejoras en MySQLEconomyProvider.java

Todos los métodos de operación (`getBalance`, `setBalance`, `addBalance`, `removeBalance`, `hasEnoughBalance`, `createPlayer`) ahora:

- Intentan reconectar automáticamente si detectan que la conexión se ha perdido
- Muestran mensajes informativos en la consola sobre el estado de la conexión
- Cierran correctamente las conexiones problemáticas para forzar una reconexión limpia

### 4. Tarea de Verificación Periódica

Se añadió una tarea programada en `EconomyManager.java` que verifica cada 5 minutos si la conexión MySQL está activa y la restablece si es necesario:

```java
private void startMySQLConnectionCheckTask() {
    // Verificar la conexión cada 5 minutos (6000 ticks)
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
        if (isUsingMySQL() && mysqlConnection != null) {
            if (!mysqlConnection.isConnected()) {
                plugin.getLogger().warning("Conexión MySQL perdida, intentando reconectar...");
                if (mysqlConnection.connect()) {
                    plugin.getLogger().info("Conexión MySQL restablecida exitosamente");
                } else {
                    plugin.getLogger().severe("No se pudo restablecer la conexión MySQL");
                }
            }
        }
    }, 6000L, 6000L);
}
```

## Cómo Verificar la Solución

1. **Reinicia el plugin** después de aplicar los cambios.
2. **Verifica los mensajes de inicio** en la consola. Deberías ver:
   - "Conexión exitosa a MySQL"
   - "Usando MySQL como proveedor de datos de economía"
3. **Ejecuta comandos de economía** como `/currency set Spectrasonic 1000 money`
4. **Observa la consola** para verificar que no aparezca el error "No hay conexión activa con MySQL"

## Solución de Problemas Adicionales

### Si la conexión sigue fallando:

1. **Verifica la configuración de MySQL** en `config.yml`:
   ```yaml
   mysql:
       host: "localhost"
       port: 3306
       database: "MythicEconomy"
       username: "root"
       password: ""
       use-ssl: false
   ```

2. **Asegúrate de que el servidor MySQL esté funcionando** y sea accesible desde el servidor de Minecraft.

3. **Verifica que la base de datos exista** y que el usuario tenga los permisos necesarios.

4. **Revisa los logs completos** del servidor para identificar errores específicos de conexión.

### Para monitorear la conexión:

Puedes usar el comando `/currency check <jugador> <moneda>` para verificar el estado del almacenamiento y diagnosticar problemas.

## Mejoras Futuras Sugeridas

1. **Implementar un pool de conexiones** con HikariCP para mayor rendimiento y fiabilidad.
2. **Añadir métricas de conexión** para monitorear el estado de la base de datos.
3. **Configurar notificaciones** cuando la conexión se pierda y se restablezca.
4. **Implementar modo degradado** que use almacenamiento temporal local si MySQL no está disponible.