package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.registry.ModEnchantments;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings({"null", "deprecation"})
@Mod.EventBusSubscriber(modid = Enchantism.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SwiftBladeEffectHandler {
  private static final UUID ATTACK_SPEED_MODIFIER_UUID =
      UUID.fromString("64228e77-61e4-4c96-b334-70bf7dca5741");

  private static final String ATTACK_SPEED_MODIFIER_NAME = "enchantism.swift_blade.attack_speed";

  private static final double[] COOLDOWN_REDUCTIONS = {0.0, 0.125, 0.25, 0.375, 0.50, 0.675};

  private SwiftBladeEffectHandler() {}

  @SubscribeEvent
  public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
    if (event.getSlotType() != EquipmentSlot.MAINHAND) {
      return;
    }

    ItemStack stack = event.getItemStack();

    if (!SwiftBladeEnchantment.supportsAttackCooldown(stack)) {
      return;
    }

    int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SWIFT_BLADE.get(), stack);

    if (level <= 0) {
      return;
    }

    double cooldownReduction = getCooldownReduction(level);

    double attackSpeedMultiplier = 1.0D / (1.0D - cooldownReduction);

    double modifierAmount = attackSpeedMultiplier - 1.0D;

    event.addModifier(
        Attributes.ATTACK_SPEED,
        new AttributeModifier(
            ATTACK_SPEED_MODIFIER_UUID,
            ATTACK_SPEED_MODIFIER_NAME,
            modifierAmount,
            AttributeModifier.Operation.MULTIPLY_TOTAL));
  }

  public static double getCooldownReduction(int level) {
    int clampedLevel = Math.max(1, Math.min(level, COOLDOWN_REDUCTIONS.length - 1));

    return COOLDOWN_REDUCTIONS[clampedLevel];
  }
}
