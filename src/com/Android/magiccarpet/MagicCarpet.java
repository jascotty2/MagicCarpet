package com.Android.magiccarpet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.file.FileConfiguration;

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
	private FileConfiguration config;
	private File confFile;
	private ArrayList<String> owners = new ArrayList<String>();
	private ArrayList<String> bums = new ArrayList<String>();
	private boolean all_can_fly = true;
	private boolean crouchDef = true;
	private boolean glowCenter = true;
	protected static int blockID = 20;
	protected static byte blockDat = 0;

	@Override
	public void onEnable() {
		config = getConfig();
		confFile = new File(this.getDataFolder(), "config.yml");
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		loadConfig();

		getLogger().info("Take yourself wonder by wonder, using /magiccarpet or /mc. ");
		if (all_can_fly) {
			getLogger().info("Anyone can use the Magic Carpet.");
		}
		if (!bums.isEmpty()) {
			getLogger().info("Ignore: " + bums.toString());
		}
		if (!all_can_fly && !owners.isEmpty()) {
			getLogger().info("Restricted to: " + owners.toString());
		}
		getServer().getPluginManager().registerEvents(playerListener, this);
	}

	public void loadConfig() {
		if (confFile.exists()) {
			try {
				config.load(confFile);
				crouchDef = config.getBoolean("Crouch Default", crouchDef);
				glowCenter = config.getBoolean("Put glowstone for light in center", glowCenter);
				blockID = config.getInt("Default Block Type", blockID);
				blockDat = (byte) config.getInt("Default Block Type Data", blockID);
				all_can_fly = config.getBoolean("all_can_fly", all_can_fly);
				String dreamers = config.getString("can-fly", "");
				if (dreamers.length() > 0) {
					String[] fliers = dreamers.toLowerCase().split(",");
					if (fliers.length > 0) {
						owners = new ArrayList<String>(Arrays.asList(fliers));
					}
				}

				String paupers = config.getString("cannot-fly", "");
				if (paupers.length() > 0) {
					String[] penniless = paupers.toLowerCase().split(",");
					if (penniless.length > 0) {
						bums = new ArrayList<String>(Arrays.asList(penniless));
					}
				}
			} catch (Exception ex) {
				getLogger().log(Level.SEVERE, null, ex);
			}
		}

		if (blockID >= 256 || blockID <= 0 || org.bukkit.Material.getMaterial(blockID) == null) {
			blockID = 20;
			blockDat = 0;
		} else if (blockDat > 15) {
			blockDat = 0;
		}
		saveConfig();
	}

	@Override
	public void saveConfig() {
		config.set("Crouch Default", crouchDef);
		config.set("Put glowstone for light in center", glowCenter);
		config.set("Default Block Type", blockID);
		config.set("Default Block Type Data", blockID);
		config.set("all_can_fly", all_can_fly);
		if (!config.isSet("can-fly")) {
			config.set("can-fly", "trusted_users_here,maybe_here_too");
		}
		if (!config.isSet("cannot-fly")) {
			config.set("cannot-fly", "untrusted_users_here,maybe_here_too");
		}
		playerListener.crouchDef = crouchDef;
		try {
			config.save(confFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void onDisable() {
		carpets.clearCarpets();
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
		Carpet carpet = carpets.getCarpet(player.getName());

		if (commandName.equals("mc") || commandName.equals("magiccarpet")) {
			if (canFly(player)) {
				if (carpet == null) {
					if (split.length < 1) {
						player.sendMessage("A glass carpet appears below your feet.");
						carpets.newCarpet(player, c, false, glowCenter);
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
						carpets.newCarpet(player, c, false, glowCenter);
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
		} else if (commandName.equals("ml")) {
			if (canLight(player)) {
				if (carpet != null) {
					if (carpet.lights) {
						player.sendMessage("The luminous stones in the carpet slowly fade away.");
						carpet.setLights(false);
					} else {
						player.sendMessage("A bright flash shines as glowing stones appear in the carpet.");
						carpet.setLights(true);
					}
				} else {
					player.sendMessage("A glowing glass carpet appears below your feet.");
					carpets.newCarpet(player, c, true, glowCenter);
				}
			} else {
				player.sendMessage("You do not have permission to use Magic Light!");
			}
			return true;
		} else if (commandName.equals("carpetswitch") || commandName.equals("mcs")) {
			if (canFly(player)) {
				boolean crouch = carpets.CarpetSwitch(player.getName());
				if (!crouchDef) {
					if (crouch) {
						player.sendMessage("You now crouch to descend");
					} else {
						player.sendMessage("You now look down to descend");
					}
				} else if (!crouch) {
					player.sendMessage("You now crouch to descend");
				} else {
					player.sendMessage("You now look down to descend");
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean canFly(Player player) {
		if (bums.contains(player.getName().toLowerCase())) {
			return false;
		} else if (all_can_fly || player.isOp()) {
			return true;
		} else if (player.isPermissionSet("magiccarpet.mc")) {
			return player.hasPermission("magiccarpet.mc");
		} else {
			return owners.contains(player.getName().toLowerCase());
		}
	}

	private boolean canLight(Player player) {
		if (player.isPermissionSet("magiccarpet.ml")) {
			return player.hasPermission("magiccarpet.ml");
		} else {
			return true;
		}
	}
}
