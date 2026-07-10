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

@SuppressWarnings("null")
public final class SpecialEnchantmentTableBlock extends EnchantmentTableBlock
{
    private static final Component CONTAINER_TITLE =
        Component.translatable("container.enchantism.special_enchantment");

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
        ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);

        return new SimpleMenuProvider(
            (containerId, inventory, player) ->
                new SpecialEnchantmentMenu(
                    containerId,
                    inventory,
                    access
                ),
            CONTAINER_TITLE
        );
    }

    /**
     * This block does not currently use an enchantment-table block entity.
     * Therefore, the floating-book animation is not rendered.
     */
    @Override
    public BlockEntity newBlockEntity(
        BlockPos pos,
        BlockState state
    )
    {
        return null;
    }
}