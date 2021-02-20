package br.com.eterniaserver.eterniashop.core;

import br.com.eterniaserver.acf.BaseCommand;
import br.com.eterniaserver.acf.CommandHelp;
import br.com.eterniaserver.acf.annotation.CatchUnknown;
import br.com.eterniaserver.acf.annotation.CommandAlias;
import br.com.eterniaserver.acf.annotation.CommandCompletion;
import br.com.eterniaserver.acf.annotation.CommandPermission;
import br.com.eterniaserver.acf.annotation.Default;
import br.com.eterniaserver.acf.annotation.Description;
import br.com.eterniaserver.acf.annotation.HelpCommand;
import br.com.eterniaserver.acf.annotation.Optional;
import br.com.eterniaserver.acf.annotation.Subcommand;
import br.com.eterniaserver.acf.annotation.Syntax;
import br.com.eterniaserver.eternialib.CmdConfirmationManager;
import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.core.queries.Delete;
import br.com.eterniaserver.eternialib.core.queries.Insert;
import br.com.eterniaserver.eterniashop.Constants;
import br.com.eterniaserver.eterniashop.EterniaShop;
import br.com.eterniaserver.eterniashop.baseobjects.PlayerTeleport;
import br.com.eterniaserver.eterniashop.baseobjects.ShopConfirmation;
import br.com.eterniaserver.eterniashop.baseobjects.UpdateShop;
import br.com.eterniaserver.eterniashop.enums.Booleans;
import br.com.eterniaserver.eterniashop.enums.Doubles;
import br.com.eterniaserver.eterniashop.enums.Messages;
import br.com.eterniaserver.eterniashop.enums.Strings;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%eterniashop")
public class ShopCommands extends BaseCommand {

    private final EterniaShop plugin;

    public ShopCommands(final EterniaShop plugin) {
        this.plugin = plugin;
    }

    @Default
    @CatchUnknown
    @CommandPermission("%eterniashop_permission")
    @Description("%eterniashop_description")
    public void onEterniaShop(final CommandSender sender) {
        plugin.sendMessage(sender, Messages.ETERNIASHOP);
    }

    @HelpCommand
    @Subcommand("%eterniashop_help")
    @CommandPermission("%eterniashop_help_permission")
    @Syntax("%eterniashop_help_syntax")
    @Description("%eterniashop_help_description")
    public void onEterniaShopHelp(final CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @CommandAlias("%del_shop")
    @CommandPermission("%del_shop_permission")
    @Description("%del_shop_description")
    public void onDelShop(final Player player) {
        final String shopName = player.getName().toLowerCase();
        if (!plugin.shops.containsKey(shopName)) {
            plugin.sendMessage(player, Messages.NOT_SHOP);
            return;

        }

        if (plugin.getBool(Booleans.VAULT) && plugin.getBool(Booleans.USE_CONFIRMATION)) {
            CmdConfirmationManager.scheduleCommand(player, () -> {
                deleteShop(shopName);
                plugin.sendMessage(player, Messages.SHOP_DELETED);
            });
            plugin.sendMessage(player, Messages.SHOP_DELETE_CONFIRM);
            return;
        }

        deleteShop(shopName);
        plugin.sendMessage(player, Messages.SHOP_DELETED);
    }

    @CommandAlias("%set_server_shop")
    @CommandPermission("%set_server_shop_permission")
    @Description("%set_server_shop_description")
    public void onSetServerShop(final Player player) {
        final Location location = player.getLocation();
        if (isNotSafeLocation(location)) {
            plugin.sendMessage(player, Messages.NOT_SAFE);
            return;
        }

        setShop(Constants.SERVER_SHOP, location);
        plugin.sendMessage(player, Messages.SERVER_SHOP_DEFINED);
    }

    @CommandAlias("%set_shop")
    @CommandPermission("%set_shop_permission")
    @Description("%set_shop_description")
    public void onSetShop(final Player player) {
        if (plugin.getEconomy() == null || !plugin.getBool(Booleans.VAULT) || !plugin.getBool(Booleans.VAULT_SET_SHOP) || player.hasPermission(plugin.getString(Strings.PRICE_BYPASS))) {
            final Location location = player.getLocation();
            if (isNotSafeLocation(location)) {
                plugin.sendMessage(player, Messages.NOT_SAFE);
                return;
            }

            setShop(player.getName().toLowerCase(), location);
            plugin.sendMessage(player, Messages.SHOP_DEFINED);
            return;
        }

        final String playerName = player.getName();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

        if (!plugin.getEconomy().has(offlinePlayer, plugin.getDouble(Doubles.SET_SHOP_PRICE))) {
            plugin.sendMessage(player, Messages.NOT_MONEY, String.valueOf(plugin.getDouble(Doubles.SET_SHOP_PRICE)));
        }

        if (plugin.getBool(Booleans.USE_CONFIRMATION)) {
            CmdConfirmationManager.scheduleCommand(player, () -> {
                if (plugin.getEconomy().has(offlinePlayer, plugin.getDouble(Doubles.SET_SHOP_PRICE))) {
                    setShop(player.getName().toLowerCase(), player.getLocation());
                    plugin.getEconomy().withdrawPlayer(offlinePlayer, plugin.getDouble(Doubles.SHOP_PRICE));
                    plugin.sendMessage(player, Messages.SHOP_DEFINED);
                    return;
                }
                plugin.sendMessage(player, Messages.NOT_MONEY, String.valueOf(plugin.getDouble(Doubles.SET_SHOP_PRICE)));
            });
            plugin.sendMessage(player, Messages.COMMAND_COST, String.valueOf(plugin.getDouble(Doubles.SET_SHOP_PRICE)));
            return;
        }

        if (plugin.getEconomy().has(offlinePlayer, plugin.getDouble(Doubles.SET_SHOP_PRICE))) {
            setShop(player.getName().toLowerCase(), player.getLocation());
            plugin.getEconomy().withdrawPlayer(offlinePlayer, plugin.getDouble(Doubles.SHOP_PRICE));
            plugin.sendMessage(player, Messages.SHOP_DEFINED);
            return;
        }
        plugin.sendMessage(player, Messages.NOT_MONEY, String.valueOf(plugin.getDouble(Doubles.SET_SHOP_PRICE)));
    }

    @CommandAlias("%shop")
    @CommandPermission("%shop_permission")
    @Syntax("%shop_syntax")
    @Description("%shop_description")
    @CommandCompletion("@list_of_shops")
    public void onShop(final Player player, @Optional String shopName) {
        if (shopName == null) {
            final Location location = plugin.shops.get(Constants.SERVER_SHOP);
            if (location == null) {
                plugin.sendMessage(player, Messages.SERVER_SHOP_NOT_DEFINED);
                return;
            }

            Tick.playersInTeleporting.put(player.getName(), new PlayerTeleport(player, location, plugin.getMessage(Messages.SHOP_WELCOME, true), plugin));
            return;
        }

        shopName = shopName.toLowerCase();
        final Location location = plugin.shops.get(shopName);

        if (location == null) {
            plugin.sendMessage(player, Messages.SHOP_PLAYER_NOT);
            return;
        }

        if (plugin.getEconomy() != null && plugin.getBool(Booleans.VAULT) && plugin.getBool(Booleans.VAULT_SHOP) && !player.hasPermission(plugin.getString(Strings.PRICE_BYPASS))) {
            final String playerName = player.getName();
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

            if (plugin.getEconomy().has(offlinePlayer, plugin.getDouble(Doubles.SHOP_PRICE))) {
                if (plugin.getBool(Booleans.USE_CONFIRMATION)) {
                    final String definitiveShopName = shopName;
                    CmdConfirmationManager.scheduleCommand(player, new ShopConfirmation(() -> {
                        if (plugin.getEconomy().has(offlinePlayer, plugin.getDouble(Doubles.SHOP_PRICE))) {
                            Tick.playersInTeleporting.put(player.getName(), new PlayerTeleport(player, location, plugin.getMessage(Messages.SHOP_PLAYER_WELCOME, true, definitiveShopName), plugin));
                            plugin.getEconomy().withdrawPlayer(offlinePlayer, plugin.getDouble(Doubles.SHOP_PRICE));
                            return;
                        }
                        plugin.sendMessage(player, Messages.NOT_MONEY, String.valueOf(plugin.getDouble(Doubles.SHOP_PRICE)));
                    }));
                    plugin.sendMessage(player, Messages.COMMAND_COST, String.valueOf(plugin.getDouble(Doubles.SHOP_PRICE)));
                    return;
                }

                if (plugin.getEconomy().has(offlinePlayer, plugin.getDouble(Doubles.SHOP_PRICE))) {
                    Tick.playersInTeleporting.put(player.getName(), new PlayerTeleport(player, location, plugin.getMessage(Messages.SHOP_PLAYER_WELCOME, true, shopName), plugin));
                    plugin.getEconomy().withdrawPlayer(offlinePlayer, plugin.getDouble(Doubles.SHOP_PRICE));
                    return;
                }
                plugin.sendMessage(player, Messages.NOT_MONEY, String.valueOf(plugin.getDouble(Doubles.SHOP_PRICE)));
                return;
            }

            plugin.sendMessage(player, Messages.NOT_MONEY, String.valueOf(plugin.getDouble(Doubles.SET_SHOP_PRICE)));
            return;
        }

        Tick.playersInTeleporting.put(player.getName(), new PlayerTeleport(player, location, plugin.getMessage(Messages.SHOP_PLAYER_WELCOME, true, shopName), plugin));
    }

    private void setShop(final String playerName, final Location location) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.shops.containsKey(playerName)) {
                final UpdateShop updateShop = new UpdateShop(plugin.getString(Strings.TABLE_SHOP));
                updateShop.where.set("player_name", playerName);
                updateShop.setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
                SQL.execute(updateShop);
            } else {
                final Insert insert = new Insert(plugin.getString(Strings.TABLE_SHOP));
                insert.columns.set("player_name", "world", "coord_x", "coord_y", "coord_z", "coord_yaw", "coord_pitch");
                insert.values.set(playerName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
                SQL.execute(insert);
            }
            plugin.listOfShops.add(playerName.toLowerCase());
            plugin.shops.put(playerName.toLowerCase(), location);
        });
    }

    private void deleteShop(final String shopName) {
        final Delete delete = new Delete(plugin.getString(Strings.TABLE_SHOP));
        delete.where.set("player_name", shopName);
        SQL.executeAsync(delete);
        plugin.listOfShops.remove(shopName);
    }

    private boolean isNotSafeLocation(final Location location) {
        Block feet = location.getBlock();

        if (!(feet.getType() == Material.AIR) && !(feet.getLocation().add(0, 1, 0).getBlock().getType() == Material.AIR)) {
            return true;
        }

        if (!(feet.getRelative(BlockFace.UP).getType() == Material.AIR)) {
            return true;
        }

        return !feet.getRelative(BlockFace.DOWN).getType().isSolid();
    }

}