package io.github.meistermods.enchantism.menu;

import javax.annotation.Nonnull;

import io.github.meistermods.enchantism.blockentity.ElementInfuserBlockEntity;
import io.github.meistermods.enchantism.element.ElementHelper;
import io.github.meistermods.enchantism.element.ElementType;
import io.github.meistermods.enchantism.item.ElementContainerItem;
import io.github.meistermods.enchantism.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

@SuppressWarnings({"null", "deprecation"})
public final class ElementInfuserMenu extends AbstractContainerMenu
{
    private static final int MACHINE_SLOT_COUNT = 2;

    private static final int PLAYER_INVENTORY_START = 2;
    private static final int PLAYER_INVENTORY_END = 29;

    private static final int HOTBAR_START = 29;
    private static final int HOTBAR_END = 38;

    private final Container container;
    private final ContainerData data;

    public ElementInfuserMenu(
            int containerId,
            @Nonnull Inventory inventory,
            FriendlyByteBuf buffer
    )
    {
        this(
                containerId,
                inventory,
                getBlockEntityContainer(
                        inventory,
                        buffer
                ),
                new SimpleContainerData(2)
        );
    }

    public ElementInfuserMenu(
            int containerId,
            @Nonnull Inventory inventory,
            @Nonnull ElementInfuserBlockEntity blockEntity,
            @Nonnull ContainerData data
    )
    {
        this(
                containerId,
                inventory,
                (Container) blockEntity,
                data
        );
    }

    private ElementInfuserMenu(
            int containerId,
            @Nonnull Inventory inventory,
            @Nonnull Container container,
            @Nonnull ContainerData data
    )
    {
        super(
                ModMenus.ELEMENT_INFUSER.get(),
                containerId
        );

        checkContainerSize(
                container,
                MACHINE_SLOT_COUNT
        );

        checkContainerDataCount(
                data,
                2
        );

        this.container = container;
        this.data = data;

        this.container.startOpen(
                inventory.player
        );

        this.addMachineSlots();
        this.addPlayerInventory(inventory);
        this.addPlayerHotbar(inventory);

        this.addDataSlots(data);
    }

    private void addMachineSlots()
    {
        /*
        * Material input slot.
        */
        this.addSlot(
                new Slot(
                        this.container,
                        ElementInfuserBlockEntity.MATERIAL_SLOT,
                        56,
                        35
                )
                {
                    @Override
                    public boolean mayPlace(
                            @Nonnull ItemStack stack
                    )
                    {
                        return ElementHelper.getElementType(stack)
                                != ElementType.EMPTY;
                    }
                }
        );

        /*
        * Element container slot.
        */
        this.addSlot(
                new Slot(
                        this.container,
                        ElementInfuserBlockEntity.CONTAINER_SLOT,
                        116,
                        35
                )
                {
                    @Override
                    public boolean mayPlace(
                            @Nonnull ItemStack stack
                    )
                    {
                        return stack.getItem()
                                instanceof ElementContainerItem;
                    }

                    @Override
                    public int getMaxStackSize()
                    {
                        return 1;
                    }
                }
        );
    }
    
    private void addPlayerInventory(
            @Nonnull Inventory inventory
    )
    {
        for (int row = 0; row < 3; row++)
        {
            for (int column = 0; column < 9; column++)
            {
                this.addSlot(
                        new Slot(
                                inventory,
                                column + row * 9 + 9,
                                8 + column * 18,
                                84 + row * 18
                        )
                );
            }
        }
    }

    private void addPlayerHotbar(
            @Nonnull Inventory inventory
    )
    {
        for (int column = 0; column < 9; column++)
        {
            this.addSlot(
                    new Slot(
                            inventory,
                            column,
                            8 + column * 18,
                            142
                    )
            );
        }
    }

    private static Container getBlockEntityContainer(
            @Nonnull Inventory inventory,
            FriendlyByteBuf buffer
    )
    {
        if (buffer == null)
        {
            return new SimpleContainer(
                    MACHINE_SLOT_COUNT
            );
        }

        BlockPos pos =
                buffer.readBlockPos();

        BlockEntity blockEntity =
                inventory.player
                        .level()
                        .getBlockEntity(pos);

        if (blockEntity instanceof ElementInfuserBlockEntity infuser)
        {
            return infuser;
        }

        return new SimpleContainer(
                MACHINE_SLOT_COUNT
        );
    }

    public int getProgress()
    {
        return this.data.get(0);
    }

    public int getMaxProgress()
    {
        return this.data.get(1);
    }

    public int getScaledProgress(int width)
    {
        int progress =
                this.getProgress();

        int maxProgress =
                this.getMaxProgress();

        if (progress <= 0 || maxProgress <= 0)
        {
            return 0;
        }

        return progress * width / maxProgress;
    }

    @Override
    public boolean stillValid(
            @Nonnull Player player
    )
    {
        return this.container.stillValid(player);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(
            @Nonnull Player player,
            int index
    )
    {
        Slot sourceSlot =
                this.slots.get(index);

        if (!sourceSlot.hasItem())
        {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack =
                sourceSlot.getItem();

        ItemStack copiedStack =
                sourceStack.copy();

        if (index < MACHINE_SLOT_COUNT)
        {
            if (!this.moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            ))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (sourceStack.getItem()
                instanceof ElementContainerItem)
        {
            if (!this.moveItemStackTo(
                    sourceStack,
                    ElementInfuserBlockEntity.CONTAINER_SLOT,
                    ElementInfuserBlockEntity.CONTAINER_SLOT + 1,
                    false
            ))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (ElementHelper.getElementType(sourceStack)
                != ElementType.EMPTY)
        {
            if (!this.moveItemStackTo(
                    sourceStack,
                    ElementInfuserBlockEntity.MATERIAL_SLOT,
                    ElementInfuserBlockEntity.MATERIAL_SLOT + 1,
                    false
            ))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (index >= PLAYER_INVENTORY_START
                && index < PLAYER_INVENTORY_END)
        {
            if (!this.moveItemStackTo(
                    sourceStack,
                    HOTBAR_START,
                    HOTBAR_END,
                    false
            ))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (index >= HOTBAR_START
                && index < HOTBAR_END)
        {
            if (!this.moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    false
            ))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty())
        {
            sourceSlot.set(ItemStack.EMPTY);
        }
        else
        {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount()
                == copiedStack.getCount())
        {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(
                player,
                sourceStack
        );

        return copiedStack;
    }
    
    @Override
    public void removed(
            @Nonnull Player player
    )
    {
        super.removed(player);

        this.container.stopOpen(player);
    }
}