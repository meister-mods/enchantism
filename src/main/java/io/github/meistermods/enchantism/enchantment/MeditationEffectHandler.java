package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.Enchantism;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings({"null"})
@Mod.EventBusSubscriber(
    modid = Enchantism.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MeditationEffectHandler {
  private static final String CROUCH_TICKS_TAG =
      "EnchantismMeditationCrouchTicks";

  private static final String WAS_CROUCHING_TAG =
      "EnchantismMeditationWasCrouching";

  private static final int REQUIRED_CROUCH_TICKS = 60;
  private static final int LINGER_DURATION_TICKS = 60;
  private static final int ACTIVE_REFRESH_DURATION_TICKS = 10;

  private MeditationEffectHandler() {}

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.END) {
      return;
    }

    Player player = event.player;

    if (player.level().isClientSide) {
      return;
    }

    CompoundTag data = player.getPersistentData();

    int meditationLevel =
        EnchantmentEffectHelper.getMeditationLevel(player);

    boolean hasLignification =
        EnchantmentEffectHelper.hasLignification(player);

    if (meditationLevel <= 0 || hasLignification) {
      clearMeditationState(data);
      return;
    }

    boolean crouching = player.isCrouching();
    boolean wasCrouching = data.getBoolean(WAS_CROUCHING_TAG);
    int crouchTicks = data.getInt(CROUCH_TICKS_TAG);

    if (crouching) {
      crouchTicks++;

      data.putInt(CROUCH_TICKS_TAG, crouchTicks);
      data.putBoolean(WAS_CROUCHING_TAG, true);

      applyRegeneration(
          player,
          meditationLevel,
          ACTIVE_REFRESH_DURATION_TICKS);

      return;
    }

    if (wasCrouching && crouchTicks >= REQUIRED_CROUCH_TICKS) {
      applyRegeneration(
          player,
          meditationLevel,
          LINGER_DURATION_TICKS);
    }

    clearMeditationState(data);
  }

  private static void applyRegeneration(
      Player player,
      int meditationLevel,
      int duration) {
    int amplifier = meditationLevel - 1;

    MobEffectInstance currentEffect =
        player.getEffect(MobEffects.REGENERATION);

    if (currentEffect != null
        && currentEffect.getAmplifier() > amplifier) {
      return;
    }

    if (currentEffect != null
        && currentEffect.getAmplifier() == amplifier
        && currentEffect.getDuration() >= duration) {
      return;
    }

    player.addEffect(
        new MobEffectInstance(
            MobEffects.REGENERATION,
            duration,
            amplifier,
            true,
            false,
            true));
  }

  private static void clearMeditationState(CompoundTag data) {
    data.remove(CROUCH_TICKS_TAG);
    data.remove(WAS_CROUCHING_TAG);
  }
}