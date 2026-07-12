package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.registry.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@SuppressWarnings({"null", "deprecation"})
public final class EnchantmentEffectHelper {
  private EnchantmentEffectHelper() {}

  public static int getTotalLignificationLevel(Player player) {
    int totalLevel = 0;

    for (ItemStack armorStack : player.getArmorSlots()) {
      totalLevel +=
          EnchantmentHelper.getItemEnchantmentLevel(
              ModEnchantments.LIGNIFICATION.get(), armorStack);
    }

    return totalLevel;
  }

  public static int getMaximumLignificationLevel(Player player) {
    int maximumLevel = 0;

    for (ItemStack armorStack : player.getArmorSlots()) {
      int level =
          EnchantmentHelper.getItemEnchantmentLevel(
              ModEnchantments.LIGNIFICATION.get(), armorStack);

      maximumLevel = Math.max(maximumLevel, level);
    }

    return maximumLevel;
  }

  public static boolean hasLignification(Player player) {
    return getTotalLignificationLevel(player) > 0;
  }

  public static boolean isLignificationActive(Player player) {
    return player.isCrouching() && getTotalLignificationLevel(player) > 0;
  }

  public static boolean hasStealth(Player player) {
    return getChestEnchantmentLevel(player, ModEnchantments.STEALTH.get()) > 0;
  }

  public static boolean hasMeditation(Player player) {
    return getMeditationLevel(player) > 0;
  }

  public static int getMeditationLevel(Player player) {
    return getChestEnchantmentLevel(player, ModEnchantments.MEDITATION.get());
  }

  public static boolean isStealthActive(Player player) {
    return player.isCrouching() && hasStealth(player) && !hasMeditation(player);
  }

  private static int getChestEnchantmentLevel(Player player, Enchantment enchantment) {
    return Math.max(
        0,
        EnchantmentHelper.getItemEnchantmentLevel(
            enchantment, player.getItemBySlot(EquipmentSlot.CHEST)));
  }
}
