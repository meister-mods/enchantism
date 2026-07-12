package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.element.ElementType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;

@SuppressWarnings({"null"})
public final class ElementEnchantmentContext {
  private final List<ElementUsage> usages;

  private final Map<ElementType, Integer> amounts = new EnumMap<>(ElementType.class);

  private final int totalAmount;

  public ElementEnchantmentContext(List<ElementUsage> usages) {
    this.usages = List.copyOf(usages);

    int calculatedTotal = 0;

    for (ElementUsage usage : usages) {
      this.amounts.merge(usage.element(), usage.amount(), Integer::sum);

      calculatedTotal += usage.amount();
    }

    this.totalAmount = calculatedTotal;
  }

  public List<ElementUsage> usages() {
    return this.usages;
  }

  public int getAmount(ElementType element) {
    return this.amounts.getOrDefault(element, 0);
  }

  public boolean contains(ElementType element) {
    return this.getAmount(element) > 0;
  }

  public int getTotalAmount() {
    return this.totalAmount;
  }

  public Map<ElementType, Integer> getAmounts() {
    return Collections.unmodifiableMap(this.amounts);
  }

  public List<ElementType> getDominantElements() {
    int largestAmount = 0;

    List<ElementType> dominantElements = new ArrayList<>();

    for (Map.Entry<ElementType, Integer> entry : this.amounts.entrySet()) {
      ElementType element = entry.getKey();

      int amount = entry.getValue();

      if (element == ElementType.EMPTY) {
        continue;
      }

      if (amount > largestAmount) {
        largestAmount = amount;

        dominantElements.clear();
        dominantElements.add(element);
      } else if (amount == largestAmount) {
        dominantElements.add(element);
      }
    }

    return dominantElements;
  }

  public ElementType chooseDominantElement(RandomSource random) {
    List<ElementType> dominantElements = this.getDominantElements();

    if (dominantElements.isEmpty()) {
      return ElementType.EMPTY;
    }

    return dominantElements.get(random.nextInt(dominantElements.size()));
  }
}
