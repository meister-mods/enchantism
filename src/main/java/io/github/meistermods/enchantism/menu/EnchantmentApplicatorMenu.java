package io.github.meistermods.enchantism.menu;

import io.github.meistermods.enchantism.blockentity.EnchantmentApplicatorBlockEntity;
import io.github.meistermods.enchantism.registry.ModMenus;
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

@SuppressWarnings("null")
public final class EnchantmentApplicatorMenu extends AbstractContainerMenu {
  private static final int MACHINE_SLOT_COUNT = 3;

  private static final int PLAYER_INVENTORY_START = 3;
  private static final int PLAYER_INVENTORY_END = 30;

  private static final int HOTBAR_START = 30;
  private static final int HOTBAR_END = 39;

  private final Container container;
  private final ContainerData data;

  /**
   * Client-side constructor.
   *
   * <p>Forge uses this constructor when the menu is opened from the network.
   */
  public EnchantmentApplicatorMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
    this(
        containerId,
        inventory,
        getBlockEntityContainer(inventory, buffer),
        new SimpleContainerData(2));
  }

  /** Server-side constructor. */
  public EnchantmentApplicatorMenu(
      int containerId,
      Inventory inventory,
      EnchantmentApplicatorBlockEntity blockEntity,
      ContainerData data) {
    this(containerId, inventory, (Container) blockEntity, data);
  }

  private EnchantmentApplicatorMenu(
      int containerId, Inventory inventory, Container container, ContainerData data) {
    super(ModMenus.ENCHANTMENT_APPLICATOR.get(), containerId);

    checkContainerSize(container, MACHINE_SLOT_COUNT);

    checkContainerDataCount(data, 2);

    this.container = container;
    this.data = data;

    container.startOpen(inventory.player);

    addMachineSlots();
    addPlayerInventory(inventory);
    addPlayerHotbar(inventory);

    addDataSlots(data);
  }

  private void addMachineSlots() {
    /*
     * Book input.
     */
    this.addSlot(
        new Slot(this.container, EnchantmentApplicatorBlockEntity.BOOK_SLOT, 56, 17) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.BOOK);
          }

          @Override
          public int getMaxStackSize() {
            return 64;
          }
        });

    /*
     * Material input.
     */
    this.addSlot(
        new Slot(this.container, EnchantmentApplicatorBlockEntity.MATERIAL_SLOT, 56, 53) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return !stack.isEmpty();
          }
        });

    /*
     * Enchanted-book output.
     */
    this.addSlot(
        new Slot(this.container, EnchantmentApplicatorBlockEntity.OUTPUT_SLOT, 116, 35) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return false;
          }
        });
  }

  private void addPlayerInventory(Inventory inventory) {
    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 9; column++) {
        this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
      }
    }
  }

  private void addPlayerHotbar(Inventory inventory) {
    for (int column = 0; column < 9; column++) {
      this.addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }
  }

  private static Container getBlockEntityContainer(Inventory inventory, FriendlyByteBuf buffer) {
    if (buffer == null) {
      return new SimpleContainer(MACHINE_SLOT_COUNT);
    }

    var blockPos = buffer.readBlockPos();
    var blockEntity = inventory.player.level().getBlockEntity(blockPos);

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
    int progress = getProgress();
    int maxProgress = getMaxProgress();

    if (progress <= 0 || maxProgress <= 0) {
      return 0;
    }

    return progress * width / maxProgress;
  }

  public boolean isProcessing() {
    return getProgress() > 0;
  }

  @Override
  public boolean stillValid(Player player) {
    return this.container.stillValid(player);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    ItemStack copiedStack = ItemStack.EMPTY;
    Slot sourceSlot = this.slots.get(index);

    if (!sourceSlot.hasItem()) {
      return copiedStack;
    }

    ItemStack sourceStack = sourceSlot.getItem();
    copiedStack = sourceStack.copy();

    /*
     * Machine slots -> player inventory.
     */
    if (index < MACHINE_SLOT_COUNT) {
      if (!this.moveItemStackTo(sourceStack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
        return ItemStack.EMPTY;
      }
    }
    /*
     * Player inventory -> book slot.
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
     * Player inventory -> material slot.
     */
    else {
      if (!this.moveItemStackTo(
          sourceStack,
          EnchantmentApplicatorBlockEntity.MATERIAL_SLOT,
          EnchantmentApplicatorBlockEntity.MATERIAL_SLOT + 1,
          false)) {
        if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
          if (!this.moveItemStackTo(sourceStack, HOTBAR_START, HOTBAR_END, false)) {
            return ItemStack.EMPTY;
          }
        } else if (index >= HOTBAR_START && index < HOTBAR_END) {
          if (!this.moveItemStackTo(
              sourceStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
          }
        } else {
          return ItemStack.EMPTY;
        }
      }
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
  public void removed(Player player) {
    super.removed(player);

    this.container.stopOpen(player);
  }
}
