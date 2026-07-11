package io.github.meistermods.enchantism.element;

import net.minecraft.world.item.ItemStack;

@SuppressWarnings({"null"})
public final class ElementHelper
{
    private ElementHelper()
    {
    }

    public static ElementType getElementType(
        ItemStack stack
    )
    {
        if (stack.isEmpty())
        {
            return ElementType.EMPTY;
        }

        if (stack.is(ModElementTags.STONE))
        {
            return ElementType.STONE;
        }

        if (stack.is(ModElementTags.WOOD))
        {
            return ElementType.WOOD;
        }

        if (stack.is(ModElementTags.DUST))
        {
            return ElementType.DUST;
        }

        return ElementType.EMPTY;
    }
}