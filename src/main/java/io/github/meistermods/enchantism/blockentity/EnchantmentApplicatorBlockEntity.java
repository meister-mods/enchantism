package io.github.meistermods.enchantism.blockentity;

import io.github.meistermods.enchantism.element.ElementType;
import io.github.meistermods.enchantism.enchantment.ElementEnchantmentContext;
import io.github.meistermods.enchantism.enchantment.ElementEnchantmentSelector;
import io.github.meistermods.enchantism.enchantment.ElementUsage;
import io.github.meistermods.enchantism.enchantment.SelectedEnchantment;
import io.github.meistermods.enchantism.item.ElementContainerItem;
import io.github.meistermods.enchantism.menu.EnchantmentApplicatorMenu;
import io.github.meistermods.enchantism.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings({"null"})
public final class EnchantmentApplicatorBlockEntity extends BlockEntity
    implements MenuProvider, Container {
  public static final int ELEMENT_SLOT_START = 0;
  public static final int ELEMENT_SLOT_COUNT = 9;
  public static final int ELEMENT_SLOT_END = ELEMENT_SLOT_START + ELEMENT_SLOT_COUNT;

  public static final int BOOK_SLOT = 9;
  public static final int OUTPUT_SLOT = 10;
  public static final int SLOT_COUNT = 11;

  public static final int DEFAULT_PROCESS_TIME = 200;

  private static final int[] ELEMENT_COSTS = {100, 90, 80, 70, 60, 50, 40, 30, 20};

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

  public EnchantmentApplicatorBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.ENCHANTMENT_APPLICATOR.get(), pos, state);
  }

  public static void serverTick(
      Level level, BlockPos pos, BlockState state, EnchantmentApplicatorBlockEntity blockEntity) {
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
    ItemStack book = this.items.get(BOOK_SLOT);

    ItemStack output = this.items.get(OUTPUT_SLOT);

    if (!book.is(Items.BOOK)) {
      return false;
    }

    if (!output.isEmpty()) {
      return false;
    }

    List<ElementUsage> usages = this.createUsagePlan();

    return !usages.isEmpty();
  }

  private List<ElementUsage> createUsagePlan() {
    List<ElementUsage> usages = new ArrayList<>();

    for (int slot = ELEMENT_SLOT_START; slot < ELEMENT_SLOT_END; slot++) {
      ItemStack container = this.items.get(slot);

      if (container.isEmpty()) {
        continue;
      }

      if (!(container.getItem() instanceof ElementContainerItem)) {
        return List.of();
      }

      ElementType element = ElementContainerItem.getElement(container);

      if (element == ElementType.EMPTY) {
        return List.of();
      }

      int relativeSlot = slot - ELEMENT_SLOT_START;

      int requiredAmount = ELEMENT_COSTS[relativeSlot];

      int storedAmount = ElementContainerItem.getElementAmount(container);

      if (storedAmount < requiredAmount) {
        return List.of();
      }

      usages.add(new ElementUsage(slot, element, requiredAmount));
    }

    return usages;
  }

  private void process() {
    if (this.level == null || !this.canProcess()) {
      return;
    }

    List<ElementUsage> usages = this.createUsagePlan();

    if (usages.isEmpty()) {
      return;
    }

    ElementEnchantmentContext context = new ElementEnchantmentContext(usages);

    SelectedEnchantment selected = ElementEnchantmentSelector.select(context, this.level.random);

    /*
     * This should occur only when there are no valid
     * custom or fallback enchantments.
     */
    if (selected == null) {
      return;
    }

    List<ItemStack> updatedContainers = new ArrayList<>();

    for (ElementUsage usage : usages) {
      ItemStack copiedContainer = this.items.get(usage.slot()).copy();

      boolean consumed = ElementContainerItem.consumeElement(copiedContainer, usage.amount());

      if (!consumed) {
        return;
      }

      updatedContainers.add(copiedContainer);
    }

    ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);

    EnchantedBookItem.addEnchantment(
        result, new EnchantmentInstance(selected.enchantment(), selected.level()));

    for (int index = 0; index < usages.size(); index++) {
      ElementUsage usage = usages.get(index);

      this.items.set(usage.slot(), updatedContainers.get(index));
    }

    ItemStack book = this.items.get(BOOK_SLOT);

    book.shrink(1);

    if (book.isEmpty()) {
      this.items.set(BOOK_SLOT, ItemStack.EMPTY);
    }

    this.items.set(OUTPUT_SLOT, result);

    this.setChanged();
  }

  public static int getElementCost(int elementSlot) {
    if (elementSlot < ELEMENT_SLOT_START || elementSlot >= ELEMENT_SLOT_END) {
      return 0;
    }

    return ELEMENT_COSTS[elementSlot - ELEMENT_SLOT_START];
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
    return Component.translatable("container.enchantism.enchantment_applicator");
  }

  @Override
  public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
    return new EnchantmentApplicatorMenu(containerId, inventory, this, this.data);
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

    int maximumStackSize = this.isElementSlot(slot) ? 1 : this.getMaxStackSize();

    if (stack.getCount() > maximumStackSize) {
      stack.setCount(maximumStackSize);
    }

    this.setChanged();
  }

  private boolean isElementSlot(int slot) {
    return slot >= ELEMENT_SLOT_START && slot < ELEMENT_SLOT_END;
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    if (this.isElementSlot(slot)) {
      return stack.getItem() instanceof ElementContainerItem;
    }

    return switch (slot) {
      case BOOK_SLOT -> stack.is(Items.BOOK);

      case OUTPUT_SLOT -> false;

      default -> false;
    };
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
  public void clearContent() {
    this.items.clear();
    this.setChanged();
  }
}
