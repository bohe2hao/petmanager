package com.github.bohe2hao.petmanager.network;

import static com.github.bohe2hao.petmanager.others.PetOthers.RECALL;
import static com.github.bohe2hao.petmanager.others.PetOthers.SERVER;

import com.github.bohe2hao.petmanager.others.PetOthers;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PetTeleportPack {

  MinecraftServer server;
  ResourceKey<Level> sourceLevelKey;
  ChunkPos sourceChunkPos;
  ResourceKey<Level> targetLevelKey;
  Vec3 targetPos;
  UUID entityId;
  Consumer<Entity> onSuccess;
  Runnable onFail;

  PetTeleportPack(
    MinecraftServer server,
    ResourceKey<Level> sourceLevelKey,
    ChunkPos sourceChunkPos,
    ResourceKey<Level> targetLevelKey,
    Vec3 targetPos,
    UUID entityId,
    Consumer<Entity> onSuccess,
    Runnable onFail
  ) {
    this.server = server;
    this.sourceLevelKey = sourceLevelKey;
    this.sourceChunkPos = sourceChunkPos;
    this.targetLevelKey = targetLevelKey;
    this.targetPos = targetPos;
    this.entityId = entityId;
    this.onSuccess = onSuccess;
    this.onFail = onFail;
  }

  void teleportEnhanced() {
    Entity entity = getEntity();
    if (entity == null) {
      fromUnloadChunk();
    } else {
        if(isCrossLevel(entity)){
            teleportAndRebuild(entity);
        }else{
            tryNormalTeleport(entity);
        }
    }
  }

  void tryNormalTeleport(Entity entity){
      ServerLevel level = server.getLevel(targetLevelKey);
      if (level == null) {
          onFail.run();
          return;
      }
      List<ServerPlayer> players=level.getChunkSource().chunkMap.getPlayers(new ChunkPos(BlockPos.containing(targetPos)), false);
      for(ServerPlayer player:players){
          boolean haveCantSeen= !level.getChunkSource().chunkMap.getPlayersWatching(entity).contains(player);
          if(haveCantSeen){
              teleportAndRebuild(entity);
              return;
          }
      }
      onSuccess.accept(entity);
      entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
      tryPostTeleportLoadChunk(level,entity);
  }

  void fromUnloadChunk() {
    ServerLevel sourceLevel = server.getLevel(sourceLevelKey);
    if (sourceLevel == null) {
      onFail.run();
      return;
    }

    sourceLevel.getChunkSource().addRegionTicket(RECALL, sourceChunkPos, 0, entityId);

    SERVER.add(0.2f, 0, unused -> teleportAndRebuild(getEntity()));
  }

  void teleportAndRebuild(@Nullable Entity entity) {
    ServerLevel level = server.getLevel(targetLevelKey);
    if (level == null) {
      onFail.run();
      return;
    }
    if (entity == null) {
      onFail.run();
      return;
    }
    onSuccess.accept(entity);
    entity.unRide();
    if (isCrossLevel(entity)) {
      entity.teleportTo(level, targetPos.x, targetPos.y, targetPos.z, Set.of(), entity.getYRot(), entity.getXRot());
    } else {
      entity.unRide();
      Entity entityNew = entity.getType().create(level);
      if (entityNew == null) {
        return;
      }
      entityNew.restoreFrom(entity);
      entityNew.teleportTo(targetPos.x, targetPos.y, targetPos.z);
      entityNew.setYHeadRot(entity.getYRot());
      entity.setRemoved(Entity.RemovalReason.DISCARDED);
      level.addDuringTeleport(entityNew);
      tryPostTeleportLoadChunk(level,entityNew);
    }
  }

  boolean isCrossLevel(Entity entity) {
    return !entity.level().dimension().equals(targetLevelKey);
  }

  void tryPostTeleportLoadChunk(ServerLevel level,Entity entity){
      if(level.isLoaded(BlockPos.containing(targetPos)))return;
      level.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(BlockPos.containing(targetPos)), 0, entity.getId());
  }

  @Nullable
  Entity getEntity() {
    return PetOthers.entity.findFromServer(server, entityId);
  }
}
