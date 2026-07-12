package io.github.meistermods.enchantism.client;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.client.screen.ElementInfuserScreen;
import io.github.meistermods.enchantism.client.screen.EnchantmentApplicatorScreen;
import io.github.meistermods.enchantism.client.screen.SpecialEnchantmentScreen;
import io.github.meistermods.enchantism.element.ElementType;
import io.github.meistermods.enchantism.item.ElementContainerItem;
import io.github.meistermods.enchantism.registry.ModItems;
import io.github.meistermods.enchantism.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(
    modid = Enchantism.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public final class ClientModEvents {
  private ClientModEvents() {}

  @SubscribeEvent
  public static void onClientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(
        () -> {
          MenuScreens.register(ModMenus.SPECIAL_ENCHANTMENT.get(), SpecialEnchantmentScreen::new);

          MenuScreens.register(
              ModMenus.ENCHANTMENT_APPLICATOR.get(), EnchantmentApplicatorScreen::new);

          MenuScreens.register(ModMenus.ELEMENT_INFUSER.get(), ElementInfuserScreen::new);
        });
  }

  @SubscribeEvent
  public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    event.register(
        (stack, tintIndex) -> {
          if (tintIndex != 1) {
            return 0xFFFFFF;
          }

          ElementType element = ElementContainerItem.getElement(stack);

          return element.getColor();
        },
        ModItems.ELEMENT_CONTAINER.get());
  }
}
