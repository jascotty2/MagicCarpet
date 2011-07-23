package com.Android.magiccarpet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Magic Carpet 1.5
 * Copyright (C) 2011 Android <spparr@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * MagicPlayerListener.java
 * <br /><br />
 * Listens for calls for the magic carpet, makes a carpet when a player logs on, removes one when a player logs off,
 * and moves the carpet when the player moves.
 *
 * @author Android <spparr@gmail.com>
 */
public class MagicPlayerListener extends PlayerListener {

    private MagicCarpet plugin = null;
    boolean crouchDef = false;

    public MagicPlayerListener(MagicCarpet plug) {
        plugin = plug;
    }

    @Override
    //When a player joins the game, if they had a carpet when the logged out it puts it back.
    public void onPlayerJoin(PlayerJoinEvent event) {
        MagicCarpet.carpets.drawCarpet(event.getPlayer().getName());
    }

    @Override
    //When a player quits, it removes the carpet from the server
    public void onPlayerQuit(PlayerQuitEvent event) {
        MagicCarpet.carpets.vanishCarpet(event.getPlayer().getName());
    }

    @Override
    //When a player quits, it removes the carpet from the server
    //also, don't allow kicking for flying while descending
    public void onPlayerKick(PlayerKickEvent event) {
        Player pl = event.getPlayer();
        Carpet carpet = (Carpet) MagicCarpet.carpets.getCarpet(pl.getName());
        if (carpet != null) {
            String reas = event.getReason();
            if (reas != null && reas.equals("Flying is not enabled on this server")) { // && pl.isSneaking()
                // if useing mc, won't kick for flying
                event.setCancelled(true);
            } else {
                carpet.removeCarpet();
            }
        }
    }

    @Override
    //Lets the carpet move with the player
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Carpet carpet = (Carpet) MagicCarpet.carpets.getCarpet(player.getName());
        if (carpet == null) {
            return;
        }
        Location to = event.getTo().clone();
        Location from = event.getFrom().clone();

        to.setY(to.getY() - 1);
        from.setY(from.getY() - 1);

        // andrew http://forums.bukkit.org/posts/348324
        if (from.getX() > to.getX()) {
            to.setX(to.getX() - .5);
            from.setX(from.getX() - .5);
        } else {
            to.setX(to.getX() + .5);
            from.setX(from.getX() + .5);
        }
        if (from.getZ() > to.getZ()) {
            to.setZ(to.getZ() - .5);
            from.setZ(from.getZ() - .5);
        } else {
            to.setZ(to.getZ() + .5);
            from.setZ(from.getZ() + .5);
        }
        //</andrew>

        // ceiling fix
        if(from.getY() < to.getY()) to.setY(to.getY() + .6);

        if (MagicCarpet.carpets.isDecsending(player, crouchDef)) {
            to.setY(to.getY() - 1);
        }

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        carpet.removeCarpet();
        if (plugin.canFly(player)) {
            carpet.currentBlock = to.getBlock();
            carpet.drawCarpet();
        } else {
            MagicCarpet.carpets.removeCarpet(player.getName());
        }
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo().clone();
        Player player = event.getPlayer();
        // Check if the player has a carpet
        Carpet carpet = (Carpet) MagicCarpet.carpets.getCarpet(player.getName());
        if (carpet == null) {
            return;
        }

        // Check if the player moved 1 block
        to.setY(to.getY() - 1);
        Location last = carpet.currentBlock.getLocation();
        if (last.getBlockX() == to.getBlockX()
                && last.getBlockY() == to.getBlockY()
                && last.getBlockZ() == to.getBlockZ()) {
            return;
        }

        // Move the carpet
        carpet.removeCarpet();
        carpet.currentBlock = to.getBlock();
        carpet.drawCarpet();
    }

    @Override
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        // Check if the player has a carpet
        Carpet carpet = (Carpet) MagicCarpet.carpets.getCarpet(player.getName());
        
        if (carpet != null && MagicCarpet.carpets.isDecsending(player, crouchDef)) {
            CarpetHandler.lowerCarpet(carpet);
        }
        
    }
}
