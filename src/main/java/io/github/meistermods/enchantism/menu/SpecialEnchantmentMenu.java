package io.github.meistermods.enchantism.menu;

import java.util.List;

import io.github.meistermods.enchantism.registry.ModBlocks;
import io.github.meistermods.enchantism.registry.ModMenus;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.EnchantmentTableBlock;

@SuppressWarnings({"null", "deprecation"})
public final class SpecialEnchantmentMenu extends AbstractContainerMenu
{
    private static final int TARGET_SLOT = 0;
    private static final int MATERIAL_SLOT = 1;
    private static final int PLAYER_INVENTORY_START = 2;
    private static final int PLAYER_INVENTORY_END = 29;
    private static final int HOTBAR_START = 29;
    private static final int HOTBAR_END = 38;

    private final Container enchantSlots = new SimpleContainer(2)
    {
        @Override
        public void setChanged()
        {
            super.setChanged();
            SpecialEnchantmentMenu.this.slotsChanged(this);
        }
    };

    private final ContainerLevelAccess access;
    private final RandomSource random = RandomSource.create();
    private final DataSlot enchantmentSeed = DataSlot.standalone();

    public final int[] costs = new int[3];
    public final int[] enchantClue = {-1, -1, -1};
    public final int[] levelClue = {-1, -1, -1};

    public SpecialEnchantmentMenu(int containerId, Inventory inventory)
    {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public SpecialEnchantmentMenu(
        int containerId,
        Inventory inventory,
        ContainerLevelAccess access
    )
    {
        super(ModMenus.SPECIAL_ENCHANTMENT.get(), containerId);
        this.access = access;
        this.enchantmentSeed.set(inventory.player.getEnchantmentSeed());

        // Item to enchant.
        this.addSlot(new Slot(this.enchantSlots, TARGET_SLOT, 15, 47)
        {
            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        // Any non-empty item may be used as the enchanting material.
        this.addSlot(new Slot(this.enchantSlots, MATERIAL_SLOT, 35, 47)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return !stack.isEmpty();
            }
        });

        // Player inventory.
        for (int row = 0; row < 3; row++)
        {
            for (int column = 0; column < 9; column++)
            {
                this.addSlot(new Slot(
                    inventory,
                    column + row * 9 + 9,
                    8 + column * 18,
                    84 + row * 18
                ));
            }
        }

        // Hotbar.
        for (int column = 0; column < 9; column++)
        {
            this.addSlot(new Slot(
                inventory,
                column,
                8 + column * 18,
                142
            ));
        }

        for (int i = 0; i < 3; i++)
        {
            this.addDataSlot(DataSlot.shared(this.costs, i));
        }

        for (int i = 0; i < 3; i++)
        {
            this.addDataSlot(DataSlot.shared(this.enchantClue, i));
        }

        for (int i = 0; i < 3; i++)
        {
            this.addDataSlot(DataSlot.shared(this.levelClue, i));
        }

        this.addDataSlot(this.enchantmentSeed);
    }

    @Override
    public void slotsChanged(Container container)
    {
        if (container != this.enchantSlots)
        {
            return;
        }

        ItemStack target = container.getItem(TARGET_SLOT);

        if (target.isEmpty() || !target.isEnchantable())
        {
            clearOffers();
            return;
        }

        this.access.execute((level, tablePos) ->
        {
            float enchantmentPower = 0.0F;

            for (BlockPos offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS)
            {
                if (EnchantmentTableBlock.isValidBookShelf(level, tablePos, offset))
                {
                    BlockPos shelfPos = tablePos.offset(offset);
                    enchantmentPower += level.getBlockState(shelfPos)
                        .getEnchantPowerBonus(level, shelfPos);
                }
            }

            this.random.setSeed(this.enchantmentSeed.get());

            for (int option = 0; option < 3; option++)
            {
                this.costs[option] = EnchantmentHelper.getEnchantmentCost(
                    this.random,
                    option,
                    (int) enchantmentPower,
                    target
                );

                this.enchantClue[option] = -1;
                this.levelClue[option] = -1;

                if (this.costs[option] < option + 1)
                {
                    this.costs[option] = 0;
                }
            }

            for (int option = 0; option < 3; option++)
            {
                if (this.costs[option] <= 0)
                {
                    continue;
                }

                List<EnchantmentInstance> offers = getEnchantmentList(
                    target,
                    option,
                    this.costs[option]
                );

                if (!offers.isEmpty())
                {
                    EnchantmentInstance clue =
                        offers.get(this.random.nextInt(offers.size()));

                    this.enchantClue[option] =
                        BuiltInRegistries.ENCHANTMENT.getId(clue.enchantment);
                    this.levelClue[option] = clue.level;
                }
            }

            this.broadcastChanges();
        });
    }

    private void clearOffers()
    {
        for (int option = 0; option < 3; option++)
        {
            this.costs[option] = 0;
            this.enchantClue[option] = -1;
            this.levelClue[option] = -1;
        }

        this.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int option)
    {
        if (option < 0 || option >= this.costs.length)
        {
            return false;
        }

        ItemStack target = this.enchantSlots.getItem(TARGET_SLOT);
        ItemStack material = this.enchantSlots.getItem(MATERIAL_SLOT);
        int materialCost = option + 1;
        int requiredLevel = this.costs[option];

        if (requiredLevel <= 0 || target.isEmpty() || !target.isEnchantable())
        {
            return false;
        }

        if (material.isEmpty() || material.getCount() < materialCost)
        {
            return false;
        }

        if (!player.getAbilities().instabuild
            && (player.experienceLevel < materialCost
            || player.experienceLevel < requiredLevel))
        {
            return false;
        }

        this.access.execute((level, tablePos) ->
        {
            List<EnchantmentInstance> offers =
                getEnchantmentList(target, option, requiredLevel);

            if (offers.isEmpty())
            {
                return;
            }

            player.onEnchantmentPerformed(target, materialCost);

            boolean isBook = target.is(Items.BOOK);
            ItemStack enchantedTarget = target;

            if (isBook)
            {
                enchantedTarget = new ItemStack(Items.ENCHANTED_BOOK);
                this.enchantSlots.setItem(TARGET_SLOT, enchantedTarget);
            }

            for (EnchantmentInstance offer : offers)
            {
                if (isBook)
                {
                    EnchantedBookItem.addEnchantment(enchantedTarget, offer);
                }
                else
                {
                    enchantedTarget.enchant(offer.enchantment, offer.level);
                }
            }

            player.awardStat(Stats.ENCHANT_ITEM);

            if (player instanceof ServerPlayer serverPlayer)
            {
                CriteriaTriggers.ENCHANTED_ITEM.trigger(
                    serverPlayer,
                    enchantedTarget,
                    materialCost
                );
            }

            material.shrink(materialCost);

            if (material.isEmpty())
            {
                this.enchantSlots.setItem(MATERIAL_SLOT, ItemStack.EMPTY);
            }
            else
            {
                this.enchantSlots.setChanged();
            }

            this.enchantSlots.setChanged();
            this.enchantmentSeed.set(player.getEnchantmentSeed());
            this.slotsChanged(this.enchantSlots);

            level.playSound(
                null,
                tablePos,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS,
                1.0F,
                level.random.nextFloat() * 0.1F + 0.9F
            );
        });

        return true;
    }

    private List<EnchantmentInstance> getEnchantmentList(
        ItemStack target,
        int option,
        int level
    )
    {
        this.random.setSeed(this.enchantmentSeed.get() + option);

        List<EnchantmentInstance> offers =
            EnchantmentHelper.selectEnchantment(
                this.random,
                target,
                level,
                false
            );

        // Vanilla removes one random result when enchanting a normal book.
        if (target.is(Items.BOOK) && offers.size() > 1)
        {
            offers.remove(this.random.nextInt(offers.size()));
        }

        return offers;
    }

    public int getMaterialCount()
    {
        ItemStack material = this.enchantSlots.getItem(MATERIAL_SLOT);
        return material.isEmpty() ? 0 : material.getCount();
    }

    public int getEnchantmentSeed()
    {
        return this.enchantmentSeed.get();
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        this.access.execute((level, pos) ->
            this.clearContainer(player, this.enchantSlots));
    }

    @Override
    public boolean stillValid(Player player)
    {
        return stillValid(
            this.access,
            player,
            ModBlocks.SPECIAL_ENCHANTMENT_TABLE.get()
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack copied = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(index);

        if (!sourceSlot.hasItem())
        {
            return copied;
        }

        ItemStack source = sourceSlot.getItem();
        copied = source.copy();

        if (index == TARGET_SLOT || index == MATERIAL_SLOT)
        {
            if (!this.moveItemStackTo(
                source,
                PLAYER_INVENTORY_START,
                HOTBAR_END,
                true
            ))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (source.isEnchantable()
            && !this.slots.get(TARGET_SLOT).hasItem())
        {
            if (!this.moveItemStackTo(
                source,
                TARGET_SLOT,
                TARGET_SLOT + 1,
                false
            ))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (!this.slots.get(MATERIAL_SLOT).hasItem())
        {
            if (!this.moveItemStackTo(
                source,
                MATERIAL_SLOT,
                MATERIAL_SLOT + 1,
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
                source,
                HOTBAR_START,
                HOTBAR_END,
                false
            ))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (index >= HOTBAR_START && index < HOTBAR_END)
        {
            if (!this.moveItemStackTo(
                source,
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

        if (source.isEmpty())
        {
            sourceSlot.set(ItemStack.EMPTY);
        }
        else
        {
            sourceSlot.setChanged();
        }

        if (source.getCount() == copied.getCount())
        {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, source);
        return copied;
    }
}
