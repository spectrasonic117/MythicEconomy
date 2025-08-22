# Ejemplo de Plugin usando MythicEconomy API

Este es un ejemplo completo de cómo crear un plugin que use la API de MythicEconomy.

## Estructura del Proyecto

```
MiPlugin/
├── lib/
│   └── MythicEconomy-1.1.0.jar     # JAR de MythicEconomy aquí
├── src/main/java/com/ejemplo/miplugin/
│   ├── MiPlugin.java
│   ├── commands/TiendaCommand.java
│   └── listeners/EconomyListener.java
├── src/main/resources/
│   └── plugin.yml
└── pom.xml
```

## plugin.yml

```yaml
name: MiPlugin
version: 1.0.0
main: com.ejemplo.miplugin.MiPlugin
api-version: '1.21'
authors: [TuNombre]
description: "Ejemplo de plugin usando MythicEconomy API"
depend: [MythicEconomy]

commands:
  tienda:
    description: "Abrir la tienda"
    usage: "/tienda"
```

## MiPlugin.java

```java
package com.ejemplo.miplugin;

import com.spectrasonic.MythicEconomy.api.MythicEconomyAPI;
import com.ejemplo.miplugin.commands.TiendaCommand;
import com.ejemplo.miplugin.listeners.EconomyListener;
import org.bukkit.plugin.java.JavaPlugin;

public class MiPlugin extends JavaPlugin {
    
    private MythicEconomyAPI economyAPI;
    
    @Override
    public void onEnable() {
        // Verificar si MythicEconomy está disponible
        if (!MythicEconomyAPI.isAvailable()) {
            getLogger().severe("MythicEconomy no está disponible! Deshabilitando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Obtener la instancia de la API
        this.economyAPI = MythicEconomyAPI.getInstance();
        getLogger().info("MythicEconomy API cargada correctamente!");
        
        // Registrar comandos
        getCommand("tienda").setExecutor(new TiendaCommand(economyAPI));
        
        // Registrar listeners
        getServer().getPluginManager().registerEvents(new EconomyListener(economyAPI), this);
        
        getLogger().info("MiPlugin habilitado correctamente!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MiPlugin deshabilitado.");
    }
    
    public MythicEconomyAPI getEconomyAPI() {
        return economyAPI;
    }
}
```

## TiendaCommand.java

```java
package com.ejemplo.miplugin.commands;

import com.spectrasonic.MythicEconomy.api.MythicEconomyAPI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TiendaCommand implements CommandExecutor {
    
    private final MythicEconomyAPI economyAPI;
    
    public TiendaCommand(MythicEconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo los jugadores pueden usar este comando.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            mostrarTienda(player);
            return true;
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("comprar")) {
            try {
                int cantidad = Integer.parseInt(args[1]);
                comprarDiamantes(player, cantidad);
            } catch (NumberFormatException e) {
                player.sendMessage("§cCantidad inválida!");
            }
            return true;
        }
        
        player.sendMessage("§cUso: /tienda o /tienda comprar <cantidad>");
        return true;
    }
    
    private void mostrarTienda(Player player) {
        player.sendMessage("§6=== TIENDA ===");
        player.sendMessage("§eDiamante: §a" + economyAPI.formatMoney(100.0) + " §ecada uno");
        player.sendMessage("§eTu balance: §a" + economyAPI.formatMoney(economyAPI.getBalance(player)));
        player.sendMessage("§eUsa: §f/tienda comprar <cantidad>");
    }
    
    private void comprarDiamantes(Player player, int cantidad) {
        if (cantidad <= 0) {
            player.sendMessage("§cLa cantidad debe ser mayor a 0!");
            return;
        }
        
        double precioUnitario = 100.0;
        double costoTotal = precioUnitario * cantidad;
        
        // Verificar si el jugador tiene suficiente dinero
        if (!economyAPI.canPay(player, costoTotal)) {
            player.sendMessage("§cNo tienes suficiente dinero!");
            player.sendMessage("§cNecesitas: §e" + economyAPI.formatMoney(costoTotal));
            player.sendMessage("§cTienes: §e" + economyAPI.formatMoney(economyAPI.getBalance(player)));
            return;
        }
        
        // Realizar la compra
        if (economyAPI.removeMoney(player, costoTotal)) {
            ItemStack diamantes = new ItemStack(Material.DIAMOND, cantidad);
            player.getInventory().addItem(diamantes);
            
            player.sendMessage("§a¡Compra exitosa!");
            player.sendMessage("§eCompraste: §f" + cantidad + " diamantes");
            player.sendMessage("§eCosto: §c-" + economyAPI.formatMoney(costoTotal));
            player.sendMessage("§eBalance actual: §a" + economyAPI.formatMoney(economyAPI.getBalance(player)));
        } else {
            player.sendMessage("§cError al procesar la compra.");
        }
    }
}
```

## EconomyListener.java

```java
package com.ejemplo.miplugin.listeners;

import com.spectrasonic.MythicEconomy.api.MythicEconomyAPI;
import com.spectrasonic.MythicEconomy.api.events.MoneyAddEvent;
import com.spectrasonic.MythicEconomy.api.events.MoneyRemoveEvent;
import com.spectrasonic.MythicEconomy.api.events.MoneyTransferEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyListener implements Listener {
    
    private final MythicEconomyAPI economyAPI;
    
    public EconomyListener(MythicEconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
    }
    
    // Recompensa por unirse al servidor
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!player.hasPlayedBefore()) {
            // Dar bono de bienvenida a nuevos jugadores
            double bono = 500.0;
            economyAPI.addMoney(player, bono);
            player.sendMessage("§a¡Bienvenido! Has recibido un bono de " + 
                              economyAPI.formatMoney(bono) + "!");
        } else {
            // Recompensa diaria para jugadores existentes
            double recompensaDiaria = 50.0;
            economyAPI.addMoney(player, recompensaDiaria);
            player.sendMessage("§e¡Recompensa diaria! +" + 
                              economyAPI.formatMoney(recompensaDiaria));
        }
    }
    
    // Recompensas por minar
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material block = event.getBlock().getType();
        double recompensa = 0;
        
        switch (block) {
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                recompensa = 5.0;
                break;
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                recompensa = 10.0;
                break;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                recompensa = 20.0;
                break;
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                recompensa = 50.0;
                break;
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                recompensa = 75.0;
                break;
        }
        
        if (recompensa > 0) {
            economyAPI.addMoney(player, recompensa);
            player.sendMessage("§a+" + economyAPI.formatMoney(recompensa) + 
                              " por minar " + block.name().toLowerCase().replace("_", " "));
        }
    }
    
    // Escuchar eventos de la API de MythicEconomy
    @EventHandler
    public void onMoneyAdd(MoneyAddEvent event) {
        Player player = event.getPlayer();
        double amount = event.getAmount();
        
        // Log de transacciones grandes
        if (amount >= 1000.0) {
            System.out.println("[MiPlugin] Transacción grande: " + player.getName() + 
                              " recibió " + economyAPI.formatMoney(amount));
        }
    }
    
    @EventHandler
    public void onMoneyRemove(MoneyRemoveEvent event) {
        Player player = event.getPlayer();
        double amount = event.getAmount();
        
        // Avisar si el jugador se está quedando sin dinero
        if (event.getNewBalance() < 100.0) {
            player.sendMessage("§6¡Cuidado! Te estás quedando sin dinero.");
            player.sendMessage("§6Considera minar o hacer trabajos para ganar más.");
        }
    }
    
    @EventHandler
    public void onMoneyTransfer(MoneyTransferEvent event) {
        Player from = event.getFrom();
        Player to = event.getTo();
        double amount = event.getAmount();
        
        // Prevenir transferencias muy grandes entre jugadores nuevos
        if (amount > 10000.0 && (!from.hasPlayedBefore() || !to.hasPlayedBefore())) {
            event.setCancelled(true);
            from.sendMessage("§cNo puedes transferir cantidades tan grandes siendo nuevo.");
            return;
        }
        
        // Log de transferencias
        System.out.println("[MiPlugin] Transferencia: " + from.getName() + 
                          " -> " + to.getName() + " (" + economyAPI.formatMoney(amount) + ")");
    }
}
```

## pom.xml (ejemplo)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ejemplo</groupId>
    <artifactId>MiPlugin</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>papermc-repo</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- Paper API -->
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>1.21.1-R0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- MythicEconomy (dependencia local) -->
        <dependency>
            <groupId>com.spectrasonic</groupId>
            <artifactId>MythicEconomy</artifactId>
            <version>1.1.0</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/lib/MythicEconomy-1.1.0.jar</systemPath>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Características del Ejemplo

Este plugin de ejemplo demuestra:

1. **Verificación de disponibilidad** de MythicEconomy
2. **Uso básico de la API** para comprar items
3. **Sistema de recompensas** automático
4. **Escucha de eventos** de la API
5. **Validaciones y controles** de seguridad
6. **Formateo correcto** de cantidades de dinero

## Compilación y Uso

1. **Preparar dependencia:**
   - Crea una carpeta `lib/` en la raíz de tu proyecto
   - Coloca `MythicEconomy-1.1.0.jar` en la carpeta `lib/`

2. **Configurar proyecto:**
   - Copia el código en tu proyecto
   - Asegúrate de que el `pom.xml` tenga la dependencia local configurada

3. **Compilar:**
   ```bash
   mvn clean package
   ```

4. **Instalar:**
   - Asegúrate de tener MythicEconomy instalado en tu servidor
   - Coloca tu JAR compilado en la carpeta `plugins/`
   - Reinicia el servidor

El plugin creará automáticamente un sistema de tienda simple y recompensas por minar.