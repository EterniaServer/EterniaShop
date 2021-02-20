package br.com.eterniaserver.eterniashop.core;

import br.com.eterniaserver.eterniashop.EterniaShop;
import br.com.eterniaserver.eterniashop.baseobjects.PlayerTeleport;
import br.com.eterniaserver.eterniashop.enums.Messages;
import br.com.eterniaserver.eterniashop.enums.Strings;
import br.com.eterniaserver.paperlib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Tick extends BukkitRunnable {

    private final EterniaShop plugin;

    protected static final Map<String, PlayerTeleport> playersInTeleporting = new HashMap<>();

    public Tick(final EterniaShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        for (final Player player : Bukkit.getOnlinePlayers()) {
            getPlayersInTp(player);
        }

    }

    private void getPlayersInTp(final Player player) {
        final PlayerTeleport playerTeleport = playersInTeleporting.get(player.getName());

        if (playerTeleport == null) {
            return;
        }

        if (playerTeleport.getCountdown() == 0 || player.hasPermission(plugin.getString(Strings.TELEPORT_DELAY_BYPASS))) {
            PaperLib.teleportAsync(player, playerTeleport.getWantLocation());
            player.sendMessage(playerTeleport.getMessage());
            playersInTeleporting.remove(player.getName());
            return;
        }

        if (playerTeleport.hasMoved()) {
            plugin.sendMessage(player, Messages.TELEPORT_MOVED);
            playersInTeleporting.remove(player.getName());
            return;
        }

        plugin.sendMessage(player, Messages.TELEPORT_TIMING, String.valueOf(playerTeleport.getCountdown()));
        playerTeleport.decreaseCountdown();

    }

}