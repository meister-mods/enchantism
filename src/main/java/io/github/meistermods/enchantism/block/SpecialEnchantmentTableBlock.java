package io.github.meistermods.enchantism.block;

import org.jetbrains.annotations.Nullable;

import io.github.meistermods.enchantism.menu.SpecialEnchantmentMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SpecialEnchantmentTableBlock
        extends EnchantmentTableBlock
{
    private static final Component CONTAINER_TITLE =
            Component.translatable(
                    "container.enchantism.special_enchantment"
            );

    public SpecialEnchantmentTableBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos pos
    )
    {
        return new SimpleMenuProvider(
                (containerId, inventory, player) ->
                        new SpecialEnchantmentMenu(
                                containerId,
                                inventory,
                                ContainerLevelAccess.create(
                                        level,
                                        pos
                                )
                        ),
                CONTAINER_TITLE
        );
    }

    /**
     * Vanilla's EnchantmentTableBlockEntity is registered only for the
     * vanilla enchanting-table block. Returning null avoids attaching an
     * invalid vanilla block entity to this custom block.
     *
     * The table model, particles, bookshelf checks and menu still work.
     * The animated floating book is omitted until a custom block entity
     * and renderer are added.
     */
    @Override
    @Nullable
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    )
    {
        return null;
    }
}