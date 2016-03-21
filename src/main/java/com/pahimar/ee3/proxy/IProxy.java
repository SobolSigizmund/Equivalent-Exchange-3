package com.pahimar.ee3.proxy;

import net.minecraftforge.fml.common.event.*;

public interface IProxy {

    ClientProxy getClientProxy();

    void onServerStarting(FMLServerStartingEvent event);

    void preInit(FMLPreInitializationEvent event);

    void init(FMLInitializationEvent event);

    void postInit(FMLPostInitializationEvent event);

    void onServerStopping(FMLServerStoppingEvent event);
}
