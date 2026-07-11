package io.github.meistermods.enchantism.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Base class for all Enchantism-exclusive enchantments.
 *
 * <p>Special enchantments: - are excluded from the vanilla enchanting table; - are excluded from
 * villager enchanted-book trades; - cannot normally be stored on enchanted books; - define which
 * material activates them in the special enchanting table.
 */
public abstract class SpecialEnchantment extends Enchantment {
  protected SpecialEnchantment(
      Rarity rarity, EnchantmentCategory category, EquipmentSlot... applicableSlots) {
    super(rarity, category, applicableSlots);
  }

  /**
   * Prevents this enchantment from being selected by the vanilla enchanting-table algorithm and
   * other normal random-enchantment sources.
   */
  @Override
  public final boolean isDiscoverable() {
    return false;
  }

  /** Prevents enchanted-book villagers from offering this enchantment. */
  @Override
  public final boolean isTradeable() {
    return false;
  }

  /**
   * Prevents the normal enchanted-book route.
   *
   * <p>Remove this override later if special enchanted books should be supported.
   */
  @Override
  public boolean isAllowedOnBooks() {
    return true;
  }

  /**
   * Returns whether the supplied material can generate this enchantment at the special enchanting
   * table.
   */
  public abstract boolean matchesMaterial(ItemStack material);

  /** Weight used when multiple special enchantments match one material. */
  public int getSpecialWeight() {
    return 10;
  }

  public int getGrantedLevel(ItemStack material) {
    return getMinLevel();
  }
}
