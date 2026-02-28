package com.github.bohe2hao.petmanager.event;

import com.github.bohe2hao.petmanager.PetManager;
import com.github.bohe2hao.petmanager.others.PetOthers;
import com.github.bohe2hao.petmanager.data.PetData;
import com.github.bohe2hao.petmanager.data.PetInfo;
import com.github.bohe2hao.petmanager.data.PlayerInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = PetManager.MODID)
public class PetEventListener {

  public static boolean noLegal(Level level) {
    return (PetData.isEmpty() || level.isClientSide);
  }

  public static void noRegisteredButHaveOwnerUpdate(Entity entity) {
    if (!(entity instanceof LivingEntity livingEntity)) return;
    if (!(livingEntity instanceof OwnableEntity ownableEntity)) return;
    if (PetData.getInstance().isRegistered(entity.getUUID())) return;
    if (ownableEntity.getOwner() == null) return;
    if (!(ownableEntity.getOwner() instanceof Player player)) return;
    PetData.getInstance().newPetRegistered((Entity) ownableEntity, player);
  }

  @SubscribeEvent
  public static void onEntityTick(EntityTickEvent.Post event) {
    Entity entity = event.getEntity();
    if (noLegal(entity.level())) return;
    noRegisteredButHaveOwnerUpdate(entity);
    PetData.getInstance().tryToUpdatePetInfo(event.getEntity());
  }

  @SubscribeEvent
  public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
    if (noLegal(event.getLevel())) return;
    PetData.getInstance().tryToUpdatePetInfo(event.getEntity());
  }

  @SubscribeEvent
  public static void onServerLoad(ServerStartedEvent event) {
    PetData.create();
  }

  @SubscribeEvent
  public static void onServerTick(ServerTickEvent.Pre event) {
    PetOthers.SERVER.tick(null);
  }

  @SubscribeEvent
  public static void onPlayerComeServer(PlayerEvent.PlayerLoggedInEvent event) {
    if (noLegal(event.getEntity().level())) return;
    if (PetData.getInstance().isRegisteredPlayer((ServerPlayer) event.getEntity())) return;
    PetData.getInstance().registerPlayerInfo((ServerPlayer) event.getEntity());
  }

  @SubscribeEvent
  public static void onPetHurt(AttackEntityEvent event) {
    Entity entity = event.getTarget();
    if (noLegal(entity.level())) return;
    if (PetData.getInstance().playerPetNotMatch(entity.getUUID(), event.getEntity().getUUID())) return;
    PetInfo petInfo = PetData.getInstance().getPetInfo(entity.getUUID());
    if (petInfo == null) return;
    if (petInfo.isCanceled()) return;
    PlayerInfo playerInfo = PetData.getInstance().getPlayerInfo(event.getEntity());
    if (playerInfo.isFriendFireEnable()) return;
    event.setCanceled(true);
  }
}
