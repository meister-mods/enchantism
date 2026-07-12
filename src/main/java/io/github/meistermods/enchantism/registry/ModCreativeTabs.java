package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.element.ElementType;
import io.github.meistermods.enchantism.item.ElementContainerItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModCreativeTabs {
  public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Enchantism.MOD_ID);

  public static final RegistryObject<CreativeModeTab> ENCHANTISM_TAB =
      CREATIVE_TABS.register(
          "enchantism",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.enchantism"))
                  .icon(() -> ModBlocks.SPECIAL_ENCHANTMENT_TABLE_ITEM.get().getDefaultInstance())
                  .displayItems(
                      (parameters, output) -> {
                        output.accept(ModBlocks.SPECIAL_ENCHANTMENT_TABLE_ITEM.get());
                        output.accept(ModBlocks.ENCHANTMENT_APPLICATOR_ITEM.get());
                        output.accept(ModBlocks.ELEMENT_INFUSER_ITEM.get());
                        output.accept(ModItems.ELEMENT_CONTAINER.get());
                        output.accept(ModBlocks.COMPRESSED_COBBLESTONE_ITEM.get());

                        ItemStack emptyContainer = new ItemStack(ModItems.ELEMENT_CONTAINER.get());

                        output.accept(emptyContainer);

                        for (ElementType elementType : ElementType.values()) {
                          if (elementType == ElementType.EMPTY) {
                            continue;
                          }

                          ItemStack filledContainer =
                              new ItemStack(ModItems.ELEMENT_CONTAINER.get());

                          ElementContainerItem.setElement(
                              filledContainer,
                              elementType,
                              ElementContainerItem.MAX_ELEMENT_AMOUNT);

                          output.accept(filledContainer);
                        }
                      })
                  .build());

  private ModCreativeTabs() {}

  public static void register(IEventBus modEventBus) {
    CREATIVE_TABS.register(modEventBus);
  }
}
