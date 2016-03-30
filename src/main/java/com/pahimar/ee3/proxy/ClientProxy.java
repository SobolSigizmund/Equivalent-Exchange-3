package com.pahimar.ee3.proxy;

import com.pahimar.ee3.init.ModItems;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public ClientProxy getClientProxy() {
        return this;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ModItems.initModelsAndVariants();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ModItems.registerItemColors();
    }
}
