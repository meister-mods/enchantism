package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.element.ElementType;

public record ElementUsage(int slot, ElementType element, int amount) {
  public ElementUsage {
    if (slot < 0) {
      throw new IllegalArgumentException("Element slot must not be negative");
    }

    if (element == ElementType.EMPTY) {
      throw new IllegalArgumentException("Element usage cannot contain EMPTY");
    }

    if (amount <= 0) {
      throw new IllegalArgumentException("Element usage amount must be positive");
    }
  }
}
