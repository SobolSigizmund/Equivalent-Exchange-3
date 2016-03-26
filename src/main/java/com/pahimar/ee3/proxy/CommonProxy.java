package com.pahimar.ee3.proxy;

import com.pahimar.ee3.handler.ConfigurationHandler;
import com.pahimar.ee3.handler.CraftingHandler;
import com.pahimar.ee3.init.ModItems;
import net.minecraftforge.fml.common.event.*;

public abstract class CommonProxy implements IProxy {

    @Override
    public ClientProxy getClientProxy() {
        return null;
    }

    @Override
    public void onServerStarting(FMLServerStartingEvent event) {

    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        ModItems.register();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        CraftingHandler.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Override
    public void onServerStopping(FMLServerStoppingEvent event) {

    }
}
