# MythicEconomy API Documentation

This document provides a comprehensive guide on how to integrate and utilize the MythicEconomy plugin's API in your own Minecraft plugins.

## 1. Local API Import

To use the MythicEconomy API in your project, you'll need to add it as a dependency. Place the MythicEconomy plugin JAR file into a `libs` directory in your project's root folder.

### Maven

If you are using Maven, after placing the JAR in the `libs` folder, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.spectrasonic</groupId>
    <artifactId>MythicEconomy</artifactId>
    <version>1.4.0</version> <!-- Use the actual version of the plugin -->
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/MythicEconomy-1.4.0.jar</systemPath>
</dependency>
```
*(Note: Replace `MythicEconomy.jar` and `1.4.0` with the actual filename and version of the plugin JAR you are using. The `groupId` and `artifactId` can be arbitrary but should match the plugin's actual ones if known.)*

### Gradle

If you are using Gradle, after placing the JAR in the `libs` folder, add the following to your `build.gradle` file:

```groovy
dependencies {
    compileOnly files('libs/MythicEconomy-1.4.0')
}
```
*(Note: For older Gradle versions, `compileOnly` might be `provided` or `compile` with the `provided` plugin. Replace `MythicEconomy` and `1.0.0` with the actual artifact name and version of the plugin JAR you are using. The `name` should typically be the `artifactId` from the plugin's `pom.xml` or manifest.)*

For most Minecraft plugins, distributing the API JAR directly or using a custom repository for server-side dependencies is common.

## 2. API Usage

The MythicEconomy API is accessed via a singleton instance. Always check if the API is available before attempting to use it to avoid `IllegalStateException`s if the plugin hasn't loaded yet.

### Getting the API Instance

```java
import com.spectrasonic.MythicEconomy.api.MythicEconomyAPI;

public class MyPluginIntegration {

    private MythicEconomyAPI economyAPI;

    public void onEnable() {
        if (MythicEconomyAPI.isAvailable()) {
            economyAPI = MythicEconomyAPI.getInstance();
            // API is ready to use
        } else {
            // MythicEconomy is not loaded or not ready.
            // You might want to disable your plugin's economy features or log a warning.
            System.out.println("MythicEconomy API is not available! Disabling economy features.");
        }
    }

    // ... rest of your plugin logic
}
```

### 2.1 Single Currency Methods (Default Economy)

These methods interact with the server's primary or default currency configured in MythicEconomy.

#### Get Player Balance

Retrieves the balance of a specific player for the default currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized as shown above
public double getPlayerBalance(Player player) {
    return economyAPI.getBalance(player);
}
```

#### Set Player Balance

Sets the balance of a specific player to a given amount for the default currency. Returns `true` if successful, `false` otherwise (e.g., if amount is negative).

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean setPlayerBalance(Player player, double amount) {
    if (economyAPI.setBalance(player, amount)) {
        player.sendMessage("Your balance has been set to: " + economyAPI.formatMoney(amount));
        return true;
    } else {
        player.sendMessage("Failed to set balance. Amount must be non-negative.");
        return false;
    }
}
```

#### Add Money to Player

Adds a specified amount of money to a player's balance for the default currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean givePlayerMoney(Player player, double amount) {
    if (economyAPI.addMoney(player, amount)) {
        player.sendMessage("You received: " + economyAPI.formatMoney(amount));
        return true;
    } else {
        player.sendMessage("Failed to add money. Amount must be positive.");
        return false;
    }
}
```
There's also an overloaded `addMoney(Player player, double amount, boolean allowNegative)` method, which can be used if you intend to subtract money by passing a negative `amount` and `allowNegative` as `true`. However, it's generally clearer to use `removeMoney` for subtraction.

#### Remove Money from Player

Removes a specified amount of money from a player's balance for the default currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean takePlayerMoney(Player player, double amount) {
    if (economyAPI.removeMoney(player, amount)) {
        player.sendMessage("You paid: " + economyAPI.formatMoney(amount));
        return true;
    } else {
        player.sendMessage("Failed to remove money. Insufficient funds or invalid amount.");
        return false;
    }
}
```
An overloaded `removeMoney(Player player, double amount, boolean force)` method exists to forcefully set the balance, ignoring current funds, if `force` is true. Use with caution.

#### Check Player Funds

Checks if a player has a sufficient amount of the default currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean canAfford(Player player, double amount) {
    return economyAPI.hasEnoughMoney(player, amount);
}
```

#### Transfer Money Between Players

Transfers money from one player to another using the default currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean transferFunds(Player fromPlayer, Player toPlayer, double amount) {
    if (economyAPI.transferMoney(fromPlayer, toPlayer, amount)) {
        fromPlayer.sendMessage("Transferred " + economyAPI.formatMoney(amount) + " to " + toPlayer.getName());
        toPlayer.sendMessage("Received " + economyAPI.formatMoney(amount) + " from " + fromPlayer.getName());
        return true;
    } else {
        fromPlayer.sendMessage("Failed to transfer money. Check funds or amount.");
        return false;
    }
}
```

#### Format Money Amount

Formats a double amount into a human-readable string based on the default currency's settings (e.g., "1,000.00 Coins").

```java
// Assuming economyAPI is already initialized
public String formatAmount(double amount) {
    return economyAPI.formatMoney(amount);
}
```

#### Get Currency Information (Default)

Retrieves information about the default currency.

```java
// Assuming economyAPI is already initialized
public void printDefaultCurrencyInfo() {
    System.out.println("Default Currency Symbol: " + economyAPI.getCurrencySymbol());
    System.out.println("Default Currency Name: " + economyAPI.getCurrencyName());
    System.out.println("Default Currency Name (Singular): " + economyAPI.getCurrencyNameSingular());
    System.out.println("Starting Balance: " + economyAPI.getStartingBalance());
}
```

#### Global Economy Statistics

Retrieve global statistics for the default economy.

```java
// Assuming economyAPI is already initialized
public void printGlobalStats() {
    System.out.println("Total Money in circulation: " + economyAPI.formatMoney(economyAPI.getTotalMoney()));
    System.out.println("Total Player Accounts: " + economyAPI.getTotalAccounts());
}
```

## 3. Multi-Currency Methods

MythicEconomy supports multiple currencies. These methods allow you to interact with specific currencies using their unique `currencyId`.

#### Get Player Balance for Specific Currency

Retrieves the balance of a specific player for a given currency ID.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public double getPlayerCurrencyBalance(Player player, String currencyId) {
    if (economyAPI.currencyExists(currencyId)) {
        return economyAPI.getBalance(player, currencyId);
    } else {
        System.out.println("Currency ID '" + currencyId + "' does not exist.");
        return 0.0; // Or throw an exception, depending on your error handling
    }
}
```

#### Set Player Balance for Specific Currency

Sets the balance of a specific player to a given amount for a specific currency. Returns `true` if successful, `false` otherwise.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean setPlayerCurrencyBalance(Player player, String currencyId, double amount) {
    if (!economyAPI.currencyExists(currencyId)) {
        player.sendMessage("Currency ID '" + currencyId + "' does not exist.");
        return false;
    }
    if (economyAPI.setBalance(player, amount, currencyId)) {
        player.sendMessage("Your " + currencyId + " balance has been set to: " + economyAPI.formatMoney(amount, currencyId));
        return true;
    } else {
        player.sendMessage("Failed to set " + currencyId + " balance. Amount must be non-negative.");
        return false;
    }
}
```

#### Add Money for Specific Currency

Adds a specified amount of money to a player's balance for a specific currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean givePlayerCurrencyMoney(Player player, String currencyId, double amount) {
    if (!economyAPI.currencyExists(currencyId)) {
        player.sendMessage("Currency ID '" + currencyId + "' does not exist.");
        return false;
    }
    if (economyAPI.addMoney(player, amount, currencyId)) {
        player.sendMessage("You received " + economyAPI.formatMoney(amount, currencyId));
        return true;
    } else {
        player.sendMessage("Failed to add " + currencyId + " money. Amount must be positive.");
        return false;
    }
}
```

#### Remove Money for Specific Currency

Removes a specified amount of money from a player's balance for a specific currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean takePlayerCurrencyMoney(Player player, String currencyId, double amount) {
    if (!economyAPI.currencyExists(currencyId)) {
        player.sendMessage("Currency ID '" + currencyId + "' does not exist.");
        return false;
    }
    if (economyAPI.removeMoney(player, amount, currencyId)) {
        player.sendMessage("You paid " + economyAPI.formatMoney(amount, currencyId));
        return true;
    } else {
        player.sendMessage("Failed to remove " + currencyId + " money. Insufficient funds or invalid amount.");
        return false;
    }
}
```

#### Check Player Funds for Specific Currency

Checks if a player has a sufficient amount of a specific currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean canAffordCurrency(Player player, String currencyId, double amount) {
    if (!economyAPI.currencyExists(currencyId)) {
        System.out.println("Currency ID '" + currencyId + "' does not exist.");
        return false;
    }
    return economyAPI.hasEnoughMoney(player, amount, currencyId);
}
```

#### Transfer Money Between Players for Specific Currency

Transfers money from one player to another using a specific currency.

```java
import org.bukkit.entity.Player;

// Assuming economyAPI is already initialized
public boolean transferCurrencyFunds(Player fromPlayer, Player toPlayer, String currencyId, double amount) {
    if (!economyAPI.currencyExists(currencyId)) {
        fromPlayer.sendMessage("Currency ID '" + currencyId + "' does not exist.");
        return false;
    }
    if (economyAPI.transferMoney(fromPlayer, toPlayer, amount, currencyId)) {
        fromPlayer.sendMessage("Transferred " + economyAPI.formatMoney(amount, currencyId) + " to " + toPlayer.getName());
        toPlayer.sendMessage("Received " + economyAPI.formatMoney(amount, currencyId) + " from " + fromPlayer.getName());
        return true;
    } else {
        fromPlayer.sendMessage("Failed to transfer " + currencyId + " money. Check funds or amount.");
        return false;
    }
}
```

#### Format Money Amount for Specific Currency

Formats a double amount into a human-readable string based on the specific currency's settings.

```java
// Assuming economyAPI is already initialized
public String formatCurrencyAmount(double amount, String currencyId) {
    if (economyAPI.currencyExists(currencyId)) {
        return economyAPI.formatMoney(amount, currencyId);
    } else {
        return "Unknown Currency";
    }
}
```

#### Get Currency Object

Retrieves the `Currency` object for a given `currencyId`. This object contains details like name, symbol, and singular name.

```java
import com.spectrasonic.MythicEconomy.models.Currency;

// Assuming economyAPI is already initialized
public void printCurrencyDetails(String currencyId) {
    Currency currency = economyAPI.getCurrency(currencyId);
    if (currency != null) {
        System.out.println("Currency ID: " + currency.getId());
        System.out.println("Currency Name: " + currency.getName());
        System.out.println("Currency Symbol: " + currency.getSymbol());
        System.out.println("Currency Singular Name: " + currency.getSingular());
    } else {
        System.out.println("Currency ID '" + currencyId + "' not found.");
    }
}
```

#### List Available Currencies

Retrieves collections of all enabled `Currency` objects or just their IDs.

```java
import com.spectrasonic.MythicEconomy.models.Currency;
import java.util.Collection;
import java.util.Set;

// Assuming economyAPI is already initialized
public void listAllCurrencies() {
    Collection<Currency> enabledCurrencies = economyAPI.getEnabledCurrencies();
    System.out.println("Enabled Currencies:");
    for (Currency currency : enabledCurrencies) {
        System.out.println("  - " + currency.getName() + " (" + currency.getId() + ")");
    }

    Set<String> currencyIds = economyAPI.getCurrencyIds();
    System.out.println("\nAll Currency IDs:");
    for (String id : currencyIds) {
        System.out.println("  - " + id);
    }
}
```

#### Check if Currency Exists

Verifies if a currency with the given `currencyId` is registered and enabled.

```java
// Assuming economyAPI is already initialized
public boolean checkIfCurrencyExists(String currencyId) {
    return economyAPI.currencyExists(currencyId);
}
```

## 4. Economy Events

MythicEconomy fires various events that you can listen to in your plugin to react to changes in the economy. All events are located in `com.spectrasonic.MythicEconomy.api.events`.

To listen to an event, register a listener in your plugin:

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.spectrasonic.MythicEconomy.api.events.MoneyAddEvent;
import com.spectrasonic.MythicEconomy.api.events.CurrencyMoneyAddEvent;

public class MyEconomyListener implements Listener {

    @EventHandler
    public void onMoneyAdd(MoneyAddEvent event) {
        // Handle money addition for the default currency
        System.out.println(event.getPlayer().getName() + " had " + event.getAmount() + " added to their default balance. New balance: " + event.getNewBalance());
    }

    @EventHandler
    public void onCurrencyMoneyAdd(CurrencyMoneyAddEvent event) {
        // Handle money addition for a specific currency
        System.out.println(event.getPlayer().getName() + " had " + event.getAmount() + " of " + event.getCurrencyId() + " added. New balance: " + event.getNewBalance());
    }

    // You can listen to other events like:
    // MoneyRemoveEvent
    // CurrencyMoneyRemoveEvent
    // MoneyTransferEvent
    // CurrencyEconomyEvent (generic event for all currency-related actions)
    // EconomyEvent (generic event for default economy actions)
}
```

Register your listener in your main plugin class's `onEnable()` method:

```java
// In your main plugin class (e.g., MyPlugin.java)
@Override
public void onEnable() {
    // ... other initialization
    getServer().getPluginManager().registerEvents(new MyEconomyListener(), this);
}
```

## 5. Examples

### Example 1: Simple Shop System

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

### Example 2: Reward System

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

## 6. Best Practices

### 1. Check Availability

Always check if MythicEconomy is available before using the API:

```java
if (!MythicEconomyAPI.isAvailable()) {
    // Handle the case where MythicEconomy is not available
    return;
}
```

### 2. Handle Errors

API methods often return `boolean` to indicate success or failure:

```java
if (!economyAPI.addMoney(player, amount)) {
    // Handle the error
    player.sendMessage("Error processing the transaction.");
}
```

### 3. Use Events for Logging

Listen to API events to maintain transaction logs:

```java
@EventHandler
public void onMoneyAdd(MoneyAddEvent event) {
    // Log transactions for auditing
    logTransaction(event.getPlayer(), "ADD", event.getAmount());
}
```

### 4. Format Amounts Consistently

Always use `formatMoney()` when displaying amounts to players:

```java
// ✅ Correct
player.sendMessage("Balance: " + economyAPI.formatMoney(balance));

// ❌ Incorrect
player.sendMessage("Balance: $" + balance);
```