# MangoEconomy API

## Resumen

MangoEconomy ahora incluye una **API completa y fácil de usar** que permite a otros plugins interactuar con el sistema de economía de forma segura y eficiente.

## Características de la API

### ✅ **Funcionalidades Principales**
- ➕ **Agregar dinero** a jugadores
- ➖ **Quitar dinero** de jugadores  
- 💰 **Consultar balances** de jugadores
- 🔄 **Transferir dinero** entre jugadores
- ⚙️ **Establecer balances** específicos
- 📊 **Obtener estadísticas** del sistema

### ✅ **Características Avanzadas**
- 🎯 **Eventos personalizados** (MoneyAddEvent, MoneyRemoveEvent, MoneyTransferEvent)
- 🛡️ **Validaciones de seguridad** automáticas
- 🎨 **Formateo automático** de cantidades
- 📈 **Sistema de logging** integrado
- 🔧 **Fácil integración** con otros plugins

## Archivos Creados

### 📁 **Packages de la API**
```
src/main/java/com/spectrasonic/MangoEconomy/api/
├── MangoEconomyAPI.java           # Clase principal de la API
└── events/
    ├── EconomyEvent.java          # Evento base
    ├── MoneyAddEvent.java         # Evento de agregar dinero
    ├── MoneyRemoveEvent.java      # Evento de quitar dinero
    └── MoneyTransferEvent.java    # Evento de transferencia
```

### 📚 **Documentación**
- `API_DOCUMENTATION.md` - Documentación completa de la API
- `EXAMPLE_PLUGIN.md` - Ejemplo completo de plugin usando la API
- `README_API.md` - Este archivo de resumen

## Uso Rápido

### 1. **Obtener la API**
```java
// Verificar disponibilidad
if (MangoEconomyAPI.isAvailable()) {
    MangoEconomyAPI api = MangoEconomyAPI.getInstance();
}
```

### 2. **Operaciones Básicas**
```java
// Agregar dinero
api.addMoney(player, 100.0);

// Quitar dinero
api.removeMoney(player, 50.0);

// Consultar balance
double balance = api.getBalance(player);

// Verificar si puede pagar
if (api.canPay(player, 25.0)) {
    // El jugador puede pagar
}
```

### 3. **Transferencias**
```java
// Transferir dinero entre jugadores
api.transferMoney(sender, receiver, 75.0);
```

### 4. **Eventos**
```java
@EventHandler
public void onMoneyAdd(MoneyAddEvent event) {
    Player player = event.getPlayer();
    double amount = event.getAmount();
    // Hacer algo cuando se agrega dinero
}
```

## Ventajas para Desarrolladores

### 🚀 **Simplicidad**
- Una sola clase principal (`MangoEconomyAPI`)
- Métodos intuitivos y bien documentados
- Manejo automático de errores

### 🔒 **Seguridad**
- Validaciones automáticas de cantidades
- Eventos cancelables para control adicional
- Protección contra valores negativos

### 🎯 **Flexibilidad**
- Métodos con parámetros opcionales
- Soporte para operaciones forzadas
- Integración con eventos de Bukkit

### 📊 **Información Completa**
- Estadísticas del servidor
- Información de moneda configurada
- Formateo automático de cantidades

## Integración en tu Plugin

### **plugin.yml**
```yaml
depend: [MangoEconomy]
# o para dependencia opcional:
softdepend: [MangoEconomy]
```

### **Clase Principal**
```java
public class MiPlugin extends JavaPlugin {
    private MangoEconomyAPI economyAPI;
    
    @Override
    public void onEnable() {
        if (MangoEconomyAPI.isAvailable()) {
            this.economyAPI = MangoEconomyAPI.getInstance();
            getLogger().info("MangoEconomy API cargada!");
        }
    }
}
```

## Compatibilidad

- ✅ **Bukkit/Spigot/Paper** 1.21+
- ✅ **Java** 21+
- ✅ **Vault** (opcional, para compatibilidad con otros plugins)
- ✅ **PlaceholderAPI** (opcional, para placeholders)

## Próximas Características

- 🔄 **Soporte para múltiples monedas**
- 💳 **Sistema de cuentas bancarias**
- 📈 **Historial de transacciones**
- 🏪 **API de tiendas integrada**

## Soporte

Para obtener ayuda con la API:

1. 📖 Lee la documentación completa en `API_DOCUMENTATION.md`
2. 👀 Revisa el ejemplo en `EXAMPLE_PLUGIN.md`
3. 🐛 Reporta bugs o solicita características
4. 💬 Contacta al desarrollador: **Spectrasonic**

---

**¡La API de MangoEconomy hace que integrar un sistema de economía en tu plugin sea súper fácil!** 🎉