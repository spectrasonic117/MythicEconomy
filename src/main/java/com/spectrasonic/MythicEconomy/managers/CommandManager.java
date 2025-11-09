package com.spectrasonic.MythicEconomy.managers;

import com.spectrasonic.MythicEconomy.commands.EconomyCommand;
import com.spectrasonic.MythicEconomy.commands.MoneyCommand;
import com.spectrasonic.MythicEconomy.commands.PayCommand;
import com.spectrasonic.MythicEconomy.commands.CurrencyCommand;
import com.spectrasonic.MythicEconomy.commands.BalanceCurrencyCommand;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CommandManager {

    private final JavaPlugin plugin;

    public void registerCommands() {
        new EconomyCommand().register();
        new MoneyCommand().register();
        new PayCommand().register();
        new CurrencyCommand().register();
        new BalanceCurrencyCommand().register();
    }
}