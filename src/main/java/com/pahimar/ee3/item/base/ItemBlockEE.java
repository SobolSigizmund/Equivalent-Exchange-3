package com.pahimar.ee3.item.base;

import com.google.common.base.Function;
import com.pahimar.ee3.block.base.BlockEE;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;

public abstract class ItemBlockEE extends ItemMultiTexture {

    public ItemBlockEE(BlockEE block, String ... variants) {
        super(block, block, variants);
    }

    public ItemBlockEE(BlockEE block, Function<ItemStack, String> nameFunction) {
        super(block, block, nameFunction);
    }
}
