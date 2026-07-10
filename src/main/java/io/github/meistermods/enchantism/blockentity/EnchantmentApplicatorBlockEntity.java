package io.github.meistermods.enchantism.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.meistermods.enchantism.enchantment.SpecialEnchantment;
import io.github.meistermods.enchantism.menu.EnchantmentApplicatorMenu;
import io.github.meistermods.enchantism.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("null")
public final class EnchantmentApplicatorBlockEntity extends BlockEntity implements MenuProvider, Container
{
    public static final int BOOK_SLOT = 0;
    public static final int MATERIAL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int DEFAULT_PROCESS_TIME = 200;

    private final NonNullList<ItemStack> items =
        NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int progress;
    private int maxProgress = DEFAULT_PROCESS_TIME;

    private final ContainerData data = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            return switch (index)
            {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value)
        {
            switch (index)
            {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                default ->
                {
                }
            }
        }

        @Override
        public int getCount()
        {
            return 2;
        }
    };

    public EnchantmentApplicatorBlockEntity(
        BlockPos pos,
        BlockState state
    )
    {
        super(
            ModBlockEntities.ENCHANTMENT_APPLICATOR.get(),
            pos,
            state
        );
    }

    public static void serverTick(
        Level level,
        BlockPos pos,
        BlockState state,
        EnchantmentApplicatorBlockEntity blockEntity
    )
    {
        if (!blockEntity.canProcess())
        {
            if (blockEntity.progress != 0)
            {
                blockEntity.progress = 0;
                setChanged(level, pos, state);
            }

            return;
        }

        blockEntity.progress++;

        if (blockEntity.progress >= blockEntity.maxProgress)
        {
            blockEntity.process();
            blockEntity.progress = 0;
        }

        setChanged(level, pos, state);
    }

    private boolean canProcess()
    {
        ItemStack book = this.items.get(BOOK_SLOT);
        ItemStack material = this.items.get(MATERIAL_SLOT);
        ItemStack output = this.items.get(OUTPUT_SLOT);

        if (!book.is(Items.BOOK))
        {
            return false;
        }

        if (material.isEmpty())
        {
            return false;
        }

        if (!output.isEmpty())
        {
            return false;
        }

        return !collectCandidates(material).isEmpty();
    }

    private void process()
    {
        if (this.level == null || !canProcess())
        {
            return;
        }

        ItemStack material = this.items.get(MATERIAL_SLOT);
        List<Enchantment> candidates = collectCandidates(material);

        if (candidates.isEmpty())
        {
            return;
        }

        Enchantment selected = candidates.get(
            this.level.random.nextInt(candidates.size())
        );

        int selectedLevel = getGrantedLevel(
            selected,
            material
        );

        ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);

        EnchantedBookItem.addEnchantment(
            result,
            new EnchantmentInstance(
                selected,
                selectedLevel
            )
        );

        this.items.get(BOOK_SLOT).shrink(1);
        this.items.get(MATERIAL_SLOT).shrink(1);

        if (this.items.get(BOOK_SLOT).isEmpty())
        {
            this.items.set(BOOK_SLOT, ItemStack.EMPTY);
        }

        if (this.items.get(MATERIAL_SLOT).isEmpty())
        {
            this.items.set(MATERIAL_SLOT, ItemStack.EMPTY);
        }

        this.items.set(OUTPUT_SLOT, result);

        setChanged();
    }

    /**
     * Normal enchantments are always eligible.
     *
     * Special enchantments are eligible only when their material
     * condition matches the inserted material.
     */
    private List<Enchantment> collectCandidates(ItemStack material)
    {
        List<Enchantment> candidates = new ArrayList<>();

        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT)
        {
            if (enchantment instanceof SpecialEnchantment special)
            {
                if (special.matchesMaterial(material)
                    && special.isAllowedOnBooks())
                {
                    candidates.add(special);
                }

                continue;
            }

            if (enchantment.isDiscoverable()
                && enchantment.isAllowedOnBooks())
            {
                candidates.add(enchantment);
            }
        }

        return candidates;
    }

    private int getGrantedLevel(
        Enchantment enchantment,
        ItemStack material
    )
    {
        if (enchantment instanceof SpecialEnchantment special)
        {
            int requestedLevel =
                special.getGrantedLevel(material);

            return Math.max(
                special.getMinLevel(),
                Math.min(
                    requestedLevel,
                    special.getMaxLevel()
                )
            );
        }

        int minLevel = enchantment.getMinLevel();
        int maxLevel = enchantment.getMaxLevel();

        if (minLevel >= maxLevel)
        {
            return minLevel;
        }

        return minLevel
            + this.level.random.nextInt(
                maxLevel - minLevel + 1
            );
    }

    public int getProgress()
    {
        return this.progress;
    }

    public int getMaxProgress()
    {
        return this.maxProgress;
    }

    public ContainerData getData()
    {
        return this.data;
    }

    public void dropContents()
    {
        if (this.level == null)
        {
            return;
        }

        Containers.dropContents(
            this.level,
            this.worldPosition,
            this
        );
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable(
            "container.enchantism.enchantment_applicator"
        );
    }

    @Override
    public AbstractContainerMenu createMenu(
        int containerId,
        Inventory inventory,
        Player player
    )
    {
        return new EnchantmentApplicatorMenu(
            containerId,
            inventory,
            this,
            this.data
        );
    }

    @Override
    protected void saveAdditional(
        CompoundTag tag
    )
    {
        super.saveAdditional(tag);

        tag.putInt("Progress", this.progress);
        tag.putInt("MaxProgress", this.maxProgress);

        ContainerHelper.saveAllItems(
            tag,
            this.items
        );
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);

        this.progress = tag.getInt("Progress");

        this.maxProgress = tag.contains("MaxProgress")
            ? tag.getInt("MaxProgress")
            : DEFAULT_PROCESS_TIME;

        ContainerHelper.loadAllItems(
            tag,
            this.items
        );
    }

    @Override
    public int getContainerSize()
    {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty()
    {
        for (ItemStack item : this.items)
        {
            if (!item.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot)
    {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(
            int slot,
            int amount
    )
    {
        ItemStack result = ContainerHelper.removeItem(
            this.items,
            slot,
            amount
        );

        if (!result.isEmpty())
        {
            setChanged();
        }

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        return ContainerHelper.takeItem(
            this.items,
            slot
        );
    }

    @Override
    public void setItem(
            int slot,
            ItemStack stack
    )
    {
        this.items.set(slot, stack);

        if (stack.getCount() > getMaxStackSize())
        {
            stack.setCount(getMaxStackSize());
        }

        setChanged();
    }

    @Override
    public boolean stillValid(Player player)
    {
        if (this.level == null)
        {
            return false;
        }

        if (this.level.getBlockEntity(this.worldPosition) != this)
        {
            return false;
        }

        return player.distanceToSqr(
            this.worldPosition.getX() + 0.5D,
            this.worldPosition.getY() + 0.5D,
            this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(
            int slot,
            ItemStack stack
    )
    {
        return switch (slot)
        {
            case BOOK_SLOT -> stack.is(Items.BOOK);
            case MATERIAL_SLOT -> !stack.isEmpty();
            case OUTPUT_SLOT -> false;
            default -> false;
        };
    }

    @Override
    public void clearContent()
    {
        this.items.clear();
        setChanged();
    }
}