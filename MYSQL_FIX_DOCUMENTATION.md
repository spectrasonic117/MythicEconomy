# Documentación de Correcciones de MySQL en MythicEconomy

## Problema Identificado

El plugin tenía un bug crítico donde:
1. Los jugadores nuevos recibían un saldo por defecto pero no se guardaba en la base de datos
2. Al recargar el plugin, los valores se reseteaban a 0
3. Los valores no se persistían correctamente en MySQL

## Causas del Problema

1. **Método `getBalance()` no creaba jugadores**: Cuando un jugador no existía en la BD, devolvía el saldo inicial pero no lo creaba
2. **Falta de listener de entrada**: No había un sistema automático para crear jugadores cuando entraban al servidor
3. **Lógica confusa en EconomyManager**: Había código específico para MongoDB que no aplicaba a MySQL
4. **Métodos incompletos**: Faltaba manejo específico para MySQL en varios métodos del EconomyManager

## Soluciones Implementadas

### 1. Corrección en MySQLEconomyProvider.getBalance()

```java
// ANTES: Solo devolvía el saldo inicial
if (rs.next()) {
    return rs.getDouble("balance");
} else {
    // Jugador nuevo, devolver saldo inicial de la moneda
    return currency != null ? currency.getStartingBalance() : 100.0;
}

// AHORA: Crea el jugador si no existe
if (rs.next()) {
    return rs.getDouble("balance");
} else {
    // Jugador nuevo, crearlo en la base de datos con saldo inicial
    double startingBalance = currency != null ? currency.getStartingBalance() : 100.0;
    createPlayer(playerUUID, currencyId);
    return startingBalance;
}
```

### 2. Mejora en createPlayer()

Se implementó `INSERT IGNORE` para evitar duplicados y asegurar que el jugador exista:

```java
String sql = "INSERT IGNORE INTO player_balances (player_uuid, currency_id, balance, last_updated) " +
             "VALUES (?, ?, ?, NOW())";
```

### 3. PlayerJoinListener

Se creó un listener que se ejecuta cuando un jugador entra al servidor:

```java
@EventHandler(priority = EventPriority.MONITOR)
public void onPlayerJoin(PlayerJoinEvent event) {
    // Actualiza el nombre del jugador
    economyManager.updatePlayerName(player.getUniqueId(), player.getName());
    
    // Asegura que el jugador exista en todas las monedas
    if (economyManager.isUsingMySQL()) {
        mysqlProvider.ensurePlayerExists(player.getUniqueId());
    }
}
```

### 4. Métodos de utilidad

Se agregaron métodos para verificar y asegurar la existencia de jugadores:

```java
public boolean playerExists(UUID playerUUID, String currencyId)
public void ensurePlayerExists(UUID playerUUID)
```

### 5. Comando de diagnóstico integrado

Se agregó el subcomando `/currency check` al comando existente para diagnosticar problemas:

```
/currency check <jugador> <moneda>  - Verifica el estado del almacenamiento y datos
```

Este comando:
- Muestra el tipo de almacenamiento (MySQL, MongoDB o archivos)
- Verifica si el jugador existe en la base de datos
- Crea automáticamente el registro si no existe
- Muestra el balance actual y lo compara con el saldo inicial
- Proporciona advertencias sobre posibles problemas

## Flujo de Datos Corregido

### Cuando un jugador nuevo entra:

1. **PlayerJoinListener** se activa
2. Se actualiza el nombre del jugador en la BD
3. Se llama a `ensurePlayerExists()` que:
   - Verifica si el jugador existe para cada moneda
   - Si no existe, lo crea con el saldo inicial
4. El jugador queda correctamente registrado en la BD

### Cuando se consulta el balance:

1. **getBalance()** busca al jugador en la BD
2. Si no existe, lo crea automáticamente con `createPlayer()`
3. Devuelve el saldo correcto (persistido en BD)

## Configuración Recomendada

### config.yml

```yaml
database:
  async-mode: true
  type: "MYSQL"
  use-external-database: true
  
  mysql:
    host: "localhost"
    port: 3306
    database: "MythicEconomy"
    username: "root"
    password: "tu_password"
    use-ssl: false
```

### Estructura de la tabla MySQL

```sql
CREATE TABLE IF NOT EXISTS player_balances (
    player_uuid VARCHAR(36) NOT NULL,
    currency_id VARCHAR(50) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (player_uuid, currency_id),
    INDEX idx_currency_id (currency_id),
    INDEX idx_player_uuid (player_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Pasos para Migrar Datos Existentes

1. **Backup actual**: Haz un backup de tu base de datos
2. **Instala la versión actualizada**: Reemplaza el plugin JAR
3. **Reinicia el servidor**: El plugin se actualizará automáticamente
4. **Verifica jugadores**: Usa `/economydb check <jugador> <moneda>`
5. **Repara si es necesario**: Usa `/economydb fix <jugador>`

## Solución de Problemas

### Si los balances siguen apareciendo como 0:

1. **Verifica el estado**:
   ```
   /currency check <jugador> default
   ```

2. **Si el jugador no existe, el comando lo creará automáticamente**

3. **Verifica manualmente el balance**:
   ```
   /balcur default <jugador>
   ```

3. **Verifica la configuración**:
   - Asegúrate que `database.use-external-database` sea `true`
   - Verifica que `database.type` sea `MYSQL`
   - Confirma las credenciales de MySQL

### Si hay errores de conexión:

1. **Verifica que MySQL esté corriendo**
2. **Confirma la base de datos existe**: `CREATE DATABASE MythicEconomy;`
3. **Verifica permisos del usuario MySQL**
4. **Revisa el firewall del servidor**

## Monitoreo y Mantenimiento

### Logs importantes:

- `INFO: Jugador creado en MySQL para moneda X: UUID`
- `WARNING: No hay conexión activa con MySQL`
- `SEVERE: Error al obtener saldo desde MySQL`

### Comandos útiles:

```sql
-- Ver todos los jugadores
SELECT DISTINCT player_uuid FROM player_balances;

-- Ver balances de un jugador
SELECT * FROM player_balances WHERE player_uuid = 'UUID_DEL_JUGADOR';

-- Ver total de dinero por moneda
SELECT currency_id, COUNT(*) as jugadores, SUM(balance) as total 
FROM player_balances GROUP BY currency_id;
```

## Mejoras de Rendimiento

1. **Índices optimizados**: La tabla ya tiene índices en UUID y currency_id
2. **Conexión asíncrona**: Usa HikariCP para mejor rendimiento
3. **INSERT IGNORE**: Evita errores de duplicados
4. **Batch operations**: Para múltiples operaciones

## Consideraciones de Seguridad

1. **Usa siempre prepared statements** (ya implementado)
2. **Valida entradas de usuario** (ya implementado)
3. **Limita permisos del usuario MySQL** a solo las operaciones necesarias
4. **Usa SSL en producción** si es posible

## Soporte

Si encuentras problemas:

1. Revisa los logs del servidor
2. Usa el comando `/economydb` para diagnóstico
3. Verifica la configuración de MySQL
4. Consulta esta documentación

## Comandos Útiles para Diagnóstico

### Verificar estado del sistema:
```
/currency check <jugador> <moneda>     - Diagnóstico completo
/balcur <moneda>                      - Ver balance propio
/balcur <moneda> player <jugador>     - Ver balance de otro jugador
/currency list                        - Listar monedas disponibles
/currency info <moneda>               - Información de moneda
```

### Administración de datos:
```
/currency give <jugador> <cantidad> <moneda>  - Dar dinero
/currency take <jugador> <cantidad> <moneda>  - Quitar dinero
/currency set <jugador> <cantidad> <moneda>  - Establecer balance
```

## Cambios Futuros Recomendados

1. **Sistema de cache** para reducir consultas a la BD
2. **Mecanismo de retry** para conexiones fallidas
3. **Dashboard web** para administración
4. **API REST** para integraciones externas