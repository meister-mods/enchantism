package io.github.meistermods.enchantism.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

@SuppressWarnings({"null", "deprecation"})
public final class SwiftBladeEnchantment extends Enchantment {
  private static final EnchantmentCategory ATTACK_COOLDOWN_TOOLS =
      EnchantmentCategory.create(
          "enchantism_attack_cooldown_tools", SwiftBladeEnchantment::hasDefaultAttackSpeedModifier);

  public SwiftBladeEnchantment() {
    super(Rarity.VERY_RARE, ATTACK_COOLDOWN_TOOLS, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
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
    return 5;
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
  public boolean canEnchant(ItemStack stack) {
    return supportsAttackCooldown(stack);
  }

  public static boolean supportsAttackCooldown(ItemStack stack) {
    return !stack.isEmpty() && hasDefaultAttackSpeedModifier(stack.getItem());
  }

  private static boolean hasDefaultAttackSpeedModifier(Item item) {
    return item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND)
        .containsKey(Attributes.ATTACK_SPEED);
  }
}
