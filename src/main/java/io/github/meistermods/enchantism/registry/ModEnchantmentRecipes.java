package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.element.ElementType;
import io.github.meistermods.enchantism.enchantment.CatalystPreference;
import io.github.meistermods.enchantism.enchantment.ElementEnchantmentRecipe;
import io.github.meistermods.enchantism.enchantment.ElementEnchantmentSelector;
import java.util.Map;
import java.util.Set;

public final class ModEnchantmentRecipes {
  private static boolean registered;

  private ModEnchantmentRecipes() {}

  public static void register() {
    if (registered) {
      return;
    }

    registered = true;

    registerEnchants();
  }

  private static void registerEnchants() {
    ElementEnchantmentSelector.registerRecipe(
        new ElementEnchantmentRecipe(
            ModEnchantments.LIGNIFICATION.get(),
            Map.of(ElementType.WOOD, 3.0, ElementType.LIFE, 2.0),
            450,
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

    ElementEnchantmentSelector.registerRecipe(
        new ElementEnchantmentRecipe(
            ModEnchantments.MEDITATION.get(),
            Map.of(
                ElementType.LIFE, 2.0,
                ElementType.MYSTICAL, 1.0),
            360,
            Map.of(
                ElementType.WATER,
                new CatalystPreference(60, 0.40),
                ElementType.CRYSTAL,
                new CatalystPreference(40, 0.30),
                ElementType.FIRE,
                new CatalystPreference(50, -0.50)),
            Set.of(),
            Set.of(ElementType.METAL),
            90.0,
            12.0));

    ElementEnchantmentSelector.registerRecipe(
        new ElementEnchantmentRecipe(
            ModEnchantments.STEALTH.get(),
            Map.of(
                ElementType.DUST, 3.0,
                ElementType.MYSTICAL, 2.0),
            350,
            Map.of(
                ElementType.WATER,
                new CatalystPreference(50, 0.30),
                ElementType.CRYSTAL,
                new CatalystPreference(40, 0.30),
                ElementType.FIRE,
                new CatalystPreference(50, -0.50)),
            Set.of(),
            Set.of(ElementType.METAL),
            90.0,
            12.0));

    ElementEnchantmentSelector.registerRecipe(
        new ElementEnchantmentRecipe(
            ModEnchantments.SWIFT_BLADE.get(),
            Map.of(
                ElementType.METAL, 3.0,
                ElementType.FIRE, 2.0),
            450,
            Map.of(
                ElementType.DUST,
                new CatalystPreference(50, 0.35),
                ElementType.CRYSTAL,
                new CatalystPreference(40, 0.25),
                ElementType.STONE,
                new CatalystPreference(50, -0.30)),
            Set.of(),
            Set.of(ElementType.WATER),
            90.0,
            12.0));
  }
}
