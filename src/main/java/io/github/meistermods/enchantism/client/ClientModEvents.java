package io.github.meistermods.enchantism.client;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.client.screen.SpecialEnchantmentScreen;
import io.github.meistermods.enchantism.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = Enchantism.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ClientModEvents
{
    private ClientModEvents()
    {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
                MenuScreens.register(
                        ModMenus.SPECIAL_ENCHANTMENT.get(),
                        SpecialEnchantmentScreen::new
                )
        );
    }
}
