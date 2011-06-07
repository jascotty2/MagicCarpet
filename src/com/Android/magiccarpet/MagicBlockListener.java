package com.Android.magiccarpet;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class MagicBlockListener extends BlockListener {

    CarpetHandler carpets;

    public MagicBlockListener(CarpetHandler play) {
        carpets = play;
    }

    @Override
    //When glowstone from a carpet is broken, don't drop free dust
    public void onBlockBreak(BlockBreakEvent event) {
        for (Carpet carpet : carpets.getCarpets().values()) {
            if (carpet != null && carpet.checkGlowstone(event.getBlock())) {
                event.getBlock().setTypeId(0);
            }
        }
    }
}
