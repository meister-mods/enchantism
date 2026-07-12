package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.element.ElementType;
import java.util.Map;

@SuppressWarnings({"null"})
public final class ElementEnchantmentScorer {
  private ElementEnchantmentScorer() {}

  public static double calculateScore(
      ElementEnchantmentRecipe recipe, ElementEnchantmentContext context) {
    if (!hasAllRequiredCatalysts(recipe, context)) {
      return 0.0;
    }

    if (hasForbiddenCatalyst(recipe, context)) {
      return 0.0;
    }

    int recipeAmount = getRecipeAmount(recipe, context);

    if (recipeAmount <= 0) {
      return 0.0;
    }

    for (ElementType element : recipe.preferredRatio().keySet()) {
      if (!context.contains(element)) {
        return 0.0;
      }
    }

    double ratioFit = calculateRatioFit(recipe, context, recipeAmount);

    /*
     * Remove insignificant matches so unrelated
     * enchantments do not accumulate small scores.
     */
    if (ratioFit < 0.05) {
      return 0.0;
    }

    double amountFactor = Math.min(1.0, (double) recipeAmount / recipe.preferredAmount());

    double catalystFactor = calculateCatalystFactor(recipe, context);

    return recipe.baseScore() * ratioFit * amountFactor * catalystFactor;
  }

  private static int getRecipeAmount(
      ElementEnchantmentRecipe recipe, ElementEnchantmentContext context) {
    int amount = 0;

    for (ElementType element : recipe.preferredRatio().keySet()) {
      amount += context.getAmount(element);
    }

    return amount;
  }

  private static double calculateRatioFit(
      ElementEnchantmentRecipe recipe, ElementEnchantmentContext context, int recipeAmount) {
    double error = 0.0;

    for (Map.Entry<ElementType, Double> entry : recipe.preferredRatio().entrySet()) {
      double actualRatio = (double) context.getAmount(entry.getKey()) / recipeAmount;

      double targetRatio = entry.getValue();

      error += Math.abs(actualRatio - targetRatio);
    }

    return Math.exp(-recipe.ratioSensitivity() * error * error);
  }

  private static double calculateCatalystFactor(
      ElementEnchantmentRecipe recipe, ElementEnchantmentContext context) {
    double modifier = 0.0;

    for (Map.Entry<ElementType, CatalystPreference> entry : recipe.catalysts().entrySet()) {
      ElementType catalyst = entry.getKey();

      /*
       * Recipe elements are not catalysts.
       */
      if (recipe.preferredRatio().containsKey(catalyst)) {
        continue;
      }

      int actualAmount = context.getAmount(catalyst);

      if (actualAmount <= 0) {
        continue;
      }

      CatalystPreference preference = entry.getValue();

      double satisfaction = Math.min(1.0, (double) actualAmount / preference.preferredAmount());

      modifier += preference.maximumModifier() * satisfaction;
    }

    return clamp(1.0 + modifier, 0.0, 2.0);
  }

  private static boolean hasAllRequiredCatalysts(
      ElementEnchantmentRecipe recipe, ElementEnchantmentContext context) {
    for (ElementType element : recipe.requiredCatalysts()) {
      if (!context.contains(element)) {
        return false;
      }
    }

    return true;
  }

  private static boolean hasForbiddenCatalyst(
      ElementEnchantmentRecipe recipe, ElementEnchantmentContext context) {
    for (ElementType element : recipe.forbiddenCatalysts()) {
      if (context.contains(element)) {
        return true;
      }
    }

    return false;
  }

  private static double clamp(double value, double minimum, double maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }
}
