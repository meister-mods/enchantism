package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.enchantment.LignificationEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {
  public static final DeferredRegister<Enchantment> ENCHANTMENTS =
      DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Enchantism.MOD_ID);

  public static final RegistryObject<Enchantment> LIGNIFICATION =
      ENCHANTMENTS.register("lignification", LignificationEnchantment::new);

  private ModEnchantments() {}

  public static void register(IEventBus modEventBus) {
    ENCHANTMENTS.register(modEventBus);
  }
}
