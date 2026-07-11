package io.github.meistermods.enchantism.blockentity;

import io.github.meistermods.enchantism.element.ElementHelper;
import io.github.meistermods.enchantism.element.ElementMaterialData;
import io.github.meistermods.enchantism.item.ElementContainerItem;
import io.github.meistermods.enchantism.menu.ElementInfuserMenu;
import io.github.meistermods.enchantism.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"null"})
public final class ElementInfuserBlockEntity extends BlockEntity
    implements MenuProvider, Container {
  public static final int MATERIAL_SLOT = 0;
  public static final int CONTAINER_SLOT = 1;

  public static final int SLOT_COUNT = 2;
  public static final int DEFAULT_PROCESS_TIME = 20;

  private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

  private int progress;
  private int maxProgress = DEFAULT_PROCESS_TIME;

  private final ContainerData data =
      new ContainerData() {
        @Override
        public int get(int index) {
          return switch (index) {
            case 0 -> progress;
            case 1 -> maxProgress;
            default -> 0;
          };
        }

        @Override
        public void set(int index, int value) {
          switch (index) {
            case 0 -> progress = value;
            case 1 -> maxProgress = value;
            default -> {}
          }
        }

        @Override
        public int getCount() {
          return 2;
        }
      };

  public ElementInfuserBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.ELEMENT_INFUSER.get(), pos, state);
  }

  public static void serverTick(
      Level level, BlockPos pos, BlockState state, ElementInfuserBlockEntity blockEntity) {
    if (!blockEntity.canProcess()) {
      if (blockEntity.progress != 0) {
        blockEntity.progress = 0;

        setChanged(level, pos, state);
      }

      return;
    }

    blockEntity.progress++;

    if (blockEntity.progress >= blockEntity.maxProgress) {
      blockEntity.process();
      blockEntity.progress = 0;
    }

    setChanged(level, pos, state);
  }

  private boolean canProcess() {
    ItemStack material = this.items.get(MATERIAL_SLOT);

    ItemStack container = this.items.get(CONTAINER_SLOT);

    if (material.isEmpty()) {
      return false;
    }

    if (!(container.getItem() instanceof ElementContainerItem)) {
      return false;
    }

    ElementMaterialData materialData = ElementHelper.getMaterialData(material);

    if (materialData == null) {
      return false;
    }

    if (!ElementContainerItem.canAccept(container, materialData.elementType())) {
      return false;
    }

    int currentAmount = ElementContainerItem.getElementAmount(container);

    long resultAmount = (long) currentAmount + materialData.amount();

    return resultAmount <= ElementContainerItem.MAX_ELEMENT_AMOUNT;
  }

  private void process() {
    if (!this.canProcess()) {
      return;
    }

    ItemStack material = this.items.get(MATERIAL_SLOT);

    ItemStack container = this.items.get(CONTAINER_SLOT);

    ElementMaterialData materialData = ElementHelper.getMaterialData(material);

    if (materialData == null) {
      return;
    }

    boolean success =
        ElementContainerItem.addElement(
            container, materialData.elementType(), materialData.amount());

    if (!success) {
      return;
    }

    material.shrink(1);

    if (material.isEmpty()) {
      this.items.set(MATERIAL_SLOT, ItemStack.EMPTY);
    }

    this.setChanged();
  }

  public ContainerData getData() {
    return this.data;
  }

  public void dropContents() {
    if (this.level == null) {
      return;
    }

    Containers.dropContents(this.level, this.worldPosition, this);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("container.enchantism.element_infuser");
  }

  @Override
  @Nullable
  public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
    return new ElementInfuserMenu(containerId, inventory, this, this.data);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);

    tag.putInt("Progress", this.progress);

    tag.putInt("MaxProgress", this.maxProgress);

    ContainerHelper.saveAllItems(tag, this.items);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);

    this.progress = tag.getInt("Progress");

    this.maxProgress =
        tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_PROCESS_TIME;

    ContainerHelper.loadAllItems(tag, this.items);
  }

  @Override
  public int getContainerSize() {
    return SLOT_COUNT;
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack stack : this.items) {
      if (!stack.isEmpty()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public ItemStack getItem(int slot) {
    return this.items.get(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);

    if (!result.isEmpty()) {
      this.setChanged();
    }

    return result;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    return ContainerHelper.takeItem(this.items, slot);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    this.items.set(slot, stack);

    int maximumStackSize = slot == CONTAINER_SLOT ? 1 : this.getMaxStackSize();

    if (stack.getCount() > maximumStackSize) {
      stack.setCount(maximumStackSize);
    }

    this.setChanged();
  }

  @Override
  public boolean stillValid(Player player) {
    if (this.level == null) {
      return false;
    }

    if (this.level.getBlockEntity(this.worldPosition) != this) {
      return false;
    }

    return player.distanceToSqr(
            this.worldPosition.getX() + 0.5D,
            this.worldPosition.getY() + 0.5D,
            this.worldPosition.getZ() + 0.5D)
        <= 64.0D;
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    return switch (slot) {
      case MATERIAL_SLOT -> ElementHelper.isValidMaterial(stack);

      case CONTAINER_SLOT -> stack.getItem() instanceof ElementContainerItem;

      default -> false;
    };
  }

  @Override
  public void clearContent() {
    this.items.clear();
    this.setChanged();
  }
}
