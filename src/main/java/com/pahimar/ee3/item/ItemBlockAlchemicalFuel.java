package com.pahimar.ee3.item;

import com.pahimar.ee3.init.ModBlocks;
import net.minecraft.item.ItemMultiTexture;

public class ItemBlockAlchemicalFuel extends ItemMultiTexture {

    public ItemBlockAlchemicalFuel() {
        super(ModBlocks.alchemicalFuel, ModBlocks.alchemicalFuel, ItemAlchemicalFuel.VARIANTS);
    }
}
