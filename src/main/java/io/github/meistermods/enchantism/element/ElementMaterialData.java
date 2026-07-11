package io.github.meistermods.enchantism.element;

import javax.annotation.Nonnull;

@SuppressWarnings({"null", "deprecation"})
public record ElementMaterialData(
        @Nonnull ElementType elementType,
        int amount
)
{
    public ElementMaterialData
    {
        if (amount < 1)
        {
            throw new IllegalArgumentException(
                    "Element material amount must be between 1 and 100"
            );
        }
    }
}