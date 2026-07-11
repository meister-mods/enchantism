package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModEnchantments {
  public static final DeferredRegister<Enchantment> ENCHANTMENTS =
      DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Enchantism.MOD_ID);

  private ModEnchantments() {}

  public static void register(IEventBus modEventBus) {
    ENCHANTMENTS.register(modEventBus);
  }
}
