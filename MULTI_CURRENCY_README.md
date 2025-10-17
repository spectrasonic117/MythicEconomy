# Sistema de Múltiples Monedas - MythicEconomy

## Descripción

El sistema de múltiples monedas permite al plugin MythicEconomy manejar diferentes tipos de monedas con configuraciones independientes. Cada moneda puede tener su propio símbolo, nombre, formato decimal, límites y reglas de transferencia.

## Características

- ✅ Múltiples monedas independientes
- ✅ Configuración por moneda individual
- ✅ Símbolos personalizables
- ✅ Soporte para decimales opcional
- ✅ Límites de balance y transferencia por moneda
- ✅ Comandos específicos para cada moneda
- ✅ Placeholders diferenciados por moneda
- ✅ API completa para desarrolladores
- ✅ Compatibilidad hacia atrás con moneda existente

## Comandos

### Gestión de Monedas

#### `/currency list`
Lista todas las monedas disponibles en el servidor.

**Permisos:** `MythicEconomy.currency.admin`

#### `/currency add <id> <name> <nameSingular> <symbol> <decimal>`
Agrega una nueva moneda al sistema.

**Parámetros:**
- `id`: Identificador único de la moneda
- `name`: Nombre en plural (ej: "monedas")
- `nameSingular`: Nombre en singular (ej: "moneda")
- `symbol`: Símbolo de la moneda (ej: "$", "💎", "🪙")
- `decimal`: "true" para usar decimales, "false" para números enteros

**Permisos:** `MythicEconomy.currency.admin`

**Ejemplo:**
```bash
/currency add gems gemas gema 💎 false
```

#### `/currency remove <id>`
Remueve una moneda del sistema (excepto la moneda por defecto).

**Parámetros:**
- `id`: ID de la moneda a remover

**Permisos:** `MythicEconomy.currency.admin`

#### `/currency info <id>`
Muestra información detallada de una moneda específica.

**Parámetros:**
- `id`: ID de la moneda

**Permisos:** `MythicEconomy.currency.admin`

#### `/currency reload`
Recarga todas las configuraciones de monedas.

**Permisos:** `MythicEconomy.currency.admin`

### Balances de Monedas

#### `/balcur <currency> [player]`
Muestra el balance de una moneda específica.

**Parámetros:**
- `currency`: ID de la moneda
- `[player]`: Jugador objetivo (opcional)

**Permisos:**
- `MythicEconomy.balance.currency` - Ver propio balance
- `MythicEconomy.balance.currency.others` - Ver balance de otros jugadores

**Ejemplos:**
```bash
/balcur gems                    # Ver tus gemas
/balcur coins Steve             # Ver coins de Steve
/balancecur gems                # Alias alternativo
```

## Placeholders (PlaceholderAPI)

### Placeholders de Moneda por Defecto (compatibilidad hacia atrás)
- `%eco_money%` - Balance formateado
- `%eco_money_raw%` - Balance sin formato
- `%eco_currency_symbol%` - Símbolo de moneda
- `%eco_currency_name%` - Nombre de moneda

### Placeholders para Monedas Específicas
- `%eco_<currency>_money%` - Balance formateado en moneda específica
- `%eco_<currency>_money_raw%` - Balance sin formato en moneda específica
- `%eco_<currency>_symbol%` - Símbolo de moneda específica
- `%eco_<currency>_name%` - Nombre de moneda específica

**Ejemplos:**
```bash
%eco_default_money%      # Balance en moneda por defecto
%eco_gems_money%         # Balance en gemas
%eco_coins_symbol%       # Símbolo de coins (🪙)
```

## API para Desarrolladores

### Ejemplos de Uso

```java
import com.spectrasonic.MythicEconomy.api.MythicEconomyAPI;
import com.spectrasonic.MythicEconomy.models.Currency;

// Verificar si la API está disponible
if (MythicEconomyAPI.isAvailable()) {
    MythicEconomyAPI api = MythicEconomyAPI.getInstance();

    // Trabajar con moneda por defecto (compatibilidad hacia atrás)
    Player player = ...;
    double balance = api.getBalance(player);
    api.addMoney(player, 100.0);

    // Trabajar con monedas específicas
    String currencyId = "gems";

    // Verificar si la moneda existe
    if (api.currencyExists(currencyId)) {
        // Obtener información de la moneda
        Currency currency = api.getCurrency(currencyId);

        // Operaciones con moneda específica
        double gemsBalance = api.getBalance(player, currencyId);
        api.addMoney(player, 50.0, currencyId);

        // Formatear cantidades
        String formatted = api.formatMoney(150.0, currencyId);

        // Transferir entre jugadores
        Player target = ...;
        api.transferMoney(player, target, 25.0, currencyId);
    }
}
```

## Configuración de Monedas

Cada moneda tiene su propio archivo de configuración en el directorio `currencies/`.

### Ejemplo de configuración (currencies/gems.yml):
```yaml
# Configuración de la moneda Gems (sin decimales)
id: "gems"
name: "gemas"
name-singular: "gema"
symbol: "💎"
decimal: false
starting-balance: 10.0
max-balance: 999999.0
min-transfer: 1.0
max-transfer: 10000.0
enabled: true
```

### Parámetros de Configuración

- `id`: Identificador único de la moneda
- `name`: Nombre en plural
- `name-singular`: Nombre en singular
- `symbol`: Símbolo de la moneda
- `decimal`: `true` para usar decimales, `false` para números enteros
- `starting-balance`: Balance inicial para nuevos jugadores
- `max-balance`: Balance máximo permitido
- `min-transfer`: Cantidad mínima para transferencias
- `max-transfer`: Cantidad máxima para transferencias
- `enabled`: `true` para habilitar, `false` para deshabilitar

## Configuración Principal

En `config.yml`:
```yaml
# Configuración de múltiples monedas
multi-currency:
  # Habilitar sistema de múltiples monedas
  enabled: true

  # Moneda por defecto del servidor (para compatibilidad hacia atrás)
  default-currency: "default"

  # Permitir a los jugadores usar comandos de múltiples monedas
  allow-player-commands: true

  # Directorio donde se almacenan las configuraciones de monedas
  currencies-directory: "currencies"
```

## Mejores Prácticas

1. **Planificación**: Define claramente el propósito de cada moneda antes de crearlas
2. **Límites apropiados**: Establece límites realistas según el propósito de cada moneda
3. **Símbolos únicos**: Usa símbolos distintivos para evitar confusiones
4. **Permisos**: Configura permisos apropiados para comandos administrativos
5. **Backups**: Realiza backups antes de modificar configuraciones de monedas

## Ejemplos de Uso

### Monedas para diferentes propósitos:
- **Moneda principal**: `$` para economía general
- **Gemas**: `💎` para sistema de votación/recompensas
- **Coins**: `🪙` para tienda premium
- **Puntos**: `⭐` para sistema de logros

### Integración con otros plugins:
```java
// Ejemplo: Recompensar con gemas por votar
public void rewardVoting(Player player) {
    MythicEconomyAPI api = MythicEconomyAPI.getInstance();
    api.addMoney(player, 10.0, "gems");
}
```

## Solución de Problemas

### Problema: Los comandos no funcionan
**Solución:** Verifica que el jugador tenga los permisos adecuados.

### Problema: Los placeholders no funcionan
**Solución:** Asegúrate de que PlaceholderAPI esté instalado y el jugador esté online.

### Problema: Las monedas no aparecen
**Solución:** Usa `/currency reload` para recargar las configuraciones.

### Problema: Error al agregar moneda
**Solución:** Verifica que el ID sea único y no contenga caracteres especiales.