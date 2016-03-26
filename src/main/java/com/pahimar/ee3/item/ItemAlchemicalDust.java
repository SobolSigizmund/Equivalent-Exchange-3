package com.pahimar.ee3.item;

import com.pahimar.ee3.item.base.ItemEE;
import com.pahimar.ee3.reference.Colors;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemAlchemicalDust extends ItemEE implements IItemColor {

    private static final String NAME = "alchemical_dust";
    private static final String[] VARIANTS = {"ash", "minium_dust"};

    public ItemAlchemicalDust() {
        super(NAME, VARIANTS);
        setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    public void initModelsAndVariants() {

        for (int i = 0; i < VARIANTS.length; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, getCustomModelResourceLocation(NAME));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemstack(ItemStack itemStack, int renderPass) {

        if (itemStack.getMetadata() < Colors.DUST_COLOURS.length) {
            return Integer.parseInt(Colors.DUST_COLOURS[itemStack.getMetadata()], 16);
        }

        return Integer.parseInt(Colors.PURE_WHITE, 16);
    }
}
