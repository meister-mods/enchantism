package io.github.meistermods.enchantism.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;

public record SelectedEnchantment(Enchantment enchantment, int level) {
  public SelectedEnchantment {
    if (level <= 0) {
      throw new IllegalArgumentException("Enchantment level must be positive");
    }
  }
}
