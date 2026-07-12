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

  private static final int REQUIRED_CROUCH_TICKS = 60;
  private static final int LINGER_DURATION_TICKS = 60;

  /*
   * A short duration is refreshed while meditating.
   * When movement begins, the effect disappears shortly afterward.
   */
  private static final int ACTIVE_REFRESH_DURATION_TICKS = 5;

  /*
   * Movement smaller than 0.001 blocks is ignored to avoid
   * floating-point noise in player coordinates.
   *
   * This is the squared distance:
   * 0.001 * 0.001 = 0.000001
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
      clearMeditationState(data);
      return;
    }

    boolean crouching = player.isCrouching();
    boolean wasCrouching = data.getBoolean(WAS_CROUCHING_TAG);

    int crouchTicks = data.getInt(CROUCH_TICKS_TAG);

    if (crouching) {
      /*
       * Initialize the meditation position when crouching begins.
       */
      if (!wasCrouching || !data.getBoolean(ANCHOR_INITIALIZED_TAG)) {
        setAnchorPosition(player, data);

        data.putInt(CROUCH_TICKS_TAG, 1);
        data.putBoolean(WAS_CROUCHING_TAG, true);

        applyRegeneration(player, meditationLevel, ACTIVE_REFRESH_DURATION_TICKS);

        return;
      }

      /*
       * Movement cancels the current meditation sequence.
       * The new position becomes the starting position for
       * the next stationary sequence.
       */
      if (!isAtAnchorPosition(player, data)) {
        setAnchorPosition(player, data);

        data.putInt(CROUCH_TICKS_TAG, 0);
        data.putBoolean(WAS_CROUCHING_TAG, true);

        return;
      }

      crouchTicks++;

      data.putInt(CROUCH_TICKS_TAG, crouchTicks);
      data.putBoolean(WAS_CROUCHING_TAG, true);

      applyRegeneration(player, meditationLevel, ACTIVE_REFRESH_DURATION_TICKS);

      return;
    }

    /*
     * Lingering regeneration is granted only when the player
     * completed 60 consecutive stationary crouching ticks.
     */
    if (wasCrouching && crouchTicks >= REQUIRED_CROUCH_TICKS) {
      applyRegeneration(player, meditationLevel, LINGER_DURATION_TICKS);
    }

    clearMeditationState(data);
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

  private static void applyRegeneration(Player player, int meditationLevel, int duration) {
    int amplifier = meditationLevel - 1;

    MobEffectInstance currentEffect = player.getEffect(MobEffects.REGENERATION);

    /*
     * Do not overwrite a stronger regeneration effect.
     */
    if (currentEffect != null && currentEffect.getAmplifier() > amplifier) {
      return;
    }

    /*
     * Do not shorten an equal-strength regeneration effect.
     */
    if (currentEffect != null
        && currentEffect.getAmplifier() == amplifier
        && currentEffect.getDuration() >= duration) {
      return;
    }

    player.addEffect(
        new MobEffectInstance(MobEffects.REGENERATION, duration, amplifier, true, false, true));
  }

  private static void clearMeditationState(CompoundTag data) {
    data.remove(CROUCH_TICKS_TAG);
    data.remove(WAS_CROUCHING_TAG);
    data.remove(ANCHOR_INITIALIZED_TAG);
    data.remove(ANCHOR_X_TAG);
    data.remove(ANCHOR_Y_TAG);
    data.remove(ANCHOR_Z_TAG);
  }
}
