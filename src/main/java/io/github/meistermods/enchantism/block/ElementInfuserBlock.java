package io.github.meistermods.enchantism.block;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;

import io.github.meistermods.enchantism.blockentity.ElementInfuserBlockEntity;
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
import net.minecraft.world.level.block.Block;
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

@SuppressWarnings({"null", "deprecation"})
public final class ElementInfuserBlock extends BaseEntityBlock
{
    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;

    public ElementInfuserBlock(Properties properties)
    {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            @Nonnull StateDefinition.Builder<Block, BlockState> builder
    )
    {
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(
            @Nonnull BlockPlaceContext context
    )
    {
        return this.defaultBlockState()
                .setValue(
                        FACING,
                        context.getHorizontalDirection().getOpposite()
                );
    }

    @Override
    @Nonnull
    public RenderShape getRenderShape(
            @Nonnull BlockState state
    )
    {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(
            @Nonnull BlockPos pos,
            @Nonnull BlockState state
    )
    {
        return new ElementInfuserBlockEntity(pos, state);
    }

    @Override
    @Nonnull
    public InteractionResult use(
            @Nonnull BlockState state,
            @Nonnull Level level,
            @Nonnull BlockPos pos,
            @Nonnull Player player,
            @Nonnull InteractionHand hand,
            @Nonnull BlockHitResult hit
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

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (blockEntity instanceof ElementInfuserBlockEntity infuser)
        {
            NetworkHooks.openScreen(
                    serverPlayer,
                    infuser,
                    pos
            );
        }

        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @Nonnull Level level,
            @Nonnull BlockState state,
            @Nonnull BlockEntityType<T> type
    )
    {
        if (level.isClientSide)
        {
            return null;
        }

        return createTickerHelper(
                type,
                ModBlockEntities.ELEMENT_INFUSER.get(),
                ElementInfuserBlockEntity::serverTick
        );
    }

    @Override
    public void onRemove(
            @Nonnull BlockState oldState,
            @Nonnull Level level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState newState,
            boolean movedByPiston
    )
    {
        if (!oldState.is(newState.getBlock()))
        {
            BlockEntity blockEntity =
                    level.getBlockEntity(pos);

            if (blockEntity instanceof ElementInfuserBlockEntity infuser)
            {
                infuser.dropContents();
            }
        }

        super.onRemove(
                oldState,
                level,
                pos,
                newState,
                movedByPiston
        );
    }
}