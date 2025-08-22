# MythicEconomy Plugin

Un sistema de economía completo y moderno para servidores de Minecraft usando CommandAPI y MiniMessage.

## 🚀 Características

- **Sistema de economía completamente independiente** - No requiere otros plugins
- **Compatibilidad con Vault API** - Otros plugins pueden usar MythicEconomy
- **Comandos intuitivos** usando CommandAPI
- **Mensajes coloridos** con soporte MiniMessage
- **Sistema de permisos granular**
- **Validaciones de seguridad** para todas las transacciones
- **Almacenamiento en YAML** - Fácil de configurar y hacer backup
- **Estadísticas avanzadas** - Top jugadores, dinero en circulación, etc.
- **API completa** para desarrolladores
- **Configuración flexible** - Moneda personalizable, saldo inicial, etc.

## 📋 Comandos

### Comandos de Administración

#### `/economy` (Aliases: `/eco`, `/econ`)
Comando principal de administración de economía.

**Permisos:** `MythicEconomy.economy.admin`

**Subcomandos:**
- `/economy give <jugador> <cantidad>` - Dar dinero a un jugador
- `/economy take <jugador> <cantidad>` - Quitar dinero a un jugador  
- `/economy set <jugador> <cantidad>` - Establecer el saldo de un jugador
- `/economy balance <jugador>` - Ver el saldo de otro jugador
- `/economy reload` - Recargar configuración y datos
- `/economy top [cantidad]` - Ver top de jugadores más ricos
- `/economy stats` - Ver estadísticas del sistema de economía
- `/economy setstarting <cantidad>` - Establecer saldo inicial para nuevos jugadores

### Comandos de Jugadores

#### `/money` (Aliases: `/balance`, `/bal`)
Ver tu saldo actual.

**Permisos:** `MythicEconomy.money`

#### `/pay <jugador> <cantidad>` (Alias: `/send`)
Enviar dinero a otro jugador.

**Permisos:** `MythicEconomy.pay`

## 🔐 Permisos

### Permisos de Administración
- `MythicEconomy.*` - Acceso completo al plugin
- `MythicEconomy.economy.admin` - Acceso a todos los comandos de administración
- `MythicEconomy.economy.give` - Dar dinero a jugadores
- `MythicEconomy.economy.take` - Quitar dinero a jugadores
- `MythicEconomy.economy.set` - Establecer saldo de jugadores
- `MythicEconomy.economy.balance.others` - Ver saldo de otros jugadores
- `MythicEconomy.economy.reload` - Recargar configuración

### Permisos de Jugadores
- `MythicEconomy.money` - Ver tu propio saldo (default: true)
- `MythicEconomy.pay` - Enviar dinero a otros jugadores (default: true)

## ⚙️ Configuración

El plugin genera automáticamente un archivo `config.yml` con las siguientes opciones:

```yaml
economy:
  starting-balance: 100.0  # Saldo inicial para nuevos jugadores
  currency:
    symbol: "$"
    name: "monedas"
    name-singular: "moneda"
  limits:
    max-balance: 999999999.99
    min-transfer: 0.01
    max-transfer: 100000.0
```

## 📁 Estructura de Archivos

```
plugins/MythicEconomy/
├── config.yml          # Configuración principal
└── playerdata.yml       # Datos de saldos de jugadores
```

## 🛠️ Instalación

1. **Dependencias requeridas:**
   - CommandAPI plugin instalado en tu servidor
   - Servidor Paper/Spigot 1.21+

2. **Dependencias opcionales:**
   - Vault (para compatibilidad con otros plugins)

2. **Instalación:**
   - Descarga el archivo `MythicEconomy-1.0.0.jar`
   - Colócalo en la carpeta `plugins/` de tu servidor
   - Reinicia el servidor

## 💡 Ejemplos de Uso

### Para Administradores:
```
/economy give Steve 1000     # Dar 1000 monedas a Steve
/economy take Alex 500       # Quitar 500 monedas a Alex
/economy set Bob 2500        # Establecer el saldo de Bob a 2500
/economy balance Charlie     # Ver el saldo de Charlie
```

### Para Jugadores:
```
/money                       # Ver tu saldo
/pay Steve 100              # Enviar 100 monedas a Steve
/balance                    # Ver tu saldo (alias)
```

## 🔧 Características Técnicas

- **Almacenamiento:** Archivos YAML para fácil configuración y backup
- **Validaciones:** Verificación de fondos suficientes, cantidades válidas, etc.
- **Mensajes:** Sistema de mensajes coloridos con MiniMessage
- **Rendimiento:** Carga y guardado eficiente de datos
- **Seguridad:** Validaciones para prevenir transacciones inválidas

## 🎨 Mensajes del Sistema

El plugin utiliza MiniMessage para mensajes coloridos y atractivos:

- ✅ **Verde** para operaciones exitosas
- ❌ **Rojo** para errores
- 💰 **Amarillo** para cantidades de dinero
- 👤 **Aqua** para nombres de jugadores

## 🔄 API para Desarrolladores

### **API Nativa de MythicEconomy**
```java
// Obtener el manager de economía
EconomyManager economyManager = EconomyManager.getInstance();

// Operaciones básicas
double balance = economyManager.getBalance(player);
economyManager.addMoney(player, 100.0);
economyManager.removeMoney(player, 50.0);
economyManager.setBalance(player, 1000.0);

// Verificaciones
boolean hasEnough = economyManager.hasEnoughMoney(player, 100.0);
String formatted = economyManager.formatMoney(1234.56);
```

### **API de Vault (Recomendada para compatibilidad)**
```java
// Obtener el proveedor de economía de Vault
Economy economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

// Operaciones usando Vault API
EconomyResponse response = economy.withdrawPlayer(player, 100.0);
if (response.transactionSuccess()) {
    // Transacción exitosa
    double newBalance = response.balance;
}

// Depositar dinero
EconomyResponse depositResponse = economy.depositPlayer(player, 50.0);

// Verificar saldo
double balance = economy.getBalance(player);
boolean hasEnough = economy.has(player, 100.0);
```

### **Funcionalidades Avanzadas**
```java
// Obtener estadísticas del sistema
EconomyManager economyManager = EconomyManager.getInstance();
double totalMoney = economyManager.getTotalMoney();
int totalAccounts = economyManager.getTotalAccounts();

// Top de jugadores más ricos
Map<String, Double> topPlayers = economyManager.getTopBalances(10);

// Configuración de moneda
String symbol = economyManager.getCurrencySymbol();
String currencyName = economyManager.getCurrencyName();
double startingBalance = economyManager.getStartingBalance();

// Establecer saldo inicial
economyManager.setStartingBalance(500.0);
```

## 📝 Changelog

### v1.0.0
- ✨ Lanzamiento inicial
- ✅ Sistema de economía completo
- ✅ Comandos de administración y jugadores
- ✅ Sistema de permisos
- ✅ Almacenamiento en YAML
- ✅ Mensajes con MiniMessage
- ✅ Validaciones de seguridad

## 🤝 Soporte

Si encuentras algún problema o tienes sugerencias, por favor:
1. Revisa la configuración del plugin
2. Verifica que CommandAPI esté instalado
3. Consulta los logs del servidor para errores

## 📄 Licencia

Este plugin ha sido desarrollado por SpectraSonic para uso en servidores de Minecraft.

---

**¡Disfruta de tu nuevo sistema de economía! 💰**