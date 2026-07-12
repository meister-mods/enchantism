package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.element.ElementType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.enchantment.Enchantment;

@SuppressWarnings({"null"})
public final class ElementEnchantmentRecipe {
  private final Enchantment enchantment;

  private final Map<ElementType, Double> preferredRatio;

  private final int preferredAmount;

  private final Map<ElementType, CatalystPreference> catalysts;

  private final Set<ElementType> requiredCatalysts;

  private final Set<ElementType> forbiddenCatalysts;

  private final double baseScore;
  private final double ratioSensitivity;

  public ElementEnchantmentRecipe(
      Enchantment enchantment,
      Map<ElementType, Double> preferredRatio,
      int preferredAmount,
      Map<ElementType, CatalystPreference> catalysts,
      Set<ElementType> requiredCatalysts,
      Set<ElementType> forbiddenCatalysts,
      double baseScore,
      double ratioSensitivity) {
    this.enchantment = enchantment;

    this.preferredRatio = normalizeRatios(preferredRatio);

    this.preferredAmount = preferredAmount;

    this.catalysts = new EnumMap<>(catalysts);

    this.requiredCatalysts = Set.copyOf(requiredCatalysts);

    this.forbiddenCatalysts = Set.copyOf(forbiddenCatalysts);

    this.baseScore = baseScore;

    this.ratioSensitivity = ratioSensitivity;
  }

  public Enchantment enchantment() {
    return this.enchantment;
  }

  public Map<ElementType, Double> preferredRatio() {
    return this.preferredRatio;
  }

  public int preferredAmount() {
    return this.preferredAmount;
  }

  public Map<ElementType, CatalystPreference> catalysts() {
    return this.catalysts;
  }

  public Set<ElementType> requiredCatalysts() {
    return this.requiredCatalysts;
  }

  public Set<ElementType> forbiddenCatalysts() {
    return this.forbiddenCatalysts;
  }

  public double baseScore() {
    return this.baseScore;
  }

  public double ratioSensitivity() {
    return this.ratioSensitivity;
  }

  private static Map<ElementType, Double> normalizeRatios(Map<ElementType, Double> ratios) {
    double total = ratios.values().stream().mapToDouble(Double::doubleValue).sum();

    if (total <= 0.0) {
      throw new IllegalArgumentException("Preferred ratio total must be positive");
    }

    Map<ElementType, Double> normalized = new EnumMap<>(ElementType.class);

    for (Map.Entry<ElementType, Double> entry : ratios.entrySet()) {
      normalized.put(entry.getKey(), entry.getValue() / total);
    }

    return Map.copyOf(normalized);
  }
}
