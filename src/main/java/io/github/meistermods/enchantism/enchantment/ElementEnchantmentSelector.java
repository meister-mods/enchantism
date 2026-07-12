package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.element.ElementType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;

@SuppressWarnings({"deprecation"})
public final class ElementEnchantmentSelector {
  private static final double FALLBACK_SCORE = 250.0;

  private static final int MAX_TOTAL_ELEMENT_AMOUNT = 450;

  private static final int ADVANCED_MIN_DOMINANT_AMOUNT = 180;
  private static final int ADVANCED_MIN_TOTAL_AMOUNT = 300;

  private static final int RARE_MIN_DOMINANT_AMOUNT = 280;
  private static final int RARE_MIN_TOTAL_AMOUNT = 450;

  private static final int COMMON_TIER_WEIGHT = 100;
  private static final int ADVANCED_TIER_WEIGHT = 35;
  private static final int RARE_TIER_WEIGHT = 10;

  private static final int[] FALLBACK_LEVEL_WEIGHTS = {71, 16, 8, 4, 1};

  private static final List<ElementEnchantmentRecipe> RECIPES = new ArrayList<>();

  private static final Map<ElementType, FallbackPool> FALLBACK_POOLS =
      new EnumMap<>(ElementType.class);

  private static final FallbackPool GENERAL_FALLBACK_POOL =
      new FallbackPool(
          List.of("unbreaking", "protection", "efficiency", "sharpness", "power"),
          List.of(),
          List.of());

  static {
    registerFallbackPools();
  }

  private ElementEnchantmentSelector() {}

  public static void registerRecipe(ElementEnchantmentRecipe recipe) {
    if (recipe == null) {
      throw new IllegalArgumentException("Element enchantment recipe cannot be null");
    }

    RECIPES.add(recipe);
  }

  public static void clearRecipes() {
    RECIPES.clear();
  }

  public static SelectedEnchantment select(ElementEnchantmentContext context, RandomSource random) {
    List<ScoredRecipe> scoredRecipes = calculateRecipeScores(context);

    double customScoreTotal = calculateTotalScore(scoredRecipes);

    double totalSelectionScore = FALLBACK_SCORE + customScoreTotal;

    if (!Double.isFinite(totalSelectionScore) || totalSelectionScore <= 0.0) {
      return selectFallback(context, random);
    }

    double roll = random.nextDouble() * totalSelectionScore;

    if (roll < FALLBACK_SCORE) {
      return selectFallback(context, random);
    }

    roll -= FALLBACK_SCORE;

    for (ScoredRecipe scoredRecipe : scoredRecipes) {
      if (roll < scoredRecipe.score()) {
        return createCustomSelection(scoredRecipe.recipe().enchantment(), context, random);
      }

      roll -= scoredRecipe.score();
    }

    return selectFallback(context, random);
  }

  private static void registerFallbackPools() {
    FALLBACK_POOLS.put(
        ElementType.STONE,
        new FallbackPool(
            List.of("efficiency", "unbreaking", "blast_protection"),
            List.of("protection", "knockback"),
            List.of("fortune")));

    FALLBACK_POOLS.put(
        ElementType.WOOD,
        new FallbackPool(
            List.of("efficiency", "unbreaking", "power"),
            List.of("punch", "quick_charge"),
            List.of("infinity")));

    FALLBACK_POOLS.put(
        ElementType.DUST,
        new FallbackPool(
            List.of("quick_charge", "piercing", "looting"),
            List.of("multishot", "fortune"),
            List.of("infinity")));

    FALLBACK_POOLS.put(
        ElementType.METAL,
        new FallbackPool(
            List.of("sharpness", "efficiency", "unbreaking", "protection"),
            List.of("sweeping", "piercing", "smite"),
            List.of("fortune")));

    FALLBACK_POOLS.put(
        ElementType.CRYSTAL,
        new FallbackPool(
            List.of("fortune", "loyalty", "channeling"),
            List.of("silk_touch", "impaling"),
            List.of("mending")));

    FALLBACK_POOLS.put(
        ElementType.LIFE,
        new FallbackPool(
            List.of("thorns", "respiration", "feather_falling"),
            List.of("looting", "depth_strider"),
            List.of("mending")));

    FALLBACK_POOLS.put(
        ElementType.WATER,
        new FallbackPool(
            List.of("respiration", "aqua_affinity", "depth_strider", "impaling"),
            List.of("loyalty", "riptide"),
            List.of("frost_walker")));

    FALLBACK_POOLS.put(
        ElementType.FIRE,
        new FallbackPool(
            List.of("fire_protection", "fire_aspect", "flame"),
            List.of("sharpness", "power"),
            List.of()));

    FALLBACK_POOLS.put(
        ElementType.MYSTICAL,
        new FallbackPool(
            List.of("unbreaking", "protection", "loyalty"),
            List.of("channeling", "frost_walker"),
            List.of("mending", "soul_speed", "swift_sneak")));
  }

  private static List<ScoredRecipe> calculateRecipeScores(ElementEnchantmentContext context) {
    List<ScoredRecipe> scoredRecipes = new ArrayList<>();

    for (ElementEnchantmentRecipe recipe : RECIPES) {
      double score = ElementEnchantmentScorer.calculateScore(recipe, context);

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

  private static SelectedEnchantment selectFallback(
      ElementEnchantmentContext context, RandomSource random) {
    ElementType dominantElement = context.chooseDominantElement(random);

    FallbackPool pool = FALLBACK_POOLS.getOrDefault(dominantElement, GENERAL_FALLBACK_POOL);

    FallbackTier maximumTier = getMaximumFallbackTier(dominantElement, context);

    FallbackTier selectedTier = selectFallbackTier(pool, maximumTier, random);

    List<Enchantment> candidates = collectFallbackCandidates(pool, selectedTier);

    if (candidates.isEmpty()) {
      candidates = collectFallbackCandidates(GENERAL_FALLBACK_POOL, FallbackTier.COMMON);
    }

    if (candidates.isEmpty()) {
      return null;
    }

    Enchantment enchantment = candidates.get(random.nextInt(candidates.size()));

    return createFallbackSelection(enchantment, context, dominantElement, random);
  }

  private static FallbackTier getMaximumFallbackTier(
      ElementType dominantElement, ElementEnchantmentContext context) {
    int dominantAmount = context.getAmount(dominantElement);

    int totalAmount = context.getTotalAmount();

    if (dominantAmount >= RARE_MIN_DOMINANT_AMOUNT && totalAmount >= RARE_MIN_TOTAL_AMOUNT) {
      return FallbackTier.RARE;
    }

    if (dominantAmount >= ADVANCED_MIN_DOMINANT_AMOUNT
        && totalAmount >= ADVANCED_MIN_TOTAL_AMOUNT) {
      return FallbackTier.ADVANCED;
    }

    return FallbackTier.COMMON;
  }

  private static FallbackTier selectFallbackTier(
      FallbackPool pool, FallbackTier maximumTier, RandomSource random) {
    int commonWeight = pool.common().isEmpty() ? 0 : COMMON_TIER_WEIGHT;

    int advancedWeight =
        maximumTier.ordinal() >= FallbackTier.ADVANCED.ordinal() && !pool.advanced().isEmpty()
            ? ADVANCED_TIER_WEIGHT
            : 0;

    int rareWeight =
        maximumTier == FallbackTier.RARE && !pool.rare().isEmpty() ? RARE_TIER_WEIGHT : 0;

    int totalWeight = commonWeight + advancedWeight + rareWeight;

    if (totalWeight <= 0) {
      return FallbackTier.COMMON;
    }

    int roll = random.nextInt(totalWeight);

    if (roll < commonWeight) {
      return FallbackTier.COMMON;
    }

    roll -= commonWeight;

    if (roll < advancedWeight) {
      return FallbackTier.ADVANCED;
    }

    return FallbackTier.RARE;
  }

  private static List<Enchantment> collectFallbackCandidates(FallbackPool pool, FallbackTier tier) {
    List<String> enchantmentIds = getTierEntries(pool, tier);

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

  private static List<String> getTierEntries(FallbackPool pool, FallbackTier tier) {
    return switch (tier) {
      case COMMON -> pool.common();
      case ADVANCED -> pool.advanced();
      case RARE -> pool.rare();
    };
  }

  private static Enchantment resolveVanillaEnchantment(String path) {
    ResourceLocation location = ResourceLocation.tryParse("minecraft:" + path);

    if (location == null) {
      return null;
    }

    return BuiltInRegistries.ENCHANTMENT.getOptional(location).orElse(null);
  }

  private static SelectedEnchantment createCustomSelection(
      Enchantment enchantment, ElementEnchantmentContext context, RandomSource random) {
    int level = selectCustomLevel(enchantment, context, random);

    return new SelectedEnchantment(enchantment, level);
  }

  private static SelectedEnchantment createFallbackSelection(
      Enchantment enchantment,
      ElementEnchantmentContext context,
      ElementType dominantElement,
      RandomSource random) {
    int level = selectFallbackLevel(enchantment, context, dominantElement, random);

    return new SelectedEnchantment(enchantment, level);
  }

  private static int selectFallbackLevel(
      Enchantment enchantment,
      ElementEnchantmentContext context,
      ElementType dominantElement,
      RandomSource random) {
    int minimumLevel = enchantment.getMinLevel();
    int maximumLevel = enchantment.getMaxLevel();

    if (minimumLevel >= maximumLevel) {
      return minimumLevel;
    }

    int totalAmount = Math.min(context.getTotalAmount(), MAX_TOTAL_ELEMENT_AMOUNT);

    int dominantAmount = context.getAmount(dominantElement);

    int unlockedMaximumLevel =
        getUnlockedFallbackMaximumLevel(minimumLevel, maximumLevel, totalAmount, dominantAmount);

    if (unlockedMaximumLevel <= minimumLevel) {
      return minimumLevel;
    }

    return selectWeightedFallbackLevel(minimumLevel, unlockedMaximumLevel, random);
  }

  private static int getUnlockedFallbackMaximumLevel(
      int minimumLevel, int maximumLevel, int totalAmount, int dominantAmount) {
    int unlockedLevel = minimumLevel;

    if (totalAmount >= 180 && dominantAmount >= 80) {
      unlockedLevel = Math.max(unlockedLevel, 2);
    }

    if (totalAmount >= 300 && dominantAmount >= 140) {
      unlockedLevel = Math.max(unlockedLevel, 3);
    }

    if (totalAmount >= 420 && dominantAmount >= 220) {
      unlockedLevel = Math.max(unlockedLevel, 4);
    }

    if (totalAmount >= 450 && dominantAmount >= 300) {
      unlockedLevel = Math.max(unlockedLevel, 5);
    }

    return Math.min(maximumLevel, unlockedLevel);
  }

  private static int selectWeightedFallbackLevel(
      int minimumLevel, int maximumLevel, RandomSource random) {
    int totalWeight = 0;

    for (int level = minimumLevel; level <= maximumLevel; level++) {
      totalWeight += getFallbackLevelWeight(level);
    }

    if (totalWeight <= 0) {
      return minimumLevel;
    }

    int roll = random.nextInt(totalWeight);

    for (int level = minimumLevel; level <= maximumLevel; level++) {
      int weight = getFallbackLevelWeight(level);

      if (roll < weight) {
        return level;
      }

      roll -= weight;
    }

    return minimumLevel;
  }

  private static int getFallbackLevelWeight(int level) {
    int index = level - 1;

    if (index < 0) {
      return FALLBACK_LEVEL_WEIGHTS[0];
    }

    if (index >= FALLBACK_LEVEL_WEIGHTS.length) {
      return 1;
    }

    return FALLBACK_LEVEL_WEIGHTS[index];
  }

  private static int selectCustomLevel(
      Enchantment enchantment, ElementEnchantmentContext context, RandomSource random) {
    int minimumLevel = enchantment.getMinLevel();

    int maximumLevel = enchantment.getMaxLevel();

    if (minimumLevel >= maximumLevel) {
      return minimumLevel;
    }

    int totalAmount = Math.min(context.getTotalAmount(), MAX_TOTAL_ELEMENT_AMOUNT);

    double quality = (double) totalAmount / MAX_TOTAL_ELEMENT_AMOUNT;

    int levelRange = maximumLevel - minimumLevel;

    int unlockedAdditionalLevels = (int) Math.floor(quality * (levelRange + 1));

    unlockedAdditionalLevels = Math.min(levelRange, unlockedAdditionalLevels);

    int unlockedMaximumLevel = minimumLevel + unlockedAdditionalLevels;

    if (unlockedMaximumLevel <= minimumLevel) {
      return minimumLevel;
    }

    return minimumLevel + random.nextInt(unlockedMaximumLevel - minimumLevel + 1);
  }

  private enum FallbackTier {
    COMMON,
    ADVANCED,
    RARE
  }

  private record FallbackPool(List<String> common, List<String> advanced, List<String> rare) {}

  private record ScoredRecipe(ElementEnchantmentRecipe recipe, double score) {}
}
