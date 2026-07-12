package io.github.meistermods.enchantism.element;

import java.util.EnumMap;
import java.util.Map;

public final class ElementMaterialDefinition {
  private final ElementType defaultElement;

  private final Map<ElementType, Integer> amounts = new EnumMap<>(ElementType.class);

  public ElementMaterialDefinition(ElementType defaultElement, int defaultAmount) {
    if (defaultElement == ElementType.EMPTY) {
      throw new IllegalArgumentException("Default element cannot be EMPTY");
    }

    if (defaultAmount < 1) {
      throw new IllegalArgumentException("Element amount must be at least 1");
    }

    this.defaultElement = defaultElement;
    this.amounts.put(defaultElement, defaultAmount);
  }

  public ElementMaterialDefinition add(ElementType element, int amount) {
    if (element == ElementType.EMPTY) {
      throw new IllegalArgumentException("Element cannot be EMPTY");
    }

    if (amount < 1) {
      throw new IllegalArgumentException("Element amount must be at least 1");
    }

    this.amounts.put(element, amount);

    return this;
  }

  public ElementMaterialData resolve(ElementType containerElement) {
    ElementType selectedElement =
        containerElement == ElementType.EMPTY ? this.defaultElement : containerElement;

    Integer amount = this.amounts.get(selectedElement);

    if (amount == null) {
      return null;
    }

    return new ElementMaterialData(selectedElement, amount);
  }

  public ElementMaterialData getDefaultData() {
    int amount = this.amounts.get(this.defaultElement);

    return new ElementMaterialData(this.defaultElement, amount);
  }
}
