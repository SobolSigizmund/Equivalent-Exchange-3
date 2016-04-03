package com.pahimar.ee3.init;

import com.pahimar.ee3.block.*;
import com.pahimar.ee3.block.base.BlockEE;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {

    public static final List<BlockEE> BLOCKS = new ArrayList<>();

    // FIXME I'm broke as F
//    public static final BlockEE alchemicalFuel = new BlockAlchemicalFuel();
    public static final BlockEE calcinator = new BlockCalcinator();
    public static final BlockEE glassBell = new BlockGlassBell();
    public static final BlockEE aludelBase = new BlockAludelBase();
    public static final BlockEE augmentationTable = new BlockAugmentationTable();
    public static final BlockEE researchStation = new BlockResearchStation();

    public static void register() {

        for (BlockEE blockEE : BLOCKS) {
            GameRegistry.register(blockEE);
            GameRegistry.register(new ItemBlock(blockEE).setRegistryName(blockEE.getRegistryName()));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void initModelsAndVariants() {
        BLOCKS.forEach(BlockEE::initModelsAndVariants);
    }
}
