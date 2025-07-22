# ✅ MangoEconomy - Placeholders Implementados

## 🎯 Resumen de la Implementación

Se ha implementado exitosamente el sistema de placeholders para MangoEconomy con PlaceholderAPI. El plugin ahora incluye:

### 📁 Archivos Creados/Modificados:

1. **`MangoEconomyPlaceholders.java`** - Nueva clase de placeholders
2. **`Main.java`** - Modificado para integrar PlaceholderAPI
3. **`PLACEHOLDERS.md`** - Documentación completa de placeholders

### ⚙️ Configuración:

- **Prefijo:** `mangoeco_`
- **Actualización:** Cada 10 ticks (0.5 segundos)
- **Dependencia:** PlaceholderAPI ya está configurada en `plugin.yml`

### 🔧 Funcionalidades Implementadas:

#### Placeholders Básicos de Dinero:
- `%mangoeco_money%` - Dinero formateado (ej: $1,250.50)
- `%mangoeco_money_raw%` - Solo número (ej: 1250.50)
- `%mangoeco_money_formatted%` - Con separadores de miles
- `%mangoeco_money_short%` - Formato corto (K, M, B, T)

#### Placeholders de Configuración:
- `%mangoeco_currency_symbol%` - Símbolo de moneda
- `%mangoeco_currency_name%` - Nombre de moneda (plural)
- `%mangoeco_currency_name_singular%` - Nombre de moneda (singular)
- `%mangoeco_starting_balance%` - Saldo inicial
- `%mangoeco_vault_enabled%` - Estado de Vault

#### Placeholders de Estadísticas:
- `%mangoeco_total_money%` - Total en circulación
- `%mangoeco_total_money_short%` - Total en formato corto
- `%mangoeco_total_accounts%` - Número de cuentas
- `%mangoeco_rank%` - Posición en ranking

#### Placeholders de Ranking:
- `%mangoeco_top_1_player%` / `%mangoeco_top_1_money%`
- `%mangoeco_top_2_player%` / `%mangoeco_top_2_money%`
- `%mangoeco_top_3_player%` / `%mangoeco_top_3_money%`

#### Placeholders Dinámicos:
- `%mangoeco_top_<número>_player%` - Jugador en posición X
- `%mangoeco_top_<número>_money%` - Dinero en posición X
- `%mangoeco_can_pay_<cantidad>%` - Verificar si puede pagar

### 🚀 Características Técnicas:

1. **Integración Automática:** Se registra automáticamente si PlaceholderAPI está presente
2. **Manejo de Errores:** Gestión robusta de jugadores offline y errores
3. **Rendimiento:** Actualización eficiente cada 10 ticks
4. **Compatibilidad:** Funciona con jugadores online y offline
5. **Escalabilidad:** Placeholders dinámicos para cualquier posición o cantidad

### 📊 Total de Placeholders:

- **19 placeholders básicos fijos**
- **Placeholders dinámicos ilimitados** (top_X, can_pay_X)

### 🎮 Uso en el Servidor:

Los placeholders se pueden usar en:
- Chat y mensajes
- Scoreboards y TAB lists
- Hologramas
- GUIs y menús
- Cualquier plugin compatible con PlaceholderAPI

### ✅ Estado del Proyecto:

- ✅ Compilación exitosa
- ✅ Integración con PlaceholderAPI
- ✅ Documentación completa
- ✅ Manejo de errores
- ✅ Placeholders dinámicos
- ✅ Actualización automática

### 📝 Próximos Pasos:

1. Instalar PlaceholderAPI en el servidor
2. Colocar el JAR de MangoEconomy en la carpeta plugins
3. Reiniciar el servidor
4. Los placeholders estarán disponibles automáticamente

### 🔍 Verificación:

Para verificar que los placeholders funcionan:
```
/papi parse <jugador> %mangoeco_money%
```

¡El sistema de placeholders está completamente implementado y listo para usar! 🎉