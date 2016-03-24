package com.pahimar.ee3.init;

import com.pahimar.ee3.item.*;
import com.pahimar.ee3.item.base.IItemVariantHolder;
import com.pahimar.ee3.item.base.ItemEE;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ModItems {

    public static final List<IItemVariantHolder> ITEM_VARIANT_HOLDERS = new ArrayList<>();

    public static final ItemEE alchenomion = new ItemAlchenomicon();
    public static final ItemEE alchemicalFuel = new ItemAlchemicalFuel();
    public static final ItemEE chalk = new ItemChalk();
    public static final ItemEE knowledgeScroll = new ItemKnowledgeScroll();
    public static final ItemEE lootBall = new ItemLootBall();
    public static final ItemEE miniumShard = new ItemMiniumShard();
    public static final ItemEE inertStone = new ItemInertStone();
    public static final ItemEE miniumStone = new ItemMiniumStone();
    public static final ItemEE philosophersStone = new ItemPhilosophersStone();

    public static void register() {
        GameRegistry.registerItem(alchenomion);
        GameRegistry.registerItem(alchemicalFuel);
        GameRegistry.registerItem(chalk);
        GameRegistry.registerItem(knowledgeScroll);
        GameRegistry.registerItem(lootBall);
        GameRegistry.registerItem(miniumShard);
        GameRegistry.registerItem(inertStone);
        GameRegistry.registerItem(miniumStone);
        GameRegistry.registerItem(philosophersStone);
    }

    @SideOnly(Side.CLIENT)
    public static void initModelsAndVariants() {

        for (IItemVariantHolder itemVariantHolder : ITEM_VARIANT_HOLDERS) {
            itemVariantHolder.getItem().initModelsAndVariants();
        }
    }
}
