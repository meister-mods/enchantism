package io.github.meistermods.enchantism.element;

import io.github.meistermods.enchantism.registry.ModBlocks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@SuppressWarnings({"null"})
public final class ElementHelper {
  private static final Map<Item, ElementMaterialData> MATERIALS = new HashMap<>();

  static {
    /*
     * Stone materials
     */
    register(Items.COBBLESTONE, ElementType.STONE, 10);

    register(Items.STONE, ElementType.STONE, 12);

    register(Items.GRANITE, ElementType.STONE, 13);

    register(Items.DIORITE, ElementType.STONE, 14);

    register(Items.ANDESITE, ElementType.STONE, 15);

    register(Items.DEEPSLATE, ElementType.STONE, 16);

    register(Items.COBBLED_DEEPSLATE, ElementType.STONE, 17);

    register(Items.TUFF, ElementType.STONE, 18);

    register(Items.CALCITE, ElementType.STONE, 19);

    /*
     * Wood materials
     */
    register(Items.OAK_PLANKS, ElementType.WOOD, 8);

    register(Items.SPRUCE_PLANKS, ElementType.WOOD, 9);

    register(Items.BIRCH_PLANKS, ElementType.WOOD, 10);

    register(Items.JUNGLE_PLANKS, ElementType.WOOD, 11);

    register(Items.ACACIA_PLANKS, ElementType.WOOD, 12);

    register(Items.DARK_OAK_PLANKS, ElementType.WOOD, 13);

    register(Items.OAK_LOG, ElementType.WOOD, 32);

    register(Items.SPRUCE_LOG, ElementType.WOOD, 34);

    register(Items.BIRCH_LOG, ElementType.WOOD, 30);

    /*
     * Dust materials
     */
    register(Items.REDSTONE, ElementType.DUST, 3);

    register(Items.GLOWSTONE_DUST, ElementType.DUST, 5);

    register(Items.GUNPOWDER, ElementType.DUST, 7);

    register(Items.SUGAR, ElementType.DUST, 2);

    register(Items.BONE_MEAL, ElementType.DUST, 4);
  }

  private ElementHelper() {}

  private static void register(Item item, ElementType elementType, int amount) {
    MATERIALS.put(item, new ElementMaterialData(elementType, amount));
  }

  public static ElementMaterialData getMaterialData(ItemStack stack) {
    if (stack.isEmpty()) {
      return null;
    }

    /*
     * ModBlocks registry objects should only be accessed
     * after registry initialization.
     */
    if (stack.is(ModBlocks.COMPRESSED_COBBLESTONE_ITEM.get())) {
      return new ElementMaterialData(ElementType.STONE, 90);
    }

    return MATERIALS.get(stack.getItem());
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
}
