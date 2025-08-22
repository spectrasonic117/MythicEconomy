# MythicEconomy - Lista de Placeholders

Este documento contiene todos los placeholders disponibles para MythicEconomy con PlaceholderAPI.

**Prefijo:** `eco_`  
**Actualización:** Cada 10 ticks (0.5 segundos)

## 📊 Placeholders Básicos de Dinero

| Placeholder | Descripción | Ejemplo de Salida |
|-------------|-------------|-------------------|
| `%eco_money%` | Dinero del jugador con formato | `$1,250.50` |
| `%eco_money_raw%` | Dinero del jugador sin formato (solo número) | `1250.50` |
| `%eco_money_formatted%` | Dinero con separadores de miles | `$1,250.50` |
| `%eco_money_short%` | Dinero en formato corto (K, M, B, T) | `$1.3K` |

## 💰 Placeholders de Moneda

| Placeholder | Descripción | Ejemplo de Salida |
|-------------|-------------|-------------------|
| `%eco_currency_symbol%` | Símbolo de la moneda | ` |
| `%eco_currency_name%` | Nombre de la moneda (plural) | `monedas` |
| `%eco_currency_name_singular%` | Nombre de la moneda (singular) | `moneda` |

## 🎯 Placeholders de Configuración

| Placeholder | Descripción | Ejemplo de Salida |
|-------------|-------------|-------------------|
| `%eco_starting_balance%` | Saldo inicial para nuevos jugadores | `$100.00` |
| `%eco_vault_enabled%` | Estado de la integración con Vault | `Habilitado` |

## 📈 Placeholders de Estadísticas Globales

| Placeholder | Descripción | Ejemplo de Salida |
|-------------|-------------|-------------------|
| `%eco_total_money%` | Total de dinero en circulación | `$50,000.00` |
| `%eco_total_money_short%` | Total de dinero en formato corto | `$50.0K` |
| `%eco_total_accounts%` | Número total de cuentas | `25` |

## 🏆 Placeholders de Ranking

| Placeholder | Descripción | Ejemplo de Salida |
|-------------|-------------|-------------------|
| `%eco_rank%` | Posición del jugador en el ranking | `3` |
| `%eco_top_1_player%` | Nombre del jugador más rico | `Spectrasonic` |
| `%eco_top_1_money%` | Dinero del jugador más rico | `$10,000.00` |
| `%eco_top_2_player%` | Nombre del segundo jugador más rico | `Player2` |
| `%eco_top_2_money%` | Dinero del segundo jugador más rico | `$8,500.00` |
| `%eco_top_3_player%` | Nombre del tercer jugador más rico | `Player3` |
| `%eco_top_3_money%` | Dinero del tercer jugador más rico | `$7,200.00` |

## 🔢 Placeholders Dinámicos de Top

Puedes usar cualquier número para obtener la posición específica en el ranking:

| Placeholder | Descripción | Ejemplo |
|-------------|-------------|---------|
| `%eco_top_<número>_player%` | Jugador en la posición X | `%eco_top_5_player%` |
| `%eco_top_<número>_money%` | Dinero del jugador en posición X | `%eco_top_5_money%` |

## 💳 Placeholders de Verificación de Pago

| Placeholder | Descripción | Ejemplo de Uso |
|-------------|-------------|----------------|
| `%eco_can_pay_<cantidad>%` | Verifica si el jugador puede pagar X cantidad | `%eco_can_pay_100%` |

**Ejemplos:**
- `%eco_can_pay_50%` → `Sí` o `No`
- `%eco_can_pay_1000.50%` → `Sí` o `No`

## 📝 Notas de Uso

1. **Actualización automática:** Todos los placeholders se actualizan cada 10 ticks (0.5 segundos)
2. **Jugadores offline:** Algunos placeholders requieren que el jugador esté online (como `money`, `rank`, etc.)
3. **Formato de números:** Los placeholders con formato usan la configuración de moneda del plugin
4. **Placeholders dinámicos:** Los placeholders de top y verificación de pago son dinámicos y aceptan cualquier número válido

## 🔧 Configuración

Los placeholders utilizan la configuración definida en `config.yml`:

```yaml
economy:
  starting-balance: 100.0
  currency:
    symbol: "$"
    name: "monedas"
    name-singular: "moneda"
```

## 📋 Lista Completa de Placeholders

### Placeholders Básicos (17 placeholders):
1. `%eco_money%`
2. `%eco_money_raw%`
3. `%eco_money_formatted%`
4. `%eco_money_short%`
5. `%eco_currency_symbol%`
6. `%eco_currency_name%`
7. `%eco_currency_name_singular%`
8. `%eco_starting_balance%`
9. `%eco_total_money%`
10. `%eco_total_money_short%`
11. `%eco_total_accounts%`
12. `%eco_rank%`
13. `%eco_top_1_player%`
14. `%eco_top_1_money%`
15. `%eco_top_2_player%`
16. `%eco_top_2_money%`
17. `%eco_top_3_player%`
18. `%eco_top_3_money%`
19. `%eco_vault_enabled%`

### Placeholders Dinámicos (ilimitados):
- `%eco_top_<número>_player%` (para cualquier posición)
- `%eco_top_<número>_money%` (para cualquier posición)
- `%eco_can_pay_<cantidad>%` (para cualquier cantidad)

**Total:** 19 placeholders básicos + placeholders dinámicos ilimitados