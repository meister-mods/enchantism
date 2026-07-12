package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.registry.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@SuppressWarnings({"null", "deprecation"})
public final class EnchantmentEffectHelper {
  private EnchantmentEffectHelper() {}

  public static boolean hasLignification(Player player) {
    return getChestEnchantmentLevel(player, ModEnchantments.LIGNIFICATION.get()) > 0;
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

  public static boolean isLignificationActive(Player player) {
    return player.isCrouching() && hasLignification(player);
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
