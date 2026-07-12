package io.github.meistermods.enchantism.element;

import java.util.Locale;
import net.minecraft.network.chat.Component;

public enum ElementType {
  EMPTY(0xFFFFFF),
  STONE(0x808080),
  WOOD(0x8B5A2B),
  DUST(0xC8B090),
  METAL(0xB8C0C8),
  CRYSTAL(0x66DDEE),
  LIFE(0x55CC55),
  WATER(0x3F76E4),
  FIRE(0xFF6A00),
  MYSTICAL(0xB040FF);

  private final int color;

  ElementType(int color) {
    this.color = color;
  }

  public int getColor() {
    return this.color;
  }

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
