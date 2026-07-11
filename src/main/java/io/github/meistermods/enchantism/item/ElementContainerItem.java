package io.github.meistermods.enchantism.item;

import java.util.List;

import io.github.meistermods.enchantism.element.ElementType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

@SuppressWarnings({"null"})
public final class ElementContainerItem extends Item
{
    private static final String ELEMENT_TAG = "Element";
    private static final String AMOUNT_TAG = "Amount";

    public static final int MAX_ELEMENT_AMOUNT = 64;

    public ElementContainerItem(Properties properties)
    {
        super(properties);
    }

    public static ElementType getElement(
        ItemStack stack
    )
    {
        CompoundTag tag = stack.getTag();

        if (tag == null)
        {
            return ElementType.EMPTY;
        }

        return ElementType.fromName(
            tag.getString(ELEMENT_TAG)
        );
    }

    public static int getElementAmount(
        ItemStack stack
    )
    {
        CompoundTag tag = stack.getTag();

        if (tag == null)
        {
            return 0;
        }

        return Math.max(
            0,
            tag.getInt(AMOUNT_TAG)
        );
    }

    public static void setElement(
        ItemStack stack,
        ElementType element,
        int amount
    )
    {
        if (element == ElementType.EMPTY || amount <= 0)
        {
            stack.removeTagKey(ELEMENT_TAG);
            stack.removeTagKey(AMOUNT_TAG);
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(
            ELEMENT_TAG,
            element.getSerializedName()
        );

        tag.putInt(
            AMOUNT_TAG,
            Math.min(amount, MAX_ELEMENT_AMOUNT)
        );
    }

    public static boolean canAccept(
        ItemStack container,
        ElementType materialElement
    )
    {
        if (materialElement == ElementType.EMPTY)
        {
            return false;
        }

        ElementType storedElement = getElement(container);

        return storedElement == ElementType.EMPTY
            || storedElement == materialElement;
    }

    public static boolean addElement(
        ItemStack container,
        ElementType materialElement,
        int amount
    )
    {
        if (!canAccept(container, materialElement))
        {
            return false;
        }

        int currentAmount = getElementAmount(container);

        if (currentAmount >= MAX_ELEMENT_AMOUNT)
        {
            return false;
        }

        setElement(
            container,
            materialElement,
            currentAmount + amount
        );

        return true;
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Level level,
        List<Component> tooltip,
        TooltipFlag flag
    )
    {
        ElementType element = getElement(stack);
        int amount = getElementAmount(stack);

        if (element == ElementType.EMPTY)
        {
            tooltip.add(
                Component.translatable(
                    "tooltip.enchantism.element_container.empty"
                ).withStyle(ChatFormatting.GRAY)
            );

            return;
        }

        tooltip.add(
            Component.translatable(
                "tooltip.enchantism.element_container.element",
                element.getDisplayName()
            ).withStyle(ChatFormatting.AQUA)
        );

        tooltip.add(
            Component.translatable(
                "tooltip.enchantism.element_container.amount",
                amount,
                MAX_ELEMENT_AMOUNT
            ).withStyle(ChatFormatting.GRAY)
        );
    }
}