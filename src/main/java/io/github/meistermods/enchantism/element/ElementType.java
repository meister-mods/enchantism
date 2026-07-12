package io.github.meistermods.enchantism.element;

import java.util.Locale;
import net.minecraft.network.chat.Component;

@SuppressWarnings({"null"})
public enum ElementType {
  EMPTY,
  STONE,
  WOOD,
  DUST,
  METAL,
  CRYSTAL,
  LIFE,
  WATER,
  FIRE;

  public String getSerializedName() {
    return this.name().toLowerCase(Locale.ROOT);
  }

  public Component getDisplayName() {
    return Component.translatable("element.enchantism." + this.getSerializedName());
  }

  public static ElementType fromName(String name) {
    if (name == null || name.isBlank()) {
      return EMPTY;
    }

    for (ElementType type : values()) {
      if (type.getSerializedName().equals(name)) {
        return type;
      }
    }

    return EMPTY;
  }
}
