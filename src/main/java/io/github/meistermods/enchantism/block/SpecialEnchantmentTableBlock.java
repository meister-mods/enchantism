package io.github.meistermods.enchantism.block;

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
import org.jetbrains.annotations.Nullable;

public final class SpecialEnchantmentTableBlock extends EnchantmentTableBlock
{
    private static final Component CONTAINER_TITLE =
            Component.translatable("container.enchantism.special_enchantment");

    public SpecialEnchantmentTableBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos)
    {
        return new SimpleMenuProvider(
                (containerId, inventory, player) ->
                        new SpecialEnchantmentMenu(
                                containerId,
                                inventory,
                                ContainerLevelAccess.create(level, pos)
                        ),
                CONTAINER_TITLE
        );
    }

    /**
     * The first implementation intentionally omits the animated floating book.
     *
     * Vanilla's enchanting-table block entity is only valid for the vanilla
     * enchanting-table block. Returning null avoids creating an invalid vanilla
     * block entity for this custom block. The table model, particles, bookshelf
     * checks and enchanting behavior still work.
     */
    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return null;
    }
}
