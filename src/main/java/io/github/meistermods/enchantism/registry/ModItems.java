package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.item.ElementContainerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModItems {
  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, Enchantism.MOD_ID);

  public static final RegistryObject<Item> ELEMENT_CONTAINER =
      ITEMS.register(
          "element_container", () -> new ElementContainerItem(new Item.Properties().stacksTo(1)));

  private ModItems() {}

  public static void register(IEventBus modEventBus) {
    ITEMS.register(modEventBus);
  }
}
