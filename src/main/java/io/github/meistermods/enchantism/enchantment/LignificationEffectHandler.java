package io.github.meistermods.enchantism.enchantment;

import io.github.meistermods.enchantism.Enchantism;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings({"null"})
@Mod.EventBusSubscriber(modid = Enchantism.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LignificationEffectHandler {
  /*
   * The effect is kept short so it ends soon after
   * the player stops crouching.
   */
  private static final int EFFECT_DURATION_TICKS = 10;
  private static final int REFRESH_THRESHOLD_TICKS = 4;

  private LignificationEffectHandler() {}

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.END) {
      return;
    }

    Player player = event.player;

    if (player.level().isClientSide) {
      return;
    }

    if (!EnchantmentEffectHelper.isLignificationActive(player)) {
      return;
    }

    maintainEffect(player, MobEffects.DAMAGE_RESISTANCE, 0);

    maintainEffect(player, MobEffects.FIRE_RESISTANCE, 0);
  }

  private static void maintainEffect(Player player, MobEffect effect, int amplifier) {
    MobEffectInstance currentEffect = player.getEffect(effect);

    /*
     * Do not overwrite a stronger external effect.
     */
    if (currentEffect != null && currentEffect.getAmplifier() > amplifier) {
      return;
    }

    /*
     * Do not shorten an equal-strength external effect.
     */
    if (currentEffect != null
        && currentEffect.getAmplifier() == amplifier
        && currentEffect.getDuration() > REFRESH_THRESHOLD_TICKS) {
      return;
    }

    player.addEffect(
        new MobEffectInstance(effect, EFFECT_DURATION_TICKS, amplifier, true, false, true));
  }
}
