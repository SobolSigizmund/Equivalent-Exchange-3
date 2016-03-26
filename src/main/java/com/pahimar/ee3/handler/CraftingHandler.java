package com.pahimar.ee3.handler;

import com.pahimar.ee3.item.crafting.RecipeAlchemicalBagDyes;
import net.minecraft.item.crafting.CraftingManager;

public class CraftingHandler {

    public static void init() {

        CraftingManager.getInstance().getRecipeList().add(new RecipeAlchemicalBagDyes());
    }
}
