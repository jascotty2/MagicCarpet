/**
 * Programmer: Jacob Scott
 * Program Name: CarpetHandler
 * Description:
 * Date: Jun 7, 2011
 */
package com.Android.magiccarpet;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * @author jacob
 */
public class CarpetHandler {

    private HashMap<String, Carpet> carpets = new HashMap<String, Carpet>();
    private ArrayList<String> crouchers = new ArrayList<String>();

    public CarpetHandler() {
    } // end default constructor

    public Carpet getCarpet(String userName) {
        return carpets.get(userName);
    }

    public void drawCarpet(String userName) {
        Carpet c = carpets.get(userName);
        if (c != null) {
            c.drawCarpet();
        }
    }

    public void vanishCarpet(String userName) {
        Carpet c = carpets.get(userName);
        if (c != null) {
            c.removeCarpet();
        }
    }

    public void removeCarpet(String userName) {
        Carpet c = carpets.remove(userName);
        if (c != null) {
            c.removeCarpet();
        }
    }

    public void lowerCarpet(String userName) {
        lowerCarpet(carpets.get(userName));
    }

    public static void lowerCarpet(Carpet c) {
        if (c != null) {
            c.removeCarpet();
            c.currentBlock = c.currentBlock.getRelative(BlockFace.DOWN);//(0, -1, 0);
            c.drawCarpet();
        }
    }
    
    public void newCarpet(Player pl, int size, boolean lit, boolean glowCenter) {
        if (pl != null) {
            Carpet newCarpet = new Carpet(glowCenter);
            newCarpet.currentBlock = pl.getLocation().getBlock().getRelative(BlockFace.DOWN);
            newCarpet.setSize(size);
            newCarpet.setLights(lit);
            carpets.put(pl.getName(), newCarpet);
        }
    }

    public HashMap<String, Carpet> getCarpets() {
        return carpets;
    }
//
//    public void setCarpets(HashMap<String, Carpet> carp) {
//        carpets = carp;
//    }

    /**
     * removes all carpets & clears list
     */
    public void clearCarpets() {
        for (String name : carpets.keySet()) {
            Carpet c = carpets.get(name);
            c.removeCarpet();
        }
        carpets.clear();
    }

    /**
     * changes how the specified user descends
     * @param name
     * @return the new value (without setting modifier)
     */
    public boolean CarpetSwitch(String name) {
        if (crouchers.contains(name)) {
            crouchers.remove(name);
            return false;
        } else {
            crouchers.add(name);
            return true;
        }
    }

    /**
     *
     * @param name
     * @return whether this user uses the alternate descending method
     */
    public boolean isAltDescend(String name) {
        return crouchers.contains(name);
    }

    public boolean isDecsending(Player pl, boolean crouchDef) {
        Location l = pl.getLocation();
        return pl != null && ((!crouchDef && (crouchers.contains(pl.getName()) ? pl.isSneaking() : l.getPitch() == 90))
                || (crouchDef && (crouchers.contains(pl.getName()) ? l.getPitch() == 90 : pl.isSneaking())));
    }
} // end class CarpetHandler

