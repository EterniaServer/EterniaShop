package br.com.eterniaserver.eterniashop;

import java.io.File;

public class Constants {

    public static final String SERVER_SHOP = "server_eternia_shop_grande";

    public static final String DATA_LAYER_FOLDER_PATH = "plugins" + File.separator + "EterniaShop";
    public static final String DATA_LOCALE_FOLDER_PATH = DATA_LAYER_FOLDER_PATH + File.separator + "locales";

    public static final String CONFIG_FILE_PATH = DATA_LAYER_FOLDER_PATH + File.separator + "config.yml";
    public static final String MESSAGES_FILE_PATH = DATA_LOCALE_FOLDER_PATH + File.separator + "messages.yml";
    public static final String COMMANDS_LOCALE_FILE_PATH = DATA_LOCALE_FOLDER_PATH + File.separator + "commands.yml";

}
