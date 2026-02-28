package com.github.bohe2hao.petmanager.data;

import java.util.UUID;
import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class PetInfo {

  public static final String OWNER = "owner";
  public static final String DIMENSION = "dimension";
  public static final String STATE = "state";
  public static final String LAST_UPDATE = "last_update";
  public static final String DATA = "data";
  public static final String POS = "pos";
  public static final String CANCELED = "canceled";

  public final UUID owner;
  public final CompoundTag entityData;
  private boolean isCanceled = false;

  private State infoState = State.offLine;

  public String dimension;
  private ChunkPos chunkPosCache;

  public Entity clientEntity;
  public final long lastUpdate;

  PetInfo(UUID owner, String dimension, CompoundTag entityData, ChunkPos chunkPos, long lastUpdate) {
    this.owner = owner;
    this.entityData = entityData;
    this.dimension = dimension;
    this.lastUpdate = lastUpdate;
    this.chunkPosCache = chunkPos;
  }

  public CompoundTag packAsNBT() {
    CompoundTag petData = new CompoundTag();
    CompoundTag pos = new CompoundTag();
    pos.putInt("X", chunkPosCache.x);
    pos.putInt("z", chunkPosCache.z);
    petData.putUUID(OWNER, owner);
    petData.put(DATA, entityData);
    petData.putString(DIMENSION, dimension);
    petData.putString(STATE, infoState.name());
    petData.put(POS, pos);
    petData.putLong(LAST_UPDATE, lastUpdate);
    petData.putBoolean(CANCELED, isCanceled);
    return petData;
  }

  public static PetInfo rebuild(@Nonnull CompoundTag nbt) {
    CompoundTag posNbt = nbt.getCompound(POS);

    UUID owner = nbt.getUUID(PetInfo.OWNER);
    CompoundTag data = nbt.getCompound(PetInfo.DATA);
    String dimension = nbt.getString(PetInfo.DIMENSION);
    ChunkPos pos = new ChunkPos(posNbt.getInt("X"), posNbt.getInt("z"));
    long lastUpdate = nbt.getLong(PetInfo.LAST_UPDATE);
    boolean isCanceled = nbt.getBoolean(PetInfo.CANCELED);
    PetInfo info = new PetInfo(owner, dimension, data, pos, lastUpdate).setInfoState(
      State.valueOf(nbt.getString(STATE))
    );
    info.isCanceled = isCanceled;
    return info;
  }

  public void changePos(ChunkPos pos) {
    chunkPosCache = pos;
  }

  public ChunkPos getChunkPos() {
    return chunkPosCache;
  }

  public State getInfoState() {
    return infoState;
  }

  public UUID getUUID() {
    return entityData.getUUID("UUID");
  }

  public Component getDisplayName() {
    if (clientEntity == null) return Component.literal("null error");
    return clientEntity.getDisplayName();
  }

  public PetInfo setInfoState(State infoState) {
    this.infoState = infoState;
    return this;
  }

  public boolean isCanceled() {
    return isCanceled;
  }

  public void switchCanceled() {
    isCanceled = !isCanceled;
    PetData.getInstance().setDirty();
  }

  public enum State {
    onLine,
    offLine,
    missing
  }
}
