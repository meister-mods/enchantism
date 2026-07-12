package io.github.meistermods.enchantism.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

@SuppressWarnings({"null"})
public final class LignificationEnchantment extends Enchantment {
  public LignificationEnchantment() {
    super(
        Rarity.VERY_RARE,
        EnchantmentCategory.ARMOR,
        new EquipmentSlot[] {
          EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        });
  }

  @Override
  public int getMinCost(int level) {
    return 20 + (level - 1) * 15;
  }

  @Override
  public int getMaxCost(int level) {
    return getMinCost(level) + 40;
  }

  @Override
  public int getMaxLevel() {
    return 4;
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
    if (other == Enchantments.ALL_DAMAGE_PROTECTION
        || other == Enchantments.FIRE_PROTECTION
        || other == Enchantments.BLAST_PROTECTION
        || other == Enchantments.PROJECTILE_PROTECTION
        || other == Enchantments.FALL_PROTECTION) {
      return false;
    }

    return super.checkCompatibility(other);
  }
}
