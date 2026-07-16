package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.menu.ElementInfuserMenu;
import io.github.meistermods.enchantism.menu.EnchantmentApplicatorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModMenus {
  public static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(ForgeRegistries.MENU_TYPES, Enchantism.MOD_ID);

  public static final RegistryObject<MenuType<EnchantmentApplicatorMenu>> ENCHANTMENT_APPLICATOR =
      MENUS.register(
          "enchantment_applicator", () -> IForgeMenuType.create(EnchantmentApplicatorMenu::new));

  public static final RegistryObject<MenuType<ElementInfuserMenu>> ELEMENT_INFUSER =
      MENUS.register("element_infuser", () -> IForgeMenuType.create(ElementInfuserMenu::new));

  private ModMenus() {}

  public static void register(IEventBus modEventBus) {
    MENUS.register(modEventBus);
  }
}
