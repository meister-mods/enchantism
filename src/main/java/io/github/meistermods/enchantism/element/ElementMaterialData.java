package io.github.meistermods.enchantism.element;

@SuppressWarnings({"null"})
public record ElementMaterialData(ElementType elementType, int amount) {
  public ElementMaterialData {
    if (elementType == ElementType.EMPTY) {
      throw new IllegalArgumentException("Material element type cannot be EMPTY");
    }

    if (amount < 1) {
      throw new IllegalArgumentException("Element material amount must be at least 1");
    }
  }
}
