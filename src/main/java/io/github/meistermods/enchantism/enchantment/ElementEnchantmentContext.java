package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.element.ElementType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"null"})
public final class ElementEnchantmentContext {
  private final List<ElementUsage> usages;

  private final Map<ElementType, Integer> totalAmounts = new EnumMap<>(ElementType.class);

  public ElementEnchantmentContext(List<ElementUsage> usages) {
    this.usages = List.copyOf(usages);

    for (ElementUsage usage : usages) {
      this.totalAmounts.merge(usage.element(), usage.amount(), Integer::sum);
    }
  }

  public List<ElementUsage> getUsages() {
    return this.usages;
  }

  public int getAmount(ElementType element) {
    return this.totalAmounts.getOrDefault(element, 0);
  }

  public boolean contains(ElementType element) {
    return this.getAmount(element) > 0;
  }

  public int getTotalAmount() {
    int total = 0;

    for (int amount : this.totalAmounts.values()) {
      total += amount;
    }

    return total;
  }

  public Map<ElementType, Integer> getTotalAmounts() {
    return Collections.unmodifiableMap(this.totalAmounts);
  }
}
