package io.github.meistermods.enchantism.element;

import io.github.meistermods.enchantism.Enchantism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@SuppressWarnings({"null"})
public final class ModElementTags {
  public static final TagKey<Item> STONE = create("elements/stone");

  public static final TagKey<Item> WOOD = create("elements/wood");

  public static final TagKey<Item> DUST = create("elements/dust");

  private ModElementTags() {}

  private static TagKey<Item> create(String path) {
    return TagKey.create(
        Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Enchantism.MOD_ID, path));
  }
}
