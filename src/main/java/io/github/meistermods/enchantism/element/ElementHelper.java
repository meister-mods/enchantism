package io.github.meistermods.enchantism.element;

import java.util.HashMap;
import java.util.Map;

import io.github.meistermods.enchantism.registry.ModBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@SuppressWarnings({"null"})
public final class ElementHelper {
  private static final Map<Item, ElementMaterialDefinition> MATERIALS = new HashMap<>();

  static {
    /*
     * Stone materials
     */
    register(Items.COBBLESTONE, ElementType.STONE, 10);
    register(Items.STONE, ElementType.STONE, 12);
    register(Items.GRANITE, ElementType.STONE, 12);
    register(Items.DIORITE, ElementType.STONE, 12);
    register(Items.ANDESITE, ElementType.STONE, 12);
    register(Items.DEEPSLATE, ElementType.STONE, 14);
    register(Items.COBBLED_DEEPSLATE, ElementType.STONE, 14);
    register(Items.TUFF, ElementType.STONE, 14);
    register(Items.CALCITE, ElementType.STONE, 16);

    /*
     * Wood materials
     *
     * One log produces four planks:
     * 4 * 8 = 32.
     */
    register(Items.OAK_PLANKS, ElementType.WOOD, 8);
    register(Items.SPRUCE_PLANKS, ElementType.WOOD, 8);
    register(Items.BIRCH_PLANKS, ElementType.WOOD, 8);
    register(Items.JUNGLE_PLANKS, ElementType.WOOD, 8);
    register(Items.ACACIA_PLANKS, ElementType.WOOD, 8);
    register(Items.DARK_OAK_PLANKS, ElementType.WOOD, 8);
    register(Items.MANGROVE_PLANKS, ElementType.WOOD, 8);
    register(Items.CHERRY_PLANKS, ElementType.WOOD, 8);
    register(Items.CRIMSON_PLANKS, ElementType.WOOD, 8);
    register(Items.WARPED_PLANKS, ElementType.WOOD, 8);
    register(Items.OAK_LOG, ElementType.WOOD, 32);
    register(Items.SPRUCE_LOG, ElementType.WOOD, 32);
    register(Items.BIRCH_LOG, ElementType.WOOD, 32);
    register(Items.JUNGLE_LOG, ElementType.WOOD, 32);
    register(Items.ACACIA_LOG, ElementType.WOOD, 32);
    register(Items.DARK_OAK_LOG, ElementType.WOOD, 32);
    register(Items.MANGROVE_LOG, ElementType.WOOD, 32);
    register(Items.CHERRY_LOG, ElementType.WOOD, 32);
    register(Items.CRIMSON_STEM, ElementType.WOOD, 32);
    register(Items.WARPED_STEM, ElementType.WOOD, 32);

    /*
     * Dust materials
     */
    register(Items.SUGAR, ElementType.DUST, 2);
    register(Items.REDSTONE, ElementType.DUST, 3);
    registerMultiple(Items.BONE_MEAL, ElementType.DUST, 4).add(ElementType.LIFE, 3);
    register(Items.GLOWSTONE_DUST, ElementType.DUST, 5);
    register(Items.GUNPOWDER, ElementType.DUST, 7);

    /*
     * Metal materials
     *
     * Nine nuggets equal one ingot.
     */
    register(Items.IRON_NUGGET, ElementType.METAL, 3);
    register(Items.IRON_INGOT, ElementType.METAL, 27);
    register(Items.GOLD_NUGGET, ElementType.METAL, 4);
    register(Items.GOLD_INGOT, ElementType.METAL, 36);
    register(Items.COPPER_INGOT, ElementType.METAL, 18);
    register(Items.NETHERITE_SCRAP, ElementType.METAL, 70);
    register(Items.NETHERITE_INGOT, ElementType.METAL, 4 * 70 + 4 * 36 );

    /*
     * Crystal materials
     */
    register(Items.QUARTZ, ElementType.CRYSTAL, 12);
    registerMultiple(Items.PRISMARINE_CRYSTALS, ElementType.CRYSTAL, 18).add(ElementType.WATER, 14);
    register(Items.LAPIS_LAZULI, ElementType.CRYSTAL, 20);
    register(Items.AMETHYST_SHARD, ElementType.CRYSTAL, 20);
    register(Items.EMERALD, ElementType.CRYSTAL, 40);
    register(Items.DIAMOND, ElementType.CRYSTAL, 65);
    register(Items.ECHO_SHARD, ElementType.CRYSTAL, 90);

    /*
     * Life materials
     *
     * Renewable crops intentionally have relatively low values.
     */
    register(Items.WHEAT_SEEDS, ElementType.LIFE, 2);
    register(Items.BEETROOT_SEEDS, ElementType.LIFE, 2);
    registerMultiple(Items.KELP, ElementType.LIFE, 3).add(ElementType.WATER, 2);
    register(Items.BEETROOT, ElementType.LIFE, 4);
    register(Items.WHEAT, ElementType.LIFE, 5);
    register(Items.CARROT, ElementType.LIFE, 5);
    register(Items.POTATO, ElementType.LIFE, 5);
    register(Items.APPLE, ElementType.LIFE, 10);
    register(Items.SLIME_BALL, ElementType.LIFE, 15);

    /*
     * Water materials
     */
    register(Items.SNOWBALL, ElementType.WATER, 2);
    register(Items.SNOW_BLOCK, ElementType.WATER, 8);
    register(Items.ICE, ElementType.WATER, 12);
    register(Items.PACKED_ICE, ElementType.WATER, 30);
    register(Items.BLUE_ICE, ElementType.WATER, 270);
    registerMultiple(Items.PRISMARINE_SHARD, ElementType.WATER, 10).add(ElementType.CRYSTAL, 8);
    registerMultiple(Items.NAUTILUS_SHELL, ElementType.WATER, 40).add(ElementType.LIFE, 25);

    register(Items.HEART_OF_THE_SEA, ElementType.WATER, 200);

    /*
     * Fire materials
     */
    register(Items.COAL, ElementType.FIRE, 8);
    registerMultiple(Items.CHARCOAL, ElementType.FIRE, 8).add(ElementType.WOOD, 6);
    register(Items.FIRE_CHARGE, ElementType.FIRE, 12);
    register(Items.BLAZE_POWDER, ElementType.FIRE, 14);
    register(Items.BLAZE_ROD, ElementType.FIRE, 28);
    registerMultiple(Items.MAGMA_CREAM, ElementType.FIRE, 24).add(ElementType.LIFE, 12);
  }

  private ElementHelper() {}

  private static void register(Item item, ElementType elementType, int amount) {
    MATERIALS.put(item, new ElementMaterialDefinition(elementType, amount));
  }

  private static ElementMaterialDefinition registerMultiple(
      Item item, ElementType defaultElement, int defaultAmount) {
    ElementMaterialDefinition definition =
        new ElementMaterialDefinition(defaultElement, defaultAmount);

    MATERIALS.put(item, definition);

    return definition;
  }

  public static ElementMaterialData getMaterialData(ItemStack stack, ElementType containerElement) {
    if (stack.isEmpty()) {
      return null;
    }

    if (stack.is(ModBlocks.COMPRESSED_COBBLESTONE_ITEM.get())) {
      return resolveFixedElement(ElementType.STONE, 90, containerElement);
    }

    ElementMaterialDefinition definition = MATERIALS.get(stack.getItem());

    if (definition != null) {
      return definition.resolve(containerElement);
    }

    return resolveTagMaterial(stack, containerElement);
  }

  public static ElementMaterialData getMaterialData(ItemStack stack) {
    return getMaterialData(stack, ElementType.EMPTY);
  }

  public static ElementType getElementType(ItemStack stack) {
    ElementMaterialData data = getMaterialData(stack);

    if (data == null) {
      return ElementType.EMPTY;
    }

    return data.elementType();
  }

  public static int getElementAmount(ItemStack stack) {
    ElementMaterialData data = getMaterialData(stack);

    if (data == null) {
      return 0;
    }

    return data.amount();
  }

  public static boolean isValidMaterial(ItemStack stack) {
    return getMaterialData(stack) != null;
  }

  private static ElementMaterialData resolveTagMaterial(
      ItemStack stack, ElementType containerElement) {
    if (stack.is(ModElementTags.STONE)) {
      return resolveFixedElement(ElementType.STONE, 10, containerElement);
    }

    if (stack.is(ModElementTags.WOOD)) {
      return resolveFixedElement(ElementType.WOOD, 8, containerElement);
    }

    if (stack.is(ModElementTags.DUST)) {
      return resolveFixedElement(ElementType.DUST, 3, containerElement);
    }

    if (stack.is(ModElementTags.METAL)) {
      return resolveFixedElement(ElementType.METAL, 12, containerElement);
    }

    if (stack.is(ModElementTags.CRYSTAL)) {
      return resolveFixedElement(ElementType.CRYSTAL, 16, containerElement);
    }

    if (stack.is(ModElementTags.LIFE)) {
      return resolveFixedElement(ElementType.LIFE, 4, containerElement);
    }

    return null;
  }

  private static ElementMaterialData resolveFixedElement(
      ElementType materialElement, int amount, ElementType containerElement) {
    if (containerElement != ElementType.EMPTY && containerElement != materialElement) {
      return null;
    }

    return new ElementMaterialData(materialElement, amount);
  }
}
