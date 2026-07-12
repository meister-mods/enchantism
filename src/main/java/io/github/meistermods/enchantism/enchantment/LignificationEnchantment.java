package io.github.meistermods.enchantism.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class LignificationEnchantment extends Enchantment {
  public LignificationEnchantment() {
    super(
        Rarity.RARE,
        EnchantmentCategory.ARMOR,
        new EquipmentSlot[] {
          EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        });
  }

  @Override
  public int getMinCost(int level) {
    return 20;
  }

  @Override
  public int getMaxCost(int level) {
    return 50;
  }

  @Override
  public int getMaxLevel() {
    return 1;
  }

  @Override
  public boolean isTreasureOnly() {
    return true;
  }

  @Override
  public boolean isDiscoverable() {
    return false;
  }

  @Override
  public boolean isTradeable() {
    return false;
  }
}
