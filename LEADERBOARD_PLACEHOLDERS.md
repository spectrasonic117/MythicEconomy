# Sistema de Placeholders para Leaderboard - MythicEconomy

## Overview

El sistema de placeholders para leaderboard de MythicEconomy proporciona acceso en tiempo real a los rankings de economía para todas las monedas configuradas en el servidor. Utiliza un sistema de cache optimizado que se actualiza automáticamente cada segundo para garantizar rendimiento y datos actualizados.

## Características Principales

- **Actualización Automática**: Los datos se refrescan cada 20 ticks (1 segundo) sin intervención manual
- **Detección Automática de Monedas**: El sistema detecta y agrega nuevas monedas automáticamente
- **Alto Rendimiento**: Sistema de cache en memoria para consultas instantáneas
- **Multi-Moneda**: Soporte para todas las monedas configuradas
- **Compatibilidad Total**: Funciona con bases de datos externas (MySQL, MongoDB) y archivos locales
- **Formato Flexible**: Placeholders para nombres, valores, UUIDs y formatos personalizados
- **Cero Mantenimiento**: Sistema completamente automático que no requiere comandos ni configuración

## Placeholders Disponibles

### Placeholders Principales

#### Nombre del Jugador
```
%eco_<currency>_<position>_player%
```
- **currency**: ID de la moneda (ej: default, gems, coins)
- **position**: Posición en el ranking (1-100)
- **Retorna**: Nombre del jugador en esa posición

**Ejemplos:**
```
%eco_default_1_player%    - Jugador #1 en moneda por defecto
%eco_gems_5_player%       - Jugador #5 en moneda gems
%eco_coins_10_player%     - Jugador #10 en moneda coins
```

#### Valor del Jugador
```
%eco_<currency>_<position>_value%
```
- **currency**: ID de la moneda
- **position**: Posición en el ranking (1-100)
- **Retorna**: Balance formateado del jugador

**Ejemplos:**
```
%eco_default_1_value%     - "$1,000,000.00"
%eco_gems_5_value%        - "💎 500.50"
%eco_coins_10_value%      - "🪙 100.00"
```

#### UUID del Jugador
```
%eco_<currency>_<position>_uuid%
```
- **currency**: ID de la moneda
- **position**: Posición en el ranking (1-100)
- **Retorna**: UUID del jugador

**Ejemplos:**
```
%eco_default_1_uuid%      - "550e8400-e29b-41d4-a716-446655440000"
```

#### Valor sin Formato
```
%eco_<currency>_<position>_value_raw%
```
- **currency**: ID de la moneda
- **position**: Posición en el ranking (1-100)
- **Retorna**: Valor numérico sin formato

**Ejemplos:**
```
%eco_default_1_value_raw% - "1000000.00"
%eco_gems_5_value_raw%    - "500.50"
```

## Configuración

### Sistema Automático

El sistema de cache es completamente automático y requiere **cero configuración**:

- **Tamaño del Cache**: 100 jugadores por moneda (configurable en código)
- **Intervalo de Actualización**: 20 ticks (1 segundo)
- **Detección de Monedas**: Automático, detecta nuevas monedas al momento de ser creadas
- **Limpieza Automática**: Remueve monedas deshabilitadas del cache

### Modificación de Parámetros (Opcional)

Para ajustar estos parámetros, modifica la inicialización en `MythicEconomyPlaceholders.java`:

```java
this.leaderboardCache = new LeaderboardCache(plugin, 100, 20L);
//                                                 ^    ^
//                                            tamaño    intervalo en ticks
```

**Nota**: No se recomienda modificar estos parámetros a menos que sepas exactamente lo que haces.

## Ejemplos de Uso

### Scoreboard Básico

```
# Configuración de scoreboard con placeholders
&6&lTOP ECONOMÍA
&7&m-----------------
&e1º &f%eco_default_1_player% &7- &a%eco_default_1_value%
&e2º &f%eco_default_2_player% &7- &a%eco_default_2_value%
&e3º &f%eco_default_3_player% &7- &a%eco_default_3_value%
&e4º &f%eco_default_4_player% &7- &a%eco_default_4_value%
&e5º &f%eco_default_5_player% &7- &a%eco_default_5_value%
&7&m-----------------
&7Actualizado cada 1s
```

### Scoreboard Multi-Moneda

```
&6&lLEADERBOARDS
&7&m-----------------
&b&lDINERO:
&e1º &f%eco_default_1_player% &7(%eco_default_1_value%)
&e2º &f%eco_default_2_player% &7(%eco_default_2_value%)

&a&lGEMAS:
&e1º &f%eco_gems_1_player% &7(%eco_gems_1_value%)
&e2º &f%eco_gems_2_player% &7(%eco_gems_2_value%)

&6&lMONEDAS:
&e1º &f%eco_coins_1_player% &7(%eco_coins_1_value%)
&e2º &f%eco_coins_2_player% &7(%eco_coins_2_value%)
&7&m-----------------
```

### Mensajes de Chat

```
&6[Leaderboard] &eEl jugador más rico es &f%eco_default_1_player% &econ &a%eco_default_1_value%
&6[Leaderboard] &eEstás en la posición &f#%eco_default_rank% &econ &a%eco_money_formatted%
```

### Integración con Plugins Web

Los placeholders pueden ser utilizados para generar datos para sitios web:

```
# Para exportar datos a JSON
{"player":"%eco_default_1_player%","uuid":"%eco_default_1_uuid%","balance":%eco_default_1_value_raw%}
```

## Rendimiento y Optimización

### Características de Optimización

1. **Cache en Memoria**: Los datos se almacenan en memoria para acceso instantáneo
2. **Actualización Asíncrona**: Las consultas a la base de datos se realizan en hilos separados
3. **ConcurrentHashMap**: Estructuras de datos seguras para concurrencia
4. **Consultas Optimizadas**: Solo se consultan los datos necesarios
5. **Detección Inteligente**: El sistema detecta automáticamente nuevas monedas sin reiniciar
6. **Limpieza Automática**: Remueve datos de monedas deshabilitadas para optimizar memoria

### Monitoreo de Rendimiento

El sistema es completamente automático y no requiere monitoreo manual. Sin embargo, puedes verificar el estado del sistema revisando los logs del servidor:

```
[MythicEconomy] LeaderboardCache iniciado - Actualizando cada 1 segundos
[MythicEconomy] Nueva moneda detectada en leaderboard: gems
[MythicEconomy] Moneda removida del leaderboard: old_currency
```

### Estadísticas Automáticas

El sistema mantiene estadísticas internas que se actualizan automáticamente:
- Número de monedas en cache
- Cantidad de nombres cacheados
- Intervalo de actualización
- Estado del sistema (siempre activo cuando PlaceholderAPI está disponible)

## Compatibilidad

### Bases de Datos Soportadas

- **MySQL**: Consultas optimizadas con índices apropiados
- **MongoDB**: Agregaciones eficientes para rankings
- **Archivos Locales (YML)**: Lectura optimizada de archivos

### Versiones de Minecraft

- **1.16.x - 1.21.x**: Completamente compatible
- **Paper/Spigot**: Funciona en ambas variantes

### Dependencias Requeridas

- **PlaceholderAPI**: Para el funcionamiento de los placeholders
- **MythicEconomy**: Plugin principal de economía

## Solución de Problemas

### Problemas Comunes

1. **Placeholders muestran "N/A"**
   - Verifica que la moneda exista y esté habilitada
   - Confirma que hay jugadores con balance en esa moneda
   - Espera 1-2 segundos para que el sistema detecte nuevas monedas automáticamente
   - Revisa el console para errores

2. **Datos no se actualizan**
   - El sistema se actualiza automáticamente cada segundo
   - Verifica que PlaceholderAPI esté habilitado
   - Comprueba la conexión con la base de datos
   - Revisa que la moneda esté habilitada

3. **Alto uso de CPU**
   - El sistema está optimizado para bajo impacto
   - Si experimentas problemas, reduce el tamaño del cache modificando el código
   - Aumenta el intervalo de actualización si es necesario

### Depuración Automática

El sistema incluye logs automáticos para facilitar la depuración:

```
[MythicEconomy] LeaderboardCache iniciado
[MythicEconomy] Nueva moneda detectada en leaderboard: currency_name
[MythicEconomy] Moneda removida del leaderboard: currency_name
[MythicEconomy] Error al actualizar leaderboards: error_message
```

### Verificación de Funcionamiento

Para verificar que el sistema funciona correctamente:
1. Usa un placeholder en un scoreboard o chat
2. Espera 1-2 segundos
3. Los datos deberían aparecer automáticamente

Si los placeholders no funcionan, revisa:
- Que PlaceholderAPI esté instalado y habilitado
- Que MythicEconomy esté funcionando correctamente
- Los logs del servidor para mensajes de error

## API para Desarrolladores

### Acceso Programático al Cache

Puedes acceder al sistema de cache desde otros plugins:

```java
// Obtener instancia del cache
LeaderboardCache cache = mythicEconomyPlugin.getPlaceholders().getLeaderboardCache();

// Obtener datos del leaderboard (actualizados automáticamente)
String playerName = cache.getPlayerName("default", 1);
String balance = cache.getPlayerBalance("default", 1);
String uuid = cache.getPlayerUuid("default", 1);

// Verificar si hay datos
boolean hasData = cache.hasCurrencyData("gems");

// El sistema se actualiza automáticamente, no necesitas forzar actualización
```

### Características de la API

- **Thread-Safe**: Todos los métodos son seguros para usar desde cualquier hilo
- **Datos en Tiempo Real**: Los datos siempre están actualizados (máximo 1 segundo de retraso)
- **Auto-Detección**: Nuevas monedas se detectan y agregan automáticamente
- **Sin Mantenimiento**: No necesitas gestionar el ciclo de vida del cache

### Integración con Plugins Web

Puedes usar la API para crear endpoints web:

```java
// Ejemplo para crear endpoint JSON
String jsonResponse = String.format(
    "{\"player\":\"%s\",\"balance\":\"%s\",\"uuid\":\"%s\"}",
    cache.getPlayerName("default", 1),
    cache.getPlayerBalance("default", 1),
    cache.getPlayerUuid("default", 1)
);
```

## Actualizaciones Futuras

### Características Planificadas

- **Placeholders de tendencias**: Mostrar cambios en el ranking
- **Historial de rankings**: Acceso a datos históricos
- **Filtros avanzados**: Leaderboards por período de tiempo
- **Integración con Discord**: Envío automático de rankings
- **API REST**: Endpoint para consultas externas

## Contribuciones

Para reportar bugs o solicitar características, visita el repositorio del proyecto.

---

**Nota**: Este sistema está diseñado para ser completamente automático, eficiente y escalable. No requiere configuración ni mantenimiento manual. Si experimentas problemas de rendimiento, ajusta los parámetros de cache según las necesidades de tu servidor.