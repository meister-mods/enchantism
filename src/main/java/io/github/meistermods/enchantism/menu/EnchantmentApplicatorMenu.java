package io.github.meistermods.enchantism.menu;

import io.github.meistermods.enchantism.blockentity.EnchantmentApplicatorBlockEntity;
import io.github.meistermods.enchantism.item.ElementContainerItem;
import io.github.meistermods.enchantism.registry.ModMenus;
import javax.annotation.Nonnull;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"null"})
public final class EnchantmentApplicatorMenu extends AbstractContainerMenu {
  private static final int MACHINE_SLOT_COUNT = 11;

  private static final int PLAYER_INVENTORY_START = 11;
  private static final int PLAYER_INVENTORY_END = 38;

  private static final int HOTBAR_START = 38;
  private static final int HOTBAR_END = 47;

  private final Container container;
  private final ContainerData data;

  /** Client-side constructor. */
  public EnchantmentApplicatorMenu(
      int containerId, @Nonnull Inventory inventory, @Nullable FriendlyByteBuf buffer) {
    this(
        containerId,
        inventory,
        getBlockEntityContainer(inventory, buffer),
        new SimpleContainerData(2));
  }

  /** Server-side constructor. */
  public EnchantmentApplicatorMenu(
      int containerId,
      @Nonnull Inventory inventory,
      @Nonnull EnchantmentApplicatorBlockEntity blockEntity,
      @Nonnull ContainerData data) {
    this(containerId, inventory, (Container) blockEntity, data);
  }

  private EnchantmentApplicatorMenu(
      int containerId,
      @Nonnull Inventory inventory,
      @Nonnull Container container,
      @Nonnull ContainerData data) {
    super(ModMenus.ENCHANTMENT_APPLICATOR.get(), containerId);

    checkContainerSize(container, MACHINE_SLOT_COUNT);

    checkContainerDataCount(data, 2);

    this.container = container;
    this.data = data;

    this.container.startOpen(inventory.player);

    this.addMachineSlots();
    this.addPlayerInventory(inventory);
    this.addPlayerHotbar(inventory);

    this.addDataSlots(data);
  }

  private void addMachineSlots() {
    int elementStartX = 26;
    int elementStartY = 17;

    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 3; column++) {
        int slot = row * 3 + column;

        this.addSlot(
            new Slot(this.container, slot, elementStartX + column * 18, elementStartY + row * 18) {
              @Override
              public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ElementContainerItem;
              }

              @Override
              public int getMaxStackSize() {
                return 1;
              }
            });
      }
    }

    this.addSlot(
        new Slot(this.container, EnchantmentApplicatorBlockEntity.BOOK_SLOT, 98, 35) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.BOOK);
          }
        });

    this.addSlot(
        new Slot(this.container, EnchantmentApplicatorBlockEntity.OUTPUT_SLOT, 152, 35) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return false;
          }
        });
  }

  private void addPlayerInventory(Inventory inventory) {
    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 9; column++) {
        this.addSlot(new Slot(inventory, column + row * 9 + 9, 26 + column * 18, 84 + row * 18));
      }
    }
  }

  private void addPlayerHotbar(Inventory inventory) {
    for (int column = 0; column < 9; column++) {
      this.addSlot(new Slot(inventory, column, 26 + column * 18, 142));
    }
  }

  @Nonnull
  private static Container getBlockEntityContainer(
      @Nonnull Inventory inventory, @Nullable FriendlyByteBuf buffer) {
    if (buffer == null) {
      return new SimpleContainer(MACHINE_SLOT_COUNT);
    }

    BlockPos blockPos = buffer.readBlockPos();

    BlockEntity blockEntity = inventory.player.level().getBlockEntity(blockPos);

    if (blockEntity instanceof EnchantmentApplicatorBlockEntity applicator) {
      return applicator;
    }

    return new SimpleContainer(MACHINE_SLOT_COUNT);
  }

  public int getProgress() {
    return this.data.get(0);
  }

  public int getMaxProgress() {
    return this.data.get(1);
  }

  public int getScaledProgress(int width) {
    int progress = this.getProgress();

    int maxProgress = this.getMaxProgress();

    if (progress <= 0 || maxProgress <= 0) {
      return 0;
    }

    return progress * width / maxProgress;
  }

  public boolean isProcessing() {
    return this.getProgress() > 0;
  }

  @Override
  public boolean stillValid(@Nonnull Player player) {
    return this.container.stillValid(player);
  }

  @Override
  @Nonnull
  public ItemStack quickMoveStack(@Nonnull Player player, int index) {
    if (index < 0 || index >= this.slots.size()) {
      return ItemStack.EMPTY;
    }

    Slot sourceSlot = this.slots.get(index);

    if (!sourceSlot.hasItem()) {
      return ItemStack.EMPTY;
    }

    ItemStack sourceStack = sourceSlot.getItem();

    ItemStack copiedStack = sourceStack.copy();

    /*
     * Machine inventory -> player inventory.
     */
    if (index < MACHINE_SLOT_COUNT) {
      if (!this.moveItemStackTo(sourceStack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
        return ItemStack.EMPTY;
      }
    }
    /*
     * Player inventory -> element container slots.
     */
    else if (sourceStack.getItem() instanceof ElementContainerItem) {
      if (!this.moveItemStackTo(
          sourceStack,
          EnchantmentApplicatorBlockEntity.ELEMENT_SLOT_START,
          EnchantmentApplicatorBlockEntity.ELEMENT_SLOT_END,
          false)) {
        return ItemStack.EMPTY;
      }
    }
    /*
     * Player inventory -> book input slot.
     */
    else if (sourceStack.is(Items.BOOK)) {
      if (!this.moveItemStackTo(
          sourceStack,
          EnchantmentApplicatorBlockEntity.BOOK_SLOT,
          EnchantmentApplicatorBlockEntity.BOOK_SLOT + 1,
          false)) {
        return ItemStack.EMPTY;
      }
    }
    /*
     * Main inventory -> hotbar.
     */
    else if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
      if (!this.moveItemStackTo(sourceStack, HOTBAR_START, HOTBAR_END, false)) {
        return ItemStack.EMPTY;
      }
    }
    /*
     * Hotbar -> main inventory.
     */
    else if (index >= HOTBAR_START && index < HOTBAR_END) {
      if (!this.moveItemStackTo(sourceStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
        return ItemStack.EMPTY;
      }
    } else {
      return ItemStack.EMPTY;
    }

    if (sourceStack.isEmpty()) {
      sourceSlot.set(ItemStack.EMPTY);
    } else {
      sourceSlot.setChanged();
    }

    if (sourceStack.getCount() == copiedStack.getCount()) {
      return ItemStack.EMPTY;
    }

    sourceSlot.onTake(player, sourceStack);

    return copiedStack;
  }

  @Override
  public void removed(@Nonnull Player player) {
    super.removed(player);

    this.container.stopOpen(player);
  }
}
