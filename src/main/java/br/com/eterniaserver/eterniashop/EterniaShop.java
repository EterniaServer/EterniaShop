package br.com.eterniaserver.eterniashop;

import br.com.eterniaserver.eternialib.CommandManager;
import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eterniashop.configurations.ConfigsCfg;
import br.com.eterniaserver.eterniashop.configurations.Metrics;
import br.com.eterniaserver.eterniashop.configurations.locales.CommandsLocale;
import br.com.eterniaserver.eterniashop.configurations.locales.MessagesCfg;
import br.com.eterniaserver.eterniashop.core.ShopCommands;
import br.com.eterniaserver.eterniashop.core.Tick;
import br.com.eterniaserver.eterniashop.enums.Booleans;
import br.com.eterniaserver.eterniashop.enums.Commands;
import br.com.eterniaserver.eterniashop.enums.Doubles;
import br.com.eterniaserver.eterniashop.enums.Integers;
import br.com.eterniaserver.eterniashop.enums.Messages;
import br.com.eterniaserver.eterniashop.enums.Strings;
import br.com.eterniaserver.paperlib.PaperLib;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EterniaShop extends JavaPlugin {

    public final List<String> listOfShops = new ArrayList<>();
    public final Map<String, Location> shops = new HashMap<>();

    private final int[] integers = new int[Integers.values().length];
    private final double[] doubles = new double[Doubles.values().length];
    private final boolean[] booleans = new boolean[Booleans.values().length];
    private final String[] strings = new String[Strings.values().length];
    private final String[] messages = new String[Messages.values().length];

    private Economy economy;

    @Override
    public void onEnable() {

        loadConfigurations();
        loadVault();
        loadCommandsLocale();

        new Metrics(this, 9846);
        new Tick(this).runTaskTimer(this, 20L, 20L);

        PaperLib.suggestPaper(this);

    }

    public boolean getBool(Booleans entry) {
        return booleans[entry.ordinal()];
    }

    public int getInt(Integers entry) {
        return integers[entry.ordinal()];
    }

    public double getDouble(Doubles entry) {
        return doubles[entry.ordinal()];
    }

    public String getString(Strings entry) {
        return strings[entry.ordinal()];
    }

    public Economy getEconomy() {
        return economy;
    }

    public void sendMessage(final CommandSender sender, Messages messagesId, String... args) {
        sendMessage(sender, messagesId, true, args);
    }

    public void sendMessage(final CommandSender sender, Messages messagesId, boolean prefix, String... args) {
        sender.sendMessage(getMessage(messagesId, prefix, args));
    }

    public String getMessage(Messages messagesId, boolean prefix, String... args) {
        String message = messages[messagesId.ordinal()];

        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }

        if (prefix) {
            return strings[Strings.SERVER_PREFIX.ordinal()] + message;
        }
        return message;
    }

    private void loadConfigurations() {
        final ConfigsCfg configsCfg = new ConfigsCfg(booleans, integers, doubles, strings, this);
        final MessagesCfg messagesCfg = new MessagesCfg(messages);

        EterniaLib.addReloadableConfiguration("eterniashop", "config", configsCfg);
        EterniaLib.addReloadableConfiguration("eterniashop", "messages", messagesCfg);

        configsCfg.executeConfig();
        messagesCfg.executeConfig();
        configsCfg.executeCritical();
    }

    private void loadVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            booleans[Booleans.VAULT.ordinal()] = false;
            return;
        }

        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            booleans[Booleans.VAULT.ordinal()] = false;
            return;
        }

        this.economy = rsp.getProvider();
    }

    private void loadCommandsLocale() {
        final CommandsLocale commandsLocale = new CommandsLocale();

        for (final Commands command : Commands.values()) {
            CommandManager.getCommandReplacements().addReplacements(
                    command.name().toLowerCase(), commandsLocale.getName(command),
                    command.name().toLowerCase() + "_description", commandsLocale.getDescription(command),
                    command.name().toLowerCase() + "_permission", commandsLocale.getPermission(command),
                    command.name().toLowerCase() + "_syntax", commandsLocale.getSyntax(command)
            );
        }

        CommandManager.registerCommand(new ShopCommands(this));
        CommandManager.getCommandCompletions().registerCompletion("list_of_shops", shop -> listOfShops);
    }

}
