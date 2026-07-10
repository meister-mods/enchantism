package io.github.meistermods.enchantism;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Enchantism.MOD_ID)
public final class Enchantism
{
    public static final String MOD_ID = "enchantism";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Enchantism(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Mod lifecycle events
        modEventBus.addListener(this::commonSetup);

        // Forge gameplay and server events
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Enchantism common setup started.");
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event)
    {
        LOGGER.info("Enchantism server started.");
    }
}