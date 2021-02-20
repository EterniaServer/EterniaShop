package br.com.eterniaserver.eterniashop.configurations.locales;

import br.com.eterniaserver.eternialib.core.baseobjects.CommandLocale;
import br.com.eterniaserver.eterniashop.Constants;
import br.com.eterniaserver.eterniashop.enums.Commands;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CommandsLocale {

    private final CommandLocale[] locales = new CommandLocale[Commands.values().length];

    public CommandsLocale() {
        final CommandLocale[] defaultLocale = new CommandLocale[Commands.values().length];

        // Load default configurations
        addCommand(defaultLocale, Commands.SHOP, "shop|loja", "(opcional) <jogador>", "eternia.shop", " Vá até uma loja podendo ser de jogador ou do servidor");
        addCommand(defaultLocale, Commands.SET_SHOP, "setshop|setloja", null, "eternia.setshop", " Defina sua loja");
        addCommand(defaultLocale, Commands.SET_SERVER_SHOP, "setservershop|setserverloja", null, "eternia.setservershop", " Defina a loja do servidor");
        addCommand(defaultLocale, Commands.ETERNIASHOP, "eterniashop|eternialoja", null, "eternia.eterniashop", " Verifique informações sobre o plugin");
        addCommand(defaultLocale, Commands.ETERNIASHOP_HELP, "help|ajuda", "<página>", "eternia.eterniashop.help", " Receba ajuda para os comandos de loja");
        addCommand(defaultLocale, Commands.DEL_SHOP, "delshop|delloja", null, "eternia.delshop", " Delete a sua loja");

        final FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(Constants.COMMANDS_LOCALE_FILE_PATH));

        for (final Commands entry : Commands.values()) {
            final CommandLocale commandObject = defaultLocale[entry.ordinal()];

            final String cmdName = configuration.getString(entry.name() + ".name", commandObject.name);
            final String cmdSyntax = configuration.getString(entry.name() + ".syntax", commandObject.syntax);
            final String cmdPerm = configuration.getString(entry.name() + ".perm", commandObject.perm);
            final String cmdDescription = configuration.getString(entry.name() + ".description", commandObject.description);

            configuration.set(entry.name() + ".name", cmdName);
            configuration.set(entry.name() + ".syntax", cmdSyntax);
            configuration.set(entry.name() + ".perm", cmdPerm);
            configuration.set(entry.name() + ".description", cmdDescription);

            locales[entry.ordinal()] = new CommandLocale(cmdName, cmdSyntax, cmdDescription, cmdPerm, null);
        }

        try {
            if (new File(Constants.DATA_LOCALE_FOLDER_PATH).mkdir()) {
                configuration.save(Constants.COMMANDS_LOCALE_FILE_PATH);
            }
        } catch (IOException exception) {
            // todo
        }

    }

    private void addCommand(final CommandLocale[] defaults, final Commands id, final String name, final String syntax, final String permission, final String description) {
        defaults[id.ordinal()] = new CommandLocale(name, syntax, permission, description, null);
    }

    public String getName(Commands id) {
        return locales[id.ordinal()].name;
    }

    public String getPermission(Commands id) {
        return locales[id.ordinal()].perm;
    }

    public String getSyntax(Commands id) {
        return locales[id.ordinal()].syntax != null ? locales[id.ordinal()].syntax : "";
    }

    public String getDescription(Commands id) {
        return locales[id.ordinal()].description;
    }

}