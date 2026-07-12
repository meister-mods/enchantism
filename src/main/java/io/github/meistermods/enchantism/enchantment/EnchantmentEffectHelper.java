package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.registry.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@SuppressWarnings({"null", "deprecation"})
public final class EnchantmentEffectHelper {
  private EnchantmentEffectHelper() {}

  public static boolean hasLignification(Player player) {
  return EnchantmentHelper.getItemEnchantmentLevel(
          ModEnchantments.LIGNIFICATION.get(),
          player.getItemBySlot(EquipmentSlot.CHEST))
      > 0;
}

public static boolean hasMeditation(Player player) {
  return getMeditationLevel(player) > 0;
}

public static boolean hasConflictingMeditationEnchantments(Player player) {
  return hasLignification(player) && hasMeditation(player);
}

  public static boolean isLignificationActive(Player player) {
  return player.isCrouching()
      && hasLignification(player)
      && !hasMeditation(player);
}
  public static int getMeditationLevel(Player player) {
  int level =
      EnchantmentHelper.getItemEnchantmentLevel(
          ModEnchantments.MEDITATION.get(),
          player.getItemBySlot(EquipmentSlot.CHEST));

  return Math.max(0, level);
}
}
