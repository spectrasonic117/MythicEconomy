# MythicEconomy API - Documentación para Desarrolladores

## Introducción

MythicEconomy proporciona una API simple y fácil de usar para que otros plugins puedan interactuar con el sistema de economía. Esta API permite agregar, quitar, transferir y consultar dinero de los jugadores de forma segura y eficiente.

## Instalación y Configuración

### 1. Dependencia en tu plugin

Agrega MythicEconomy como dependencia en tu `plugin.yml`:

```yaml
depend: [MythicEconomy]
# o si es opcional:
softdepend: [MythicEconomy]
```

### 2. Dependencia Local (Maven)

Para incluir MythicEconomy como dependencia local desde tu carpeta `lib`, agrega esto a tu `pom.xml`:

```xml
<!-- Dependencia local de MythicEconomy -->
<dependency>
    <groupId>com.spectrasonic</groupId>
    <artifactId>MythicEconomy</artifactId>
    <version>1.1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/MythicEconomy-*</systemPath>
</dependency>
```

**Estructura del proyecto:**
```
MiPlugin/
├── lib/
│   └── MythicEconomy-1.1.0.jar    # Coloca el JAR aquí
├── src/main/java/
├── pom.xml
└── ...
```

### 3. Dependencia Local (Gradle)

Si usas Gradle, agrega esto a tu `build.gradle`:

```gradle
dependencies {
    compileOnly files('lib/MythicEconomy-1.1.0.jar')
    // otras dependencias...
}
```

## Uso Básico

### Obtener la instancia de la API

```java
import com.spectrasonic.MythicEconomy.api.MythicEconomyAPI;

public class MiPlugin extends JavaPlugin {
    
    private MythicEconomyAPI economyAPI;
    
    @Override
    public void onEnable() {
        // Verificar si MythicEconomy está disponible
        if (!MythicEconomyAPI.isAvailable()) {
            getLogger().severe("MythicEconomy no está disponible!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Obtener la instancia de la API
        this.economyAPI = MythicEconomyAPI.getInstance();
        getLogger().info("MythicEconomy API cargada correctamente!");
    }
}
```

## Métodos Principales

### 1. Consultar Balance

```java
// Obtener el balance de un jugador
Player player = // ... obtener jugador
double balance = economyAPI.getBalance(player);
player.sendMessage("Tu balance es: " + economyAPI.formatMoney(balance));
```

### 2. Agregar Dinero

```java
// Agregar dinero a un jugador
Player player = // ... obtener jugador
double amount = 100.0;

if (economyAPI.addMoney(player, amount)) {
    player.sendMessage("¡Se han agregado " + economyAPI.formatMoney(amount) + " a tu cuenta!");
} else {
    player.sendMessage("Error al agregar dinero.");
}
```

### 3. Quitar Dinero

```java
// Quitar dinero a un jugador
Player player = // ... obtener jugador
double amount = 50.0;

if (economyAPI.removeMoney(player, amount)) {
    player.sendMessage("Se han deducido " + economyAPI.formatMoney(amount) + " de tu cuenta.");
} else {
    player.sendMessage("No tienes suficiente dinero.");
}
```

### 4. Verificar si tiene suficiente dinero

```java
// Verificar si un jugador puede pagar algo
Player player = // ... obtener jugador
double cost = 25.0;

if (economyAPI.canPay(player, cost)) {
    // El jugador puede pagar
    economyAPI.removeMoney(player, cost);
    player.sendMessage("Compra realizada!");
} else {
    player.sendMessage("No tienes suficiente dinero. Necesitas " + 
                      economyAPI.formatMoney(cost));
}
```

### 5. Transferir Dinero

```java
// Transferir dinero entre jugadores
Player sender = // ... jugador que envía
Player receiver = // ... jugador que recibe
double amount = 75.0;

if (economyAPI.transferMoney(sender, receiver, amount)) {
    sender.sendMessage("Has enviado " + economyAPI.formatMoney(amount) + 
                      " a " + receiver.getName());
    receiver.sendMessage("Has recibido " + economyAPI.formatMoney(amount) + 
                        " de " + sender.getName());
} else {
    sender.sendMessage("No tienes suficiente dinero para enviar.");
}
```

### 6. Establecer Balance

```java
// Establecer un balance específico
Player player = // ... obtener jugador
double newBalance = 500.0;

if (economyAPI.setBalance(player, newBalance)) {
    player.sendMessage("Tu balance ha sido establecido a " + 
                      economyAPI.formatMoney(newBalance));
}
```

## Métodos Avanzados

### Quitar dinero forzado (permitir balance negativo)

```java
// Quitar dinero incluso si el jugador no tiene suficiente
Player player = // ... obtener jugador
double amount = 1000.0;
boolean force = true; // Permitir balance negativo

economyAPI.removeMoney(player, amount, force);
```

### Agregar dinero con validación personalizada

```java
// Agregar dinero permitiendo cantidades negativas (equivale a quitar)
Player player = // ... obtener jugador
double amount = -50.0; // Cantidad negativa
boolean allowNegative = true;

economyAPI.addMoney(player, amount, allowNegative);
```

## Información del Sistema

### Obtener información de la moneda

```java
// Obtener información sobre la moneda configurada
String symbol = economyAPI.getCurrencySymbol(); // "$"
String name = economyAPI.getCurrencyName(); // "monedas"
String singular = economyAPI.getCurrencyNameSingular(); // "moneda"

// Formatear dinero
double amount = 123.45;
String formatted = economyAPI.formatMoney(amount); // "$123.45"
```

### Estadísticas del servidor

```java
// Obtener estadísticas del sistema económico
double totalMoney = economyAPI.getTotalMoney(); // Total de dinero en circulación
int totalAccounts = economyAPI.getTotalAccounts(); // Número de cuentas
double startingBalance = economyAPI.getStartingBalance(); // Balance inicial

getLogger().info("Dinero total en circulación: " + economyAPI.formatMoney(totalMoney));
getLogger().info("Cuentas totales: " + totalAccounts);
```

## Eventos de la API

MythicEconomy dispara eventos personalizados que puedes escuchar en tu plugin:

### 1. Evento de Agregar Dinero

```java
import com.spectrasonic.MythicEconomy.api.events.MoneyAddEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EconomyListener implements Listener {
    
    @EventHandler
    public void onMoneyAdd(MoneyAddEvent event) {
        Player player = event.getPlayer();
        double amount = event.getAmount();
        double oldBalance = event.getOldBalance();
        double newBalance = event.getNewBalance();
        
        // Hacer algo cuando se agrega dinero
        getLogger().info(player.getName() + " recibió " + amount + " monedas");
        
        // Cancelar el evento si es necesario
        // event.setCancelled(true);
    }
}
```

### 2. Evento de Quitar Dinero

```java
import com.spectrasonic.MythicEconomy.api.events.MoneyRemoveEvent;

@EventHandler
public void onMoneyRemove(MoneyRemoveEvent event) {
    Player player = event.getPlayer();
    double amount = event.getAmount();
    
    // Hacer algo cuando se quita dinero
    getLogger().info(player.getName() + " perdió " + amount + " monedas");
}
```

### 3. Evento de Transferencia

```java
import com.spectrasonic.MythicEconomy.api.events.MoneyTransferEvent;

@EventHandler
public void onMoneyTransfer(MoneyTransferEvent event) {
    Player from = event.getFrom();
    Player to = event.getTo();
    double amount = event.getAmount();
    
    // Hacer algo cuando se transfiere dinero
    getLogger().info(from.getName() + " envió " + amount + " monedas a " + to.getName());
    
    // Cancelar la transferencia si es necesario
    // event.setCancelled(true);
}
```

## Ejemplos Completos

### Ejemplo 1: Sistema de Tienda Simple

```java
public class TiendaSimple implements Listener {
    
    private MythicEconomyAPI economyAPI;
    
    public TiendaSimple() {
        this.economyAPI = MythicEconomyAPI.getInstance();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            
            if (block.getType() == Material.EMERALD_BLOCK) {
                Player player = event.getPlayer();
                double cost = 100.0;
                
                if (economyAPI.canPay(player, cost)) {
                    economyAPI.removeMoney(player, cost);
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                    player.sendMessage("¡Has comprado un diamante por " + 
                                      economyAPI.formatMoney(cost) + "!");
                } else {
                    player.sendMessage("Necesitas " + economyAPI.formatMoney(cost) + 
                                      " para comprar un diamante.");
                }
            }
        }
    }
}
```

### Ejemplo 2: Sistema de Recompensas

```java
public class SistemaRecompensas implements Listener {
    
    private MythicEconomyAPI economyAPI;
    
    public SistemaRecompensas() {
        this.economyAPI = MythicEconomyAPI.getInstance();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        double dailyReward = 50.0;
        
        // Dar recompensa diaria
        economyAPI.addMoney(player, dailyReward);
        player.sendMessage("¡Recompensa diaria! +" + 
                          economyAPI.formatMoney(dailyReward));
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.DIAMOND_ORE) {
            Player player = event.getPlayer();
            double reward = 25.0;
            
            economyAPI.addMoney(player, reward);
            player.sendMessage("¡Has ganado " + economyAPI.formatMoney(reward) + 
                              " por minar diamante!");
        }
    }
}
```

## Mejores Prácticas

### 1. Verificar disponibilidad

Siempre verifica que MythicEconomy esté disponible antes de usar la API:

```java
if (!MythicEconomyAPI.isAvailable()) {
    // Manejar el caso donde MythicEconomy no está disponible
    return;
}
```

### 2. Manejar errores

Los métodos de la API devuelven `boolean` para indicar éxito o fallo:

```java
if (!economyAPI.addMoney(player, amount)) {
    // Manejar el error
    player.sendMessage("Error al procesar la transacción.");
}
```

### 3. Usar eventos para logging

Escucha los eventos de la API para mantener logs de transacciones:

```java
@EventHandler
public void onMoneyAdd(MoneyAddEvent event) {
    // Log de transacciones para auditoría
    logTransaction(event.getPlayer(), "ADD", event.getAmount());
}
```

### 4. Formatear cantidades

Usa siempre `formatMoney()` para mostrar cantidades a los jugadores:

```java
// ✅ Correcto
player.sendMessage("Balance: " + economyAPI.formatMoney(balance));

// ❌ Incorrecto
player.sendMessage("Balance: $" + balance);
```

## Soporte y Contacto

- **Autor**: Spectrasonic
- **Versión**: 1.1.0
- **GitHub**: https://github.com/spectrasonic
- **Plugin**: MythicEconomy

Para reportar bugs o solicitar características, contacta al desarrollador o crea un issue en el repositorio del proyecto.