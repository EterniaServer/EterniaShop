package br.com.eterniaserver.eterniashop.baseobjects;

import br.com.eterniaserver.eterniashop.EterniaShop;
import br.com.eterniaserver.eterniashop.enums.Integers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerTeleport {

    private final Player player;
    private final Location firstLocation;
    private final Location wantLocation;
    private final String message;
    private int cooldown;

    public PlayerTeleport(final Player player, final Location wantLocation, final String message, final EterniaShop plugin) {
        this.player = player;
        this.firstLocation = player.getLocation();
        this.wantLocation = wantLocation;
        this.message = message;
        this.cooldown = plugin.getInt(Integers.TELEPORT_DELAY);
    }

    public boolean hasMoved() {
        final Location location = player.getLocation();
        return !(firstLocation.getBlockX() == location.getBlockX() && firstLocation.getBlockY() == location.getBlockY() && firstLocation.getBlockZ() == location.getBlockZ());
    }

    public int getCountdown() {
        return cooldown;
    }

    public void decreaseCountdown() {
        cooldown -= 1;
    }

    public Location getWantLocation() {
        return wantLocation;
    }

    public String getMessage() {
        return message;
    }

}