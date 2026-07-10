package io.github.meistermods.enchantism.block;

import io.github.meistermods.enchantism.blockentity.EnchantmentApplicatorBlockEntity;
import io.github.meistermods.enchantism.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

@SuppressWarnings("null")
public final class EnchantmentApplicatorBlock extends BaseEntityBlock
{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public EnchantmentApplicatorBlock(
        Properties properties
    )
    {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(
        StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    )
    {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(
        BlockPlaceContext context
    )
    {
        return defaultBlockState().setValue(
            FACING,
            context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    public RenderShape getRenderShape(
        BlockState state
    )
    {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(
        BlockPos pos,
        BlockState state
    )
    {
        return new EnchantmentApplicatorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hit
    )
    {
        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResult.CONSUME;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity
            instanceof EnchantmentApplicatorBlockEntity applicator)
        {
            NetworkHooks.openScreen(serverPlayer, applicator, pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level,
        BlockState state,
        BlockEntityType<T> type
    )
    {
        if (level.isClientSide)
        {
            return null;
        }

        return createTickerHelper(
            type,
            ModBlockEntities.ENCHANTMENT_APPLICATOR.get(),
            EnchantmentApplicatorBlockEntity::serverTick
        );
    }

    @Override
    public void onRemove(
        BlockState oldState,
        Level level,
        BlockPos pos,
        BlockState newState,
        boolean movedByPiston
    )
    {
        if (!oldState.is(newState.getBlock()))
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity
                instanceof EnchantmentApplicatorBlockEntity applicator)
            {
                applicator.dropContents();
            }

            super.onRemove(oldState, level, pos, newState, movedByPiston);
        }
    }
}