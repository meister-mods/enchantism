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
@Mod.EventBusSubscriber(modid = Enchantism.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MeditationEffectHandler {
  private static final String CROUCH_TICKS_TAG = "EnchantismMeditationCrouchTicks";
  private static final String WAS_CROUCHING_TAG = "EnchantismMeditationWasCrouching";
  private static final String ANCHOR_INITIALIZED_TAG = "EnchantismMeditationAnchorInitialized";
  private static final String ANCHOR_X_TAG = "EnchantismMeditationAnchorX";
  private static final String ANCHOR_Y_TAG = "EnchantismMeditationAnchorY";
  private static final String ANCHOR_Z_TAG = "EnchantismMeditationAnchorZ";
  private static final String CHARGED_TAG = "EnchantismMeditationCharged";
  private static final String LINGER_TICKS_TAG = "EnchantismMeditationLingerTicks";
  private static final String EFFECT_APPLIED_TAG = "EnchantismMeditationEffectApplied";
  private static final String EFFECT_AMPLIFIER_TAG = "EnchantismMeditationEffectAmplifier";
  private static final int REQUIRED_CROUCH_TICKS = 60;
  private static final int LINGER_DURATION_TICKS = 60;

  /*
   * Regeneration I heals once every 50 ticks.
   * The active duration therefore needs to remain above that interval.
   */
  private static final int ACTIVE_EFFECT_DURATION_TICKS = 80;
  private static final int ACTIVE_REFRESH_THRESHOLD_TICKS = 20;

  /*
   * Approximately 0.001 blocks of movement is ignored.
   */
  private static final double MAX_MOVEMENT_DISTANCE_SQUARED = 1.0E-6D;

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

    int meditationLevel = EnchantmentEffectHelper.getMeditationLevel(player);

    boolean hasLignification = EnchantmentEffectHelper.hasLignification(player);

    if (meditationLevel <= 0 || hasLignification) {
      removeMeditationRegeneration(player, data);
      clearMeditationState(data);
      return;
    }

    tickLingeringTimer(data);

    boolean crouching = player.isCrouching();
    boolean wasCrouching = data.getBoolean(WAS_CROUCHING_TAG);

    int crouchTicks = data.getInt(CROUCH_TICKS_TAG);
    boolean charged = data.getBoolean(CHARGED_TAG);
    boolean lingering = isLingering(data);

    if (crouching) {
      if (!wasCrouching || !data.getBoolean(ANCHOR_INITIALIZED_TAG)) {
        beginMeditation(player, data);

        maintainRegeneration(player, data, meditationLevel);

        return;
      }

      if (!isAtAnchorPosition(player, data)) {
        /*
         * Movement after completing three seconds starts
         * the three-second lingering effect.
         */
        if (charged || crouchTicks >= REQUIRED_CROUCH_TICKS) {
          startLingeringRegeneration(player, data, meditationLevel);

          lingering = true;
        }

        /*
         * Movement before charging cancels the active effect.
         */
        if (!lingering) {
          removeMeditationRegeneration(player, data);
        }

        resetMeditationAtCurrentPosition(player, data);
        return;
      }

      crouchTicks++;

      data.putInt(CROUCH_TICKS_TAG, crouchTicks);

      data.putBoolean(WAS_CROUCHING_TAG, true);

      if (crouchTicks >= REQUIRED_CROUCH_TICKS) {
        data.putBoolean(CHARGED_TAG, true);
      }

      maintainRegeneration(player, data, meditationLevel);

      return;
    }

    /*
     * Standing after completing three seconds starts
     * the three-second lingering effect.
     */
    if (wasCrouching && (charged || crouchTicks >= REQUIRED_CROUCH_TICKS)) {
      startLingeringRegeneration(player, data, meditationLevel);

      lingering = true;
    }

    clearCrouchingState(data);

    /*
     * Before three seconds, standing immediately cancels
     * the regeneration supplied by Meditation.
     */
    if (!lingering) {
      removeMeditationRegeneration(player, data);
    }
  }

  private static void beginMeditation(Player player, CompoundTag data) {
    setAnchorPosition(player, data);

    data.putInt(CROUCH_TICKS_TAG, 1);

    data.putBoolean(WAS_CROUCHING_TAG, true);

    data.putBoolean(CHARGED_TAG, false);
  }

  private static void resetMeditationAtCurrentPosition(Player player, CompoundTag data) {
    setAnchorPosition(player, data);

    data.putInt(CROUCH_TICKS_TAG, 0);

    data.putBoolean(WAS_CROUCHING_TAG, true);

    data.putBoolean(CHARGED_TAG, false);
  }

  private static boolean isAtAnchorPosition(Player player, CompoundTag data) {
    double deltaX = player.getX() - data.getDouble(ANCHOR_X_TAG);
    double deltaY = player.getY() - data.getDouble(ANCHOR_Y_TAG);
    double deltaZ = player.getZ() - data.getDouble(ANCHOR_Z_TAG);
    double distanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

    return distanceSquared <= MAX_MOVEMENT_DISTANCE_SQUARED;
  }

  private static void setAnchorPosition(Player player, CompoundTag data) {
    data.putDouble(ANCHOR_X_TAG, player.getX());
    data.putDouble(ANCHOR_Y_TAG, player.getY());
    data.putDouble(ANCHOR_Z_TAG, player.getZ());

    data.putBoolean(ANCHOR_INITIALIZED_TAG, true);
  }

  private static void maintainRegeneration(Player player, CompoundTag data, int meditationLevel) {
    int amplifier = meditationLevel - 1;

    MobEffectInstance currentEffect = player.getEffect(MobEffects.REGENERATION);

    if (currentEffect == null) {
      addMeditationRegeneration(player, data, amplifier, ACTIVE_EFFECT_DURATION_TICKS);

      return;
    }

    /*
     * Do not replace a stronger external regeneration effect.
     */
    if (currentEffect.getAmplifier() > amplifier) {
      return;
    }

    /*
     * Replace a weaker effect with the Meditation level.
     */
    if (currentEffect.getAmplifier() < amplifier) {
      addMeditationRegeneration(player, data, amplifier, ACTIVE_EFFECT_DURATION_TICKS);

      return;
    }

    /*
     * Let the duration decrease normally so regeneration
     * healing ticks can occur. Refresh only near expiration.
     */
    if (currentEffect.getDuration() <= ACTIVE_REFRESH_THRESHOLD_TICKS) {
      addMeditationRegeneration(player, data, amplifier, ACTIVE_EFFECT_DURATION_TICKS);
    }
  }

  private static void startLingeringRegeneration(
      Player player, CompoundTag data, int meditationLevel) {
    data.putInt(LINGER_TICKS_TAG, LINGER_DURATION_TICKS);

    int amplifier = meditationLevel - 1;

    MobEffectInstance currentEffect = player.getEffect(MobEffects.REGENERATION);

    if (currentEffect != null && currentEffect.getAmplifier() > amplifier) {
      return;
    }

    if (currentEffect != null
        && currentEffect.getAmplifier() == amplifier
        && currentEffect.getDuration() >= LINGER_DURATION_TICKS) {
      return;
    }

    addMeditationRegeneration(player, data, amplifier, LINGER_DURATION_TICKS);
  }

  private static void addMeditationRegeneration(
      Player player, CompoundTag data, int amplifier, int duration) {
    player.addEffect(
        new MobEffectInstance(MobEffects.REGENERATION, duration, amplifier, true, false, true));

    data.putBoolean(EFFECT_APPLIED_TAG, true);

    data.putInt(EFFECT_AMPLIFIER_TAG, amplifier);
  }

  private static void removeMeditationRegeneration(Player player, CompoundTag data) {
    if (!data.getBoolean(EFFECT_APPLIED_TAG)) {
      return;
    }

    int appliedAmplifier = data.getInt(EFFECT_AMPLIFIER_TAG);

    MobEffectInstance currentEffect = player.getEffect(MobEffects.REGENERATION);

    /*
     * Remove the effect only when it still matches the
     * regeneration level supplied by Meditation.
     */
    if (currentEffect != null && currentEffect.getAmplifier() == appliedAmplifier) {
      player.removeEffect(MobEffects.REGENERATION);
    }

    clearEffectState(data);
  }

  private static void tickLingeringTimer(CompoundTag data) {
    int lingerTicks = data.getInt(LINGER_TICKS_TAG);

    if (lingerTicks <= 0) {
      data.remove(LINGER_TICKS_TAG);
      return;
    }

    lingerTicks--;

    if (lingerTicks <= 0) {
      data.remove(LINGER_TICKS_TAG);
    } else {
      data.putInt(LINGER_TICKS_TAG, lingerTicks);
    }
  }

  private static boolean isLingering(CompoundTag data) {
    return data.getInt(LINGER_TICKS_TAG) > 0;
  }

  private static void clearCrouchingState(CompoundTag data) {
    data.remove(CROUCH_TICKS_TAG);
    data.remove(WAS_CROUCHING_TAG);
    data.remove(CHARGED_TAG);
    data.remove(ANCHOR_INITIALIZED_TAG);
    data.remove(ANCHOR_X_TAG);
    data.remove(ANCHOR_Y_TAG);
    data.remove(ANCHOR_Z_TAG);
  }

  private static void clearEffectState(CompoundTag data) {
    data.remove(EFFECT_APPLIED_TAG);
    data.remove(EFFECT_AMPLIFIER_TAG);
  }

  private static void clearMeditationState(CompoundTag data) {
    clearCrouchingState(data);
    clearEffectState(data);
    data.remove(LINGER_TICKS_TAG);
  }
}
