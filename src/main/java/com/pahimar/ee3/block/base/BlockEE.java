package com.pahimar.ee3.block.base;

import com.pahimar.ee3.creativetab.CreativeTab;
import com.pahimar.ee3.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockEE extends Block {

    public BlockEE(String name) {
        this(name, Material.rock);
    }

    public BlockEE(String name, Material material) {
        super(material);
        setRegistryName(name);
        setUnlocalizedName(name);
        setCreativeTab(CreativeTab.EE3_TAB);

        ModBlocks.BLOCKS.add(this);
    }

    @SideOnly(Side.CLIENT)
    public void initModelsAndVariants() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName()));
    }
}
