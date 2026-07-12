package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.Enchantism;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings({"null"})
@Mod.EventBusSubscriber(modid = Enchantism.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LignificationEffectHandler {
  /*
   * Minecraft enchantment protection is capped at 20 points.
   * Each point corresponds to 4% damage reduction.
   */
  private static final int MAX_PROTECTION_POINTS = 20;
  private static final float PROTECTION_DIVISOR = 25.0F;

  private static final int SPECIALIZED_PROTECTION_MULTIPLIER = 2;
  private static final int FALL_PROTECTION_MULTIPLIER = 3;

  private LignificationEffectHandler() {}

  @SubscribeEvent
  public static void onLivingHurt(LivingHurtEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    if (player.level().isClientSide) {
      return;
    }

    if (!player.isCrouching()) {
      return;
    }

    int lignificationLevel = EnchantmentEffectHelper.getTotalLignificationLevel(player);

    if (lignificationLevel <= 0) {
      return;
    }

    DamageSource source = event.getSource();

    /*
     * Damage explicitly configured to bypass enchantments
     * must also bypass Lignification.
     */
    if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
      return;
    }

    int lignificationProtection = calculateLignificationProtection(lignificationLevel, source);

    if (lignificationProtection <= 0) {
      return;
    }

    /*
     * Obtain existing vanilla enchantment protection so that
     * Lignification and normal enchantments share the same cap.
     */
    int existingProtection = EnchantmentHelper.getDamageProtection(player.getArmorSlots(), source);

    int clampedExistingProtection = Mth.clamp(existingProtection, 0, MAX_PROTECTION_POINTS);

int combinedProtection =
    Mth.clamp(
        Math.max(
            existingProtection,
            lignificationProtection),
        0,
        MAX_PROTECTION_POINTS);

    if (combinedProtection <= clampedExistingProtection) {
      return;
    }

    /*
     * Vanilla protection will process existingProtection later.
     * Apply only the additional ratio required to reach
     * combinedProtection.
     */
    float existingFactor = 1.0F - (float) clampedExistingProtection / PROTECTION_DIVISOR;

    float combinedFactor = 1.0F - (float) combinedProtection / PROTECTION_DIVISOR;

    float additionalFactor = combinedFactor / existingFactor;

    event.setAmount(Math.max(0.0F, event.getAmount() * additionalFactor));
  }

  private static int calculateLignificationProtection(
    int level, DamageSource source) {
  int protectionPoints = level;

  if (source.is(DamageTypeTags.IS_PROJECTILE)
      || source.is(DamageTypeTags.IS_FIRE)
      || source.is(DamageTypeTags.IS_EXPLOSION)
      || isMagicLikeDamage(source)) {
    protectionPoints =
        Math.max(
            protectionPoints,
            level * SPECIALIZED_PROTECTION_MULTIPLIER);
  }

  if (source.is(DamageTypeTags.IS_FALL)) {
    protectionPoints =
        Math.max(
            protectionPoints,
            level * FALL_PROTECTION_MULTIPLIER);
  }

  return protectionPoints;
}

  private static boolean isMagicLikeDamage(DamageSource source) {
    return source.is(DamageTypes.MAGIC)
        || source.is(DamageTypes.INDIRECT_MAGIC)
        || source.is(DamageTypes.WITHER)
        || source.is(DamageTypes.DRAGON_BREATH)
        || source.is(DamageTypes.SONIC_BOOM);
  }
}
