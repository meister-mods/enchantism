package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.element.ElementType;
import io.github.meistermods.enchantism.registry.ModEnchantments;
import java.util.Map;
import java.util.Set;

public final class ModEnchantmentRecipes {
  private ModEnchantmentRecipes() {}

  public static void register() {
    ElementEnchantmentSelector.registerRecipe(
        new ElementEnchantmentRecipe(
            ModEnchantments.LIGNIFICATION.get(),
            Map.of(ElementType.WOOD, 3.0, ElementType.LIFE, 2.0),
            350,
            Map.of(
                ElementType.WATER,
                new CatalystPreference(50, 0.30),
                ElementType.MYSTICAL,
                new CatalystPreference(40, 0.50),
                ElementType.METAL,
                new CatalystPreference(50, -0.25)),
            Set.of(),
            Set.of(ElementType.FIRE),
            100.0,
            12.0));
  }
}
