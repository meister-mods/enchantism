package io.github.meistermods.enchantism.enchantment;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;

@SuppressWarnings({"null", "deprecation"})
public final class ElementEnchantmentSelector {
  private ElementEnchantmentSelector() {}

  public static SelectedEnchantment select(ElementEnchantmentContext context, RandomSource random) {
    List<Enchantment> candidates = collectCandidates(context);

    if (candidates.isEmpty()) {
      return null;
    }

    Enchantment enchantment = candidates.get(random.nextInt(candidates.size()));

    int level = selectLevel(enchantment, context, random);

    return new SelectedEnchantment(enchantment, level);
  }

  private static List<Enchantment> collectCandidates(ElementEnchantmentContext context) {
    List<Enchantment> candidates = new ArrayList<>();

    for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
      if (!enchantment.isDiscoverable()) {
        continue;
      }

      if (!enchantment.isAllowedOnBooks()) {
        continue;
      }

      candidates.add(enchantment);
    }

    return candidates;
  }

  private static int selectLevel(
      Enchantment enchantment, ElementEnchantmentContext context, RandomSource random) {
    int minimumLevel = enchantment.getMinLevel();

    int maximumLevel = enchantment.getMaxLevel();

    if (minimumLevel >= maximumLevel) {
      return minimumLevel;
    }

    return minimumLevel + random.nextInt(maximumLevel - minimumLevel + 1);
  }
}
