package com.Android.magiccarpet;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class MagicBlockListener extends BlockListener {

    CarpetHandler carpets;

    public MagicBlockListener(CarpetHandler play) {
        carpets = play;
    }

    @Override
    //When block from a carpet is broken, don't drop free dust
    public void onBlockBreak(BlockBreakEvent event) {
        for (Carpet carpet : carpets.getCarpets().values()) {
            if (carpet != null && carpet.checkCarpet(event.getBlock())) {
                event.setCancelled(true);
                event.getBlock().setTypeId(0);
            }
        }
    }
}
