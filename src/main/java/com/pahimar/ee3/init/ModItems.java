package com.pahimar.ee3.init;

import com.pahimar.ee3.item.ItemPhilosophersStone;
import com.pahimar.ee3.item.base.IItemVariantHolder;
import com.pahimar.ee3.item.base.ItemEE;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ModItems {

    public static final List<IItemVariantHolder> ITEM_VARIANT_HOLDERS = new ArrayList<>();

    public static final ItemEE philosophersStone = new ItemPhilosophersStone();

    public static void register() {
        GameRegistry.registerItem(philosophersStone);
    }

    @SideOnly(Side.CLIENT)
    public static void initModelsAndVariants() {

        for (IItemVariantHolder itemVariantHolder : ITEM_VARIANT_HOLDERS) {
            itemVariantHolder.getItem().initModelsAndVariants();
        }
    }
}
