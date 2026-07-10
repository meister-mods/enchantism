package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.menu.SpecialEnchantmentMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus
{
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(
            ForgeRegistries.MENU_TYPES,
            Enchantism.MOD_ID
        );

    public static final RegistryObject<MenuType<SpecialEnchantmentMenu>>
        SPECIAL_ENCHANTMENT = MENUS.register(
            "special_enchantment",
            () -> new MenuType<>(
                SpecialEnchantmentMenu::new,
                FeatureFlags.DEFAULT_FLAGS
            )
        );

    private ModMenus()
    {
    }

    public static void register(IEventBus modEventBus)
    {
        MENUS.register(modEventBus);
    }
}
