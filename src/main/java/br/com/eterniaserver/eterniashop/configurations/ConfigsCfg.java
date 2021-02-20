package br.com.eterniaserver.eterniashop.configurations;

import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.core.enums.ConfigurationCategory;
import br.com.eterniaserver.eternialib.core.interfaces.ReloadableConfiguration;
import br.com.eterniaserver.eternialib.core.queries.CreateTable;
import br.com.eterniaserver.eternialib.core.queries.Select;
import br.com.eterniaserver.eterniashop.Constants;
import br.com.eterniaserver.eterniashop.EterniaShop;
import br.com.eterniaserver.eterniashop.enums.Booleans;
import br.com.eterniaserver.eterniashop.enums.Doubles;
import br.com.eterniaserver.eterniashop.enums.Integers;
import br.com.eterniaserver.eterniashop.enums.Messages;
import br.com.eterniaserver.eterniashop.enums.Strings;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigsCfg implements ReloadableConfiguration {

    private final EterniaShop plugin;

    private final boolean[] booleans;
    private final int[] integers;
    private final double[] doubles;
    private final String[] strings;

    public ConfigsCfg(final boolean[] booleans, final int[] integers, final double[] doubles, final String[] strings, final EterniaShop plugin) {
        this.booleans = booleans;
        this.integers = integers;
        this.doubles = doubles;
        this.strings = strings;
        this.plugin = plugin;
    }

    @Override
    public ConfigurationCategory category() {
        return ConfigurationCategory.WARNING_ADVICE;
    }

    @Override
    public void executeConfig() {
        // Load the configurations
        final FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(Constants.CONFIG_FILE_PATH));
        booleans[Booleans.VAULT.ordinal()] = configuration.getBoolean("vault.use", true);
        booleans[Booleans.VAULT_SHOP.ordinal()] = configuration.getBoolean("vault.shop.enabled", true);
        booleans[Booleans.VAULT_SET_SHOP.ordinal()] = configuration.getBoolean("vault.set-shop.enabled", true);
        booleans[Booleans.USE_CONFIRMATION.ordinal()] = configuration.getBoolean("vault.enable-confirmation", true);

        integers[Integers.TELEPORT_DELAY.ordinal()] = configuration.getInt("times.teleport-delay", 4);
        integers[Integers.CONFIRMATION_EXPIRATION.ordinal()] = configuration.getInt("times.confirmation-expiration", 30);

        doubles[Doubles.SHOP_PRICE.ordinal()] = configuration.getDouble("vault.shop.price", 400.0D);
        doubles[Doubles.SET_SHOP_PRICE.ordinal()] = configuration.getDouble("vault.set-shop.price", 400.0D);

        strings[Strings.SERVER_PREFIX.ordinal()] = configuration.getString("constants.prefix", "$8[$aE$9S$8]$7 ").replace('$', (char) 0x00A7);
        strings[Strings.TELEPORT_DELAY_BYPASS.ordinal()] = configuration.getString("permissions.teleport-delay-bypass", "eternia.timings.bypass");
        strings[Strings.TABLE_SHOP.ordinal()] = configuration.getString("constants.table-shop", "eterniashop").replace('$', (char) 0x00A7);
        strings[Strings.PRICE_BYPASS.ordinal()] = configuration.getString("permissions.price-bypass", "eternia.shop.bypass");

        // Save the configurations
        final FileConfiguration outConfiguration = new YamlConfiguration();

        outConfiguration.set("times.teleport-delay", integers[Integers.TELEPORT_DELAY.ordinal()]);
        outConfiguration.set("times.confirmation-expiration", integers[Integers.CONFIRMATION_EXPIRATION.ordinal()]);

        outConfiguration.set("vault.use", booleans[Booleans.VAULT.ordinal()]);
        outConfiguration.set("vault.shop.enabled", booleans[Booleans.VAULT_SHOP.ordinal()]);
        outConfiguration.set("vault.set-shop.enabled", booleans[Booleans.VAULT_SET_SHOP.ordinal()]);
        outConfiguration.set("vault.enable-confirmation", booleans[Booleans.USE_CONFIRMATION.ordinal()]);

        outConfiguration.set("vault.shop.price", doubles[Doubles.SHOP_PRICE.ordinal()]);
        outConfiguration.set("vault.set-shop.price", doubles[Doubles.SET_SHOP_PRICE.ordinal()]);

        outConfiguration.set("constants.prefix", strings[Strings.SERVER_PREFIX.ordinal()]);
        outConfiguration.set("permissions.teleport-delay-bypass", strings[Strings.TELEPORT_DELAY_BYPASS.ordinal()]);
        outConfiguration.set("constants.table-shop", strings[Strings.TABLE_SHOP.ordinal()]);
        outConfiguration.set("permissions.price-bypass", strings[Strings.PRICE_BYPASS.ordinal()]);

        try {
            if (new File(Constants.DATA_LAYER_FOLDER_PATH).mkdir()) {
                outConfiguration.save(Constants.CONFIG_FILE_PATH);
            }
        } catch (IOException exception) {
            // todo
        }
    }

    @Override
    public void executeCritical() {
        final CreateTable createTable = new CreateTable(strings[Strings.TABLE_SHOP.ordinal()]);

        if (EterniaLib.getMySQL()) {
            createTable.columns.set("id INT AUTO_INCREMENT NOT NULL PRIMARY KEY", "player_name VARCHAR(32)", "world VARCHAR(32)",
                    "coord_x DOUBLE", "coord_y DOUBLE", "coord_z DOUBLE", "coord_yaw FLOAT", "coord_pitch FLOAT");
        } else {
            createTable.columns.set("player_name VARCHAR(32)", "world VARCHAR(32)", "coord_x DOUBLE", "coord_y DOUBLE",
                    "coord_z DOUBLE", "coord_yaw FLOAT", "coord_pitch FLOAT");
        }

        SQL.execute(createTable);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            try (final Connection connection = SQL.getConnection(); final ResultSet resultSet = connection.prepareStatement(new Select(strings[Strings.TABLE_SHOP.ordinal()]).queryString()).executeQuery()) {
                while (resultSet.next()) {
                    final String playerName = resultSet.getString("player_name");
                    final String worldName = resultSet.getString("world");
                    final double x = resultSet.getDouble("coord_x");
                    final double y = resultSet.getDouble("coord_y");
                    final double z = resultSet.getDouble("coord_z");
                    final float yaw = resultSet.getFloat("coord_yaw");
                    final float pitch = resultSet.getFloat("coord_pitch");

                    if (playerName == null || worldName == null) {
                        continue;
                    }

                    final World world = plugin.getServer().getWorld(worldName);

                    if (world == null) {
                        continue;
                    }

                    if (!playerName.equals(Constants.SERVER_SHOP)) {
                        plugin.listOfShops.add(playerName);
                    }
                    plugin.shops.put(playerName.toLowerCase(), new Location(world, x, y, z, yaw, pitch));
                }
                plugin.getServer().getConsoleSender().sendMessage(plugin.getMessage(Messages.ETERNIASHOP_LOADED, true, String.valueOf(plugin.shops.size())));
            } catch (SQLException exception) {
                // todo
            }
        });
    }
}
