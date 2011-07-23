package com.Android.magiccarpet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.Properties;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

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
public class MagicCarpet extends JavaPlugin {

    protected static CarpetHandler carpets = new CarpetHandler();
    private final MagicPlayerListener playerListener = new MagicPlayerListener(this);
    private final MagicBlockListener blockListener = new MagicBlockListener(carpets);
    private static String config_comment = "Magic Carpet permissions file";
    public PermissionHandler permissions = null;
    private Configuration config;
    private String fileName = "";
    private static final Logger log = Logger.getLogger("Minecraft");
    private ArrayList<String> owners = new ArrayList<String>();
    private ArrayList<String> bums = new ArrayList<String>();
    private ArrayList<String> lights = new ArrayList<String>();
    private boolean ignore = false;
    private boolean all_can_fly = true;
    private boolean crouchDef = true;
    private boolean glowCenter = true;
    protected static int blockID = 20;
    protected static byte blockDat = 0;

    public void onEnable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        String name = pdfFile.getName();
        config = getConfiguration();

        loadConfig();
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        fileName = getDataFolder().getPath() + File.separator + "magiccarpet.properties";

        setupPermissions();

        log.info("[" + name + "] " + name + " version " + pdfFile.getVersion() + " is enabled!");
        log.info("[" + name + "] Take yourself wonder by wonder, using /magiccarpet or /mc. ");
        if (permissions != null) {
            log.info("[" + name + "] Using Permissions.");
        } else if (!all_can_fly) {
            log.info("[" + name + "] Anyone can use the Magic Carpet.");
        } else if (ignore) {
            log.info("[" + name + "] Ignore: " + bums.toString());
        } else {
            log.info("[" + name + "] Restricted to: " + owners.toString());
        }
        registerEvents();
    }

    public void loadConfig() {
        config.load();
        all_can_fly = config.getBoolean("Use Properties Permissions", false);
        crouchDef = config.getBoolean("Crouch Default", true);
        glowCenter = config.getBoolean("Put glowstone for light in center", false);
        blockID = config.getInt("Default Block Type", blockID);
        blockDat = (byte) config.getInt("Default Block Type Data", blockID);

        if (blockID >= 256 || blockID<=0 || org.bukkit.Material.getMaterial(blockID) == null) {
            blockID = 20;
            blockDat = 0;
        } else if (blockDat > 15) {
            blockDat = 0;
        }
        saveConfig();
    }

    public void saveConfig() {
        config.setProperty("Use Properties Permissions", all_can_fly);
        config.setProperty("Crouch Default", crouchDef);
        config.setProperty("Put glowstone for light in center", glowCenter);
        config.setProperty("Default Block Type", blockID);
        config.setProperty("Default Block Type Data", blockID);
        playerListener.crouchDef = crouchDef;
        config.save();
    }

    public void onDisable() {
        carpets.clearCarpets();
        log.info("Magic Carpet disabled. Thanks for trying the plugin!");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TOGGLE_SNEAK, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        String[] split = args;
        String commandName = command.getName().toLowerCase();
        int c = 5;
        Player player = (Player) sender;
        Carpet carpet = (Carpet) carpets.getCarpet(player.getName());

        if (commandName.equals("mc") || commandName.equals("magiccarpet")) {
            if (canFly(player)) {
                if (carpet == null) {
                    if (split.length < 1) {
                        player.sendMessage("A glass carpet appears below your feet.");
                        carpets.newCarpet(player, c, lights.contains(player.getName()), glowCenter);
                    } else {
                        try {
                            c = Integer.valueOf(split[0]);
                        } catch (NumberFormatException e) {
                            player.sendMessage("Correct usage is: /magiccarpet (size) or /mc (size). The size is optional, and can only be 3, 5, or 7!");
                            return false;
                        }

                        if (c != 3 && c != 5 && c != 7) {
                            player.sendMessage("The size can only be 3, 5, or 7. Please enter a proper number");
                            return false;
                        }
                        player.sendMessage("A glass carpet appears below your feet.");
                        carpets.newCarpet(player, c, lights.contains(player.getName()), glowCenter);
                    }

                }
                if (carpet != null) {
                    if (split.length == 1) {
                        try {
                            c = Integer.valueOf(split[0]);
                        } catch (NumberFormatException e) {
                            player.sendMessage("Correct usage is: /magiccarpet (size) or /mc (size). The size is optional, and can only be 3, 5, or 7!");
                            return false;
                        }

                        if (c != 3 && c != 5 && c != 7) {
                            player.sendMessage("The size can only be 3, 5, or 7. Please enter a proper number");
                            return false;
                        }
                        if (c != carpet.size) {
                            player.sendMessage("The carpet seems to react to your words, and suddenly changes shape!");
                            carpet.changeCarpetSize(c);
                        } else {
                            player.sendMessage("Poof! The magic carpet disappears.");
                            carpets.removeCarpet(player.getName());
                        }
                    } else {
                        player.sendMessage("Poof! The magic carpet disappears.");
                        carpets.removeCarpet(player.getName());
                    }

                }
                return true;
            } else {
                player.sendMessage("You shout your command, but it falls on deaf ears. Nothing happens.");
                return true;
            }
        } else {
            if (commandName.equals("ml")) {
                if (canLight(player)) {
                    if (lights.contains(player.getName())) {
                        lights.remove(player.getName());
                        player.sendMessage("The luminous stones in the carpet slowly fade away.");
                        if (carpet != null) {
                            carpet.setLights(false);
                        }
                    } else {
                        lights.add(player.getName());
                        player.sendMessage("A bright flash shines as glowing stones appear in the carpet.");
                        if (carpet != null) {
                            carpet.setLights(true);
                        }
                    }
                } else {
                    player.sendMessage("You do not have permission to use Magic Light!");
                }
                return true;
            } else {
                if (commandName.equals("carpetswitch") || commandName.equals("mcs")) {
                    if (canFly(player)) {
                        boolean crouch = carpets.CarpetSwitch(player.getName());
                        if (!crouchDef) {
                            if (crouch) {
                                player.sendMessage("You now crouch to descend");
                            } else {
                                player.sendMessage("You now look down to descend");
                            }
                        } else {
                            if (!crouch) {
                                player.sendMessage("You now crouch to descend");
                            } else {
                                player.sendMessage("You now look down to descend");
                            }
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public boolean canFly(Player player) {
        if (!all_can_fly) {
            return true;
        }
        if (permissions != null) {
            return permissions.has(player, "magiccarpet.mc");
        } else if (ignore) {
            return !bums.contains(player.getName().toLowerCase());
        } else {
            return owners.contains(player.getName().toLowerCase());
        }
    }

    private boolean canLight(Player player) {
        if (permissions != null) {
            return permissions.has(player, "magiccarpet.ml");
        } else {
            return true;
        }
    }

    public void saveDefaultSettings(boolean trust) {
        Properties props = new Properties();
        if (trust) {
            props.setProperty("can-fly", "trusted_users_here,maybe_here_too");
        } else {
            props.setProperty("cannot-fly", "untrusted_users_here,maybe_here_too");
        }
        try {
            OutputStream propOut = new FileOutputStream(new File(fileName));
            props.store(propOut, config_comment);

        } catch (IOException ioe) {
            System.out.print(ioe.getMessage());
        }
    }

    public void loadSettings() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(fileName));
            if (all_can_fly) {
                if (props.containsKey("can-fly")) {
                    String dreamers = props.getProperty("can-fly", "");
                    ignore = false;
                    if (dreamers.length() > 0) {
                        String[] fliers = dreamers.toLowerCase().split(",");
                        if (fliers.length > 0) {
                            owners = new ArrayList<String>(Arrays.asList(fliers));
                        } else {
                            this.saveDefaultSettings(true);
                        }
                    } else {
                        this.saveDefaultSettings(true);
                    }
                } else {
                    if (props.containsKey("cannot-fly")) {
                        String paupers = props.getProperty("cannot-fly", "");
                        ignore = true;
                        if (paupers.length() > 0) {
                            String[] penniless = paupers.toLowerCase().split(",");
                            if (penniless.length > 0) {
                                bums = new ArrayList<String>(Arrays.asList(penniless));
                            } else {
                                this.saveDefaultSettings(false);
                            }
                        } else {
                            this.saveDefaultSettings(false);
                        }
                    } else {
                        this.saveDefaultSettings(true);
                    }
                }
            } else {
                this.saveDefaultSettings(true);
            }
        } catch (IOException ioe) {
            this.saveDefaultSettings(true);
        }
    }

    public void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");


        if (this.permissions == null) {
            if (test != null) {
                this.permissions = ((Permissions) test).getHandler();
                all_can_fly = true;
            } else {
                log.info("Permission system not detected, defaulting to settings");
            }
        }
        loadSettings();
    }
}
