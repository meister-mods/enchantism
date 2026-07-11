package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.block.ElementInfuserBlock;
import io.github.meistermods.enchantism.block.EnchantmentApplicatorBlock;
import io.github.meistermods.enchantism.block.SpecialEnchantmentTableBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModBlocks {
  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, Enchantism.MOD_ID);

  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, Enchantism.MOD_ID);

  public static final RegistryObject<Block> SPECIAL_ENCHANTMENT_TABLE =
      BLOCKS.register(
          "special_enchantment_table",
          () ->
              new SpecialEnchantmentTableBlock(
                  BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)));

  public static final RegistryObject<Item> SPECIAL_ENCHANTMENT_TABLE_ITEM =
      ITEMS.register(
          "special_enchantment_table",
          () -> new BlockItem(SPECIAL_ENCHANTMENT_TABLE.get(), new Item.Properties()));

  public static final RegistryObject<Block> ENCHANTMENT_APPLICATOR =
      BLOCKS.register(
          "enchantment_applicator",
          () ->
              new EnchantmentApplicatorBlock(
                  BlockBehaviour.Properties.of()
                      .mapColor(MapColor.STONE)
                      .strength(3.5F)
                      .requiresCorrectToolForDrops()
                      .sound(SoundType.STONE)));

  public static final RegistryObject<Item> ENCHANTMENT_APPLICATOR_ITEM =
      ITEMS.register(
          "enchantment_applicator",
          () -> new BlockItem(ENCHANTMENT_APPLICATOR.get(), new Item.Properties()));

  public static final RegistryObject<Block> COMPRESSED_COBBLESTONE =
      BLOCKS.register(
          "compressed_cobblestone",
          () ->
              new Block(
                  BlockBehaviour.Properties.of()
                      .mapColor(MapColor.STONE)
                      .strength(4.0F, 8.0F)
                      .requiresCorrectToolForDrops()
                      .sound(SoundType.STONE)));

  public static final RegistryObject<Item> COMPRESSED_COBBLESTONE_ITEM =
      ITEMS.register(
          "compressed_cobblestone",
          () -> new BlockItem(COMPRESSED_COBBLESTONE.get(), new Item.Properties()));

  public static final RegistryObject<Block> ELEMENT_INFUSER =
      BLOCKS.register(
          "element_infuser",
          () ->
              new ElementInfuserBlock(
                  BlockBehaviour.Properties.of()
                      .mapColor(MapColor.STONE)
                      .strength(3.5F)
                      .requiresCorrectToolForDrops()
                      .sound(SoundType.STONE)));

  public static final RegistryObject<Item> ELEMENT_INFUSER_ITEM =
      ITEMS.register(
          "element_infuser", () -> new BlockItem(ELEMENT_INFUSER.get(), new Item.Properties()));

  private ModBlocks() {}

  public static void register(IEventBus modEventBus) {
    BLOCKS.register(modEventBus);
    ITEMS.register(modEventBus);
  }
}
