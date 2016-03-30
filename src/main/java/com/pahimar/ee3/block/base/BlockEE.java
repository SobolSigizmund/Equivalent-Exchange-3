package com.pahimar.ee3.block.base;

import com.pahimar.ee3.creativetab.CreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public abstract class BlockEE extends Block {

    public BlockEE(String name) {
        this(name, Material.rock);
    }

    public BlockEE(String name, Material material) {
        super(material);
        setRegistryName(name);
        setUnlocalizedName(name);
        setCreativeTab(CreativeTab.EE3_TAB);
    }
}
