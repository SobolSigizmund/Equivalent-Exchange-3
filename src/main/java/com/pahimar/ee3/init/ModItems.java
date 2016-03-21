package com.pahimar.ee3.init;

import com.pahimar.ee3.item.ItemPhilosophersStone;
import com.pahimar.ee3.item.base.ItemEE;
import com.pahimar.ee3.reference.Reference;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModItems {

    public static final ItemEE philosophersStone = new ItemPhilosophersStone();

    public static void register() {
        GameRegistry.registerItem(philosophersStone);
    }
}
