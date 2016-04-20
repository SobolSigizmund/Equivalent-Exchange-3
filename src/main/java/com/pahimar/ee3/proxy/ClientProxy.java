package com.pahimar.ee3.proxy;

import com.pahimar.ee3.init.ModBlocks;
import com.pahimar.ee3.init.ModItems;
import com.pahimar.ee3.reference.Reference;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public ClientProxy getClientProxy() {
        return this;
    }

    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
        ModItems.initModelsAndVariants();
        ModBlocks.initModelsAndVariants();
        OBJLoader.INSTANCE.addDomain(Reference.MOD_ID);
    }

    @Override
    public void onInit(FMLInitializationEvent event) {
        super.onInit(event);
        ModItems.registerItemColors();
    }
}
