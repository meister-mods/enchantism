package io.github.meistermods.enchantism.element;

@SuppressWarnings({"null"})
public record ElementMaterialData(ElementType elementType, int amount) {
  public ElementMaterialData {
    if (amount < 1) {
      throw new IllegalArgumentException("Element material amount must be between 1 and 100");
    }
  }
}
