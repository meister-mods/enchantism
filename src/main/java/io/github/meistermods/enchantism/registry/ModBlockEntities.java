package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.blockentity.EnchantmentApplicatorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    ForgeRegistries.BLOCK_ENTITY_TYPES,
                    Enchantism.MOD_ID
            );

    public static final RegistryObject<
            BlockEntityType<EnchantmentApplicatorBlockEntity>
            > ENCHANTMENT_APPLICATOR =
            BLOCK_ENTITIES.register(
                    "enchantment_applicator",
                    () -> BlockEntityType.Builder.of(
                            EnchantmentApplicatorBlockEntity::new,
                            ModBlocks.ENCHANTMENT_APPLICATOR.get()
                    ).build(null)
            );

    private ModBlockEntities()
    {
    }

    public static void register(IEventBus modEventBus)
    {
        BLOCK_ENTITIES.register(modEventBus);
    }
}