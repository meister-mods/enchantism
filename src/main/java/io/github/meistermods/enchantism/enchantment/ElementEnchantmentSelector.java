package io.github.meistermods.enchantism.enchantment;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import io.github.meistermods.enchantism.element.ElementType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;

@SuppressWarnings({"null", "deprecation"})
public final class ElementEnchantmentSelector {
  /*
   * This weight always participates in selection.
   *
   * When selected, the applicator produces a vanilla
   * enchantment based on the dominant input element.
   */
  private static final double FALLBACK_SCORE = 100.0;

  /*
   * The maximum total amount consumed by all nine slots:
   *
   * 100 + 90 + 80 + 70 + 60 + 50 + 40 + 30 + 20 = 540
   */
  private static final int MAX_TOTAL_ELEMENT_AMOUNT = 540;

  /*
   * Custom enchantment recipes are registered here
   * after the enchantment registry has been initialized.
   */
  private static final List<ElementEnchantmentRecipe> RECIPES = new ArrayList<>();

  /*
   * Vanilla fallback pools.
   *
   * Strings are used instead of direct Enchantments constants
   * so that the pools remain easy to edit and inspect.
   */
  private static final Map<ElementType, List<String>> FALLBACK_ENCHANTMENT_IDS =
      new EnumMap<>(ElementType.class);

  static {
    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.STONE,
        List.of("efficiency", "unbreaking", "protection", "blast_protection", "knockback"));

    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.WOOD, List.of("efficiency", "unbreaking", "power", "punch"));

    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.DUST, List.of("fortune", "looting", "multishot", "quick_charge", "infinity"));

    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.METAL,
        List.of("sharpness", "protection", "efficiency", "unbreaking", "sweeping"));

    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.CRYSTAL, List.of("fortune", "silk_touch", "mending", "channeling", "loyalty"));

    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.LIFE,
        List.of("mending", "thorns", "respiration", "feather_falling", "looting"));

    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.WATER,
        List.of("respiration", "aqua_affinity", "depth_strider", "riptide", "loyalty", "impaling"));

    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.FIRE, List.of("fire_aspect", "flame", "fire_protection", "sharpness", "power"));

    FALLBACK_ENCHANTMENT_IDS.put(
        ElementType.MYSTICAL,
        List.of("mending", "frost_walker", "soul_speed", "swift_sneak", "channeling"));
  }

  private ElementEnchantmentSelector() {}

  /**
   * Registers a custom enchantment recipe.
   *
   * <p>This should be called after mod enchantments have been registered.
   */
  public static void registerRecipe(ElementEnchantmentRecipe recipe) {
    if (recipe == null) {
      throw new IllegalArgumentException("Element enchantment recipe cannot be null");
    }

    RECIPES.add(recipe);
  }

  /**
   * Removes all registered custom recipes.
   *
   * <p>Mainly useful during development or data reloading.
   */
  public static void clearRecipes() {
    RECIPES.clear();
  }

  /** Selects either a custom enchantment or a dominant-element vanilla fallback enchantment. */
  public static SelectedEnchantment select(ElementEnchantmentContext context, RandomSource random) {
    List<ScoredRecipe> scoredRecipes = calculateRecipeScores(context);

    double customScoreTotal = calculateTotalScore(scoredRecipes);

    double totalSelectionScore = FALLBACK_SCORE + customScoreTotal;

    /*
     * FALLBACK_SCORE is always positive, so totalSelectionScore
     * should never be zero under normal conditions.
     */
    if (!Double.isFinite(totalSelectionScore) || totalSelectionScore <= 0.0) {
      return selectFallback(context, random);
    }

    double roll = random.nextDouble() * totalSelectionScore;

    /*
     * The first 100 points belong to the fallback result.
     */
    if (roll < FALLBACK_SCORE) {
      return selectFallback(context, random);
    }

    roll -= FALLBACK_SCORE;

    for (ScoredRecipe scoredRecipe : scoredRecipes) {
      if (roll < scoredRecipe.score()) {
        return createSelection(scoredRecipe.recipe().enchantment(), context, random);
      }

      roll -= scoredRecipe.score();
    }

    /*
     * Floating-point rounding can leave a tiny unmatched
     * interval, so fall back safely instead of returning null.
     */
    return selectFallback(context, random);
  }

  private static List<ScoredRecipe> calculateRecipeScores(ElementEnchantmentContext context) {
    List<ScoredRecipe> scoredRecipes = new ArrayList<>();

    for (ElementEnchantmentRecipe recipe : RECIPES) {
      Enchantment enchantment = recipe.enchantment();

      if (enchantment == null) {
        continue;
      }

      if (!enchantment.isAllowedOnBooks()) {
        continue;
      }

      double score = ElementEnchantmentScorer.calculateScore(recipe, context);

      /*
       * Zero-scored enchantments do not participate.
       * Non-finite values are discarded defensively.
       */
      if (!Double.isFinite(score) || score <= 0.0) {
        continue;
      }

      scoredRecipes.add(new ScoredRecipe(recipe, score));
    }

    return scoredRecipes;
  }

  private static double calculateTotalScore(List<ScoredRecipe> scoredRecipes) {
    double total = 0.0;

    for (ScoredRecipe scoredRecipe : scoredRecipes) {
      total += scoredRecipe.score();
    }

    return total;
  }

  /**
   * Produces a vanilla enchantment based on the element with the greatest total consumed amount.
   */
  private static SelectedEnchantment selectFallback(
      ElementEnchantmentContext context, RandomSource random) {
    ElementType dominantElement = context.chooseDominantElement(random);

    List<Enchantment> candidates = collectFallbackCandidates(dominantElement);

    /*
     * If an element-specific pool cannot be resolved,
     * use all valid book enchantments as a final fallback.
     */
    if (candidates.isEmpty()) {
      candidates = collectGeneralFallbackCandidates();
    }

    if (candidates.isEmpty()) {
      return null;
    }

    Enchantment selected = candidates.get(random.nextInt(candidates.size()));

    return createSelection(selected, context, random);
  }

  private static List<Enchantment> collectFallbackCandidates(ElementType element) {
    List<String> enchantmentIds = FALLBACK_ENCHANTMENT_IDS.get(element);

    if (enchantmentIds == null || enchantmentIds.isEmpty()) {
      return List.of();
    }

    List<Enchantment> candidates = new ArrayList<>();

    for (String enchantmentId : enchantmentIds) {
      Enchantment enchantment = resolveVanillaEnchantment(enchantmentId);

      if (enchantment == null) {
        continue;
      }

      if (!enchantment.isAllowedOnBooks()) {
        continue;
      }

      candidates.add(enchantment);
    }

    return candidates;
  }

  /**
   * Final fallback used only when the dominant element pool contains no resolvable enchantments.
   */
  private static List<Enchantment> collectGeneralFallbackCandidates() {
    List<Enchantment> candidates = new ArrayList<>();

    for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
      if (!enchantment.isDiscoverable()) {
        continue;
      }

      if (!enchantment.isAllowedOnBooks()) {
        continue;
      }

      candidates.add(enchantment);
    }

    return candidates;
  }

  private static Enchantment resolveVanillaEnchantment(String path) {
    ResourceLocation location = ResourceLocation.tryParse("minecraft:" + path);

    if (location == null) {
      return null;
    }

    return BuiltInRegistries.ENCHANTMENT.getOptional(location).orElse(null);
  }

  private static SelectedEnchantment createSelection(
      Enchantment enchantment, ElementEnchantmentContext context, RandomSource random) {
    int level = selectLevel(enchantment, context, random);

    return new SelectedEnchantment(enchantment, level);
  }

  /**
   * Determines the maximum possible result level from the total amount consumed by the nine
   * applicator slots.
   *
   * <p>More element usage unlocks higher enchantment levels, but the actual level is still randomly
   * selected between the enchantment minimum and the currently unlocked maximum.
   */
  private static int selectLevel(
      Enchantment enchantment, ElementEnchantmentContext context, RandomSource random) {
    int minimumLevel = enchantment.getMinLevel();

    int maximumLevel = enchantment.getMaxLevel();

    if (minimumLevel >= maximumLevel) {
      return minimumLevel;
    }

    int totalAmount = Math.min(context.getTotalAmount(), MAX_TOTAL_ELEMENT_AMOUNT);

    double quality = (double) totalAmount / MAX_TOTAL_ELEMENT_AMOUNT;

    int levelRange = maximumLevel - minimumLevel;

    /*
     * quality 0.0 -> minimum level only
     * quality 1.0 -> full maximum level unlocked
     */
    int unlockedAdditionalLevels = (int) Math.floor(quality * (levelRange + 1));

    unlockedAdditionalLevels = Math.min(levelRange, unlockedAdditionalLevels);

    int unlockedMaximumLevel = minimumLevel + unlockedAdditionalLevels;

    if (unlockedMaximumLevel <= minimumLevel) {
      return minimumLevel;
    }

    return minimumLevel + random.nextInt(unlockedMaximumLevel - minimumLevel + 1);
  }

  private record ScoredRecipe(ElementEnchantmentRecipe recipe, double score) {}
}
