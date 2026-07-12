package io.github.meistermods.enchantism.enchantment;

public record CatalystPreference(int preferredAmount, double maximumModifier) {
  public CatalystPreference {
    if (preferredAmount < 1) {
      throw new IllegalArgumentException("Preferred catalyst amount must be at least 1");
    }
  }
}
