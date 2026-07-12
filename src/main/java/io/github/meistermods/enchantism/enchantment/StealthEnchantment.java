package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.registry.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

@SuppressWarnings({"null"})
public final class StealthEnchantment extends Enchantment {
  public StealthEnchantment() {
    super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[] {EquipmentSlot.CHEST});
  }

  @Override
  public int getMinCost(int level) {
    return 25;
  }

  @Override
  public int getMaxCost(int level) {
    return 55;
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

  @Override
  protected boolean checkCompatibility(Enchantment other) {
    return other != ModEnchantments.MEDITATION.get() && super.checkCompatibility(other);
  }
}
