package com.spectrasonic.MythicEconomy;

import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.managers.CommandManager;
import com.spectrasonic.MythicEconomy.managers.EventManager;
import com.spectrasonic.MythicEconomy.managers.ConfigManager;
import com.spectrasonic.MythicEconomy.providers.VaultEconomyProvider;
import com.spectrasonic.MythicEconomy.placeholders.MythicEconomyPlaceholders;
import com.spectrasonic.MythicEconomy.utils.CommandUtils;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;
import net.milkbowl.vault.economy.Economy;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import lombok.Getter;

@Getter
public final class Main extends JavaPlugin {

    private EconomyManager economyManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private ConfigManager configManager;
    private VaultEconomyProvider vaultEconomyProvider;
    private MythicEconomyPlaceholders placeholders;
    private boolean vaultEnabled = false;
    private boolean placeholderAPIEnabled = false;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        CommandAPI.onEnable();

        this.economyManager = new EconomyManager(this);
        this.commandManager = new CommandManager(this);
        this.eventManager = new EventManager(this);
        this.configManager = new ConfigManager(this);

        this.setupVault();

        this.setupPlaceholderAPI();

        commandManager.registerCommands();

        CommandUtils.setPlugin(this);

        MessageUtils.sendStartupMessage(this);
        MessageUtils.sendConsoleMessage("<green>Sistema de economía inicializado correctamente.</green>");

        if (vaultEnabled) {
            MessageUtils.sendConsoleMessage("<green>Integración con Vault habilitada.</green>");
        }

        if (placeholderAPIEnabled) {
            MessageUtils.sendConsoleMessage("<green>Integración con PlaceholderAPI habilitada.</green>");
        }
    }

    @Override
    public void onDisable() {
        if (vaultEnabled && vaultEconomyProvider != null) {
            getServer().getServicesManager().unregister(Economy.class, vaultEconomyProvider);
            MessageUtils.sendConsoleMessage("<yellow>Proveedor de Vault desregistrado.</yellow>");
        }

        if (placeholderAPIEnabled && placeholders != null) {
            placeholders.unregister();
            MessageUtils.sendConsoleMessage("<yellow>Placeholders de PlaceholderAPI desregistrados.</yellow>");
        }

        // Guardar datos antes de cerrar
        if (economyManager != null) {
            // Guardar directamente sin llamar a savePlayerData para evitar recursión
            economyManager.getDataProvider().save();
            MessageUtils.sendConsoleMessage("<yellow>Datos de economía guardados correctamente.</yellow>");
        }

        CommandAPI.onDisable();
        MessageUtils.sendShutdownMessage(this);
    }

    // Configura la integración con Vault si está disponible
    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("Vault no encontrado. Funcionalidad de Vault deshabilitada.");
            return;
        }

        try {
            // Crear el proveedor de economía de Vault
            this.vaultEconomyProvider = new VaultEconomyProvider(economyManager);

            // Registrar el proveedor con alta prioridad
            getServer().getServicesManager().register(
                    Economy.class,
                    vaultEconomyProvider,
                    this,
                    ServicePriority.Highest);

            this.vaultEnabled = true;
            getLogger().info("Proveedor de economía de Vault registrado exitosamente.");

        } catch (Exception e) {
            getLogger().warning("Error al configurar Vault: " + e.getMessage());
            this.vaultEnabled = false;
        }
    }

    // Verifica si Vault está habilitado
    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    // Obtiene el proveedor de Vault
    public VaultEconomyProvider getVaultProvider() {
        return vaultEconomyProvider;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    // Configura la integración con PlaceholderAPI si está disponible
    private void setupPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("PlaceholderAPI no encontrado. Funcionalidad de placeholders deshabilitada.");
            return;
        }

        try {
            // Crear y registrar los placeholders
            this.placeholders = new MythicEconomyPlaceholders(this);

            if (placeholders.register()) {
                this.placeholderAPIEnabled = true;
                getLogger().info("Placeholders de MythicEconomy registrados exitosamente.");
            } else {
                getLogger().warning("Error al registrar los placeholders de MythicEconomy.");
                this.placeholderAPIEnabled = false;
            }

        } catch (Exception e) {
            getLogger().warning("Error al configurar PlaceholderAPI: " + e.getMessage());
            this.placeholderAPIEnabled = false;
        }
    }

    // Verifica si PlaceholderAPI está habilitado
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    // Obtiene la instancia de placeholders
    public MythicEconomyPlaceholders getPlaceholders() {
        return placeholders;
    }
}