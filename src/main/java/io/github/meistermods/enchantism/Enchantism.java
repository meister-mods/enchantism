package io.github.meistermods.enchantism;

import io.github.meistermods.enchantism.registry.ModBlockEntities;
import io.github.meistermods.enchantism.registry.ModBlocks;
import io.github.meistermods.enchantism.registry.ModCreativeTabs;
import io.github.meistermods.enchantism.registry.ModEnchantments;
import io.github.meistermods.enchantism.registry.ModItems;
import io.github.meistermods.enchantism.registry.ModMenus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Enchantism.MOD_ID)
public final class Enchantism
{
    public static final String MOD_ID = "enchantism";

    public Enchantism(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModEnchantments.register(modEventBus);
    }
}
