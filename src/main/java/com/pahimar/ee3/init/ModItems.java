package com.pahimar.ee3.init;

import com.pahimar.ee3.item.*;
import com.pahimar.ee3.item.base.ItemEE;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ModItems {

    public static final List<ItemEE> ITEMS = new ArrayList<>();

    public static final ItemEE alchenomicon = new ItemAlchenomicon();
    public static final ItemEE alchemicalBag = new ItemAlchemicalBag();
    public static final ItemEE alchemicalDust = new ItemAlchemicalDust();
    public static final ItemEE alchemicalFuel = new ItemAlchemicalFuel();
    public static final ItemEE chalk = new ItemChalk();
    public static final ItemEE knowledgeScroll = new ItemKnowledgeScroll();
    public static final ItemEE lootBall = new ItemLootBall();
    public static final ItemEE miniumShard = new ItemMiniumShard();
    public static final ItemEE inertStone = new ItemInertStone();
    public static final ItemEE miniumStone = new ItemMiniumStone();
    public static final ItemEE philosophersStone = new ItemPhilosophersStone();

    public static void register() {
        GameRegistry.register(alchenomicon);
        GameRegistry.register(alchemicalBag);
        GameRegistry.register(alchemicalDust);
        GameRegistry.register(alchemicalFuel);
        GameRegistry.register(chalk);
        GameRegistry.register(knowledgeScroll);
        GameRegistry.register(lootBall);
        GameRegistry.register(miniumShard);
        GameRegistry.register(inertStone);
        GameRegistry.register(miniumStone);
        GameRegistry.register(philosophersStone);
    }

    @SideOnly(Side.CLIENT)
    public static void initModelsAndVariants() {
        ITEMS.forEach(ItemEE::initModelsAndVariants);
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemColors() {

        for (ItemEE itemEE : ITEMS) {
            if (itemEE instanceof IItemColor) {
                FMLClientHandler.instance().getClient().getItemColors().registerItemColorHandler(new IItemColor() {
                    @Override
                    public int getColorFromItemstack(ItemStack itemStack, int tintIndex) {
                        return ((IItemColor) itemEE).getColorFromItemstack(itemStack, tintIndex);
                    }
                }, itemEE);
            }
        }
    }
}
