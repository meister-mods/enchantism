package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.block.SpecialEnchantmentTableBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Enchantism.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Enchantism.MOD_ID);

    public static final RegistryObject<Block> SPECIAL_ENCHANTMENT_TABLE = BLOCKS.register(
            "special_enchantment_table",
            () -> new SpecialEnchantmentTableBlock(
                    BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)
            )
    );

    public static final RegistryObject<Item> SPECIAL_ENCHANTMENT_TABLE_ITEM = ITEMS.register(
            "special_enchantment_table",
            () -> new BlockItem(SPECIAL_ENCHANTMENT_TABLE.get(), new Item.Properties())
    );

    private ModBlocks()
    {
    }

    public static void register(IEventBus modEventBus)
    {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(ModBlocks::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            event.accept(SPECIAL_ENCHANTMENT_TABLE_ITEM);
        }
    }
}
