package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.registry.ModEnchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@SuppressWarnings({"null", "deprecation"})
public final class EnchantmentEffectHelper {
  private EnchantmentEffectHelper() {}

  public static boolean hasLignification(Player player) {
    for (ItemStack armorStack : player.getArmorSlots()) {
      int level =
          EnchantmentHelper.getItemEnchantmentLevel(
              ModEnchantments.LIGNIFICATION.get(), armorStack);

      if (level > 0) {
        return true;
      }
    }

    return false;
  }

  public static boolean isLignificationActive(Player player) {
    return player.isCrouching() && hasLignification(player);
  }
}
