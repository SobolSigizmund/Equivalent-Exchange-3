package com.pahimar.ee3.init;

import com.pahimar.ee3.block.*;
import com.pahimar.ee3.block.base.BlockEE;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static final BlockEE alchemicalFuel = new BlockAlchemicalFuel();
    public static final BlockEE calcinator = new BlockCalcinator();
    public static final BlockEE glassBell = new BlockGlassBell();
    public static final BlockEE aludelBase = new BlockAludelBase();
    public static final BlockEE augmentationTable = new BlockAugmentationTable();
    public static final BlockEE researchStation = new BlockResearchStation();

    public static void register() {
//        GameRegistry.registerBlock(alchemicalFuel, ItemBlockAlchemicalFuel.class);
        GameRegistry.registerBlock(calcinator);
        GameRegistry.registerBlock(glassBell);
        GameRegistry.registerBlock(aludelBase);
        GameRegistry.registerBlock(augmentationTable);
        GameRegistry.registerBlock(researchStation);

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(calcinator), 0, new ModelResourceLocation(ModBlocks.calcinator.getRegistryName(), "normal"));
    }
}
