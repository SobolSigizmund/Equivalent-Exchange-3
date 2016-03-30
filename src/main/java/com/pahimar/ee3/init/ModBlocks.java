package com.pahimar.ee3.init;

import com.pahimar.ee3.block.BlockAlchemicalFuel;
import com.pahimar.ee3.block.BlockCalcinator;
import com.pahimar.ee3.block.base.BlockEE;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static final BlockEE alchemicalFuel = new BlockAlchemicalFuel();
    public static final BlockEE calcinator = new BlockCalcinator();

    public static void register() {
//        GameRegistry.registerBlock(alchemicalFuel, ItemBlockAlchemicalFuel.class);
        GameRegistry.registerBlock(calcinator);
    }
}
