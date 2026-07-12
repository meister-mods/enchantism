package io.github.meistermods.enchantism.event;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.enchantment.EnchantmentEffectHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings({"null"})
@Mod.EventBusSubscriber(modid = Enchantism.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModGameplayEvents {
  private ModGameplayEvents() {}

  /** Prevents hostile monsters from initially selecting a crouching player with Stealth. */
  @SubscribeEvent
  public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
    if (!(event.getEntity() instanceof Mob mob)) {
      return;
    }

    if (!(mob instanceof Enemy)) {
      return;
    }

    if (!(event.getNewTarget() instanceof Player player)) {
      return;
    }

    if (!EnchantmentEffectHelper.isStealthActive(player)) {
      return;
    }

    if (isBoss(mob)) {
      return;
    }

    /*
     * A monster attacked by this player may retaliate.
     */
    if (mob.getLastHurtByMob() == player) {
      return;
    }

    event.setNewTarget(null);
  }

  /** Clears targets that were already acquired before the player activated Lignification. */
  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.END) {
      return;
    }

    Player player = event.player;

    if (player.level().isClientSide) {
      return;
    }

    if (!EnchantmentEffectHelper.isStealthActive(player)) {
      return;
    }

    /*
     * Run once every 10 ticks rather than every tick.
     */
    if (player.tickCount % 10 != 0) {
      return;
    }

    double radius = 32.0D;

    for (Mob mob :
        player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius))) {
      if (!(mob instanceof Enemy)) {
        continue;
      }

      if (isBoss(mob)) {
        continue;
      }

      if (mob.getTarget() != player) {
        continue;
      }

      /*
       * Preserve retaliation when the player attacked first.
       */
      if (mob.getLastHurtByMob() == player) {
        continue;
      }

      mob.setTarget(null);
    }
  }

  private static boolean isBoss(Mob mob) {
    return mob instanceof WitherBoss || mob instanceof EnderDragon;
  }
}
