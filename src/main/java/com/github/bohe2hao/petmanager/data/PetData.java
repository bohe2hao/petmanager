package com.github.bohe2hao.petmanager.data;

import static com.github.bohe2hao.petmanager.PetManager.PM_LOGGER;
import static com.github.bohe2hao.petmanager.PetManager.debug;

import com.github.bohe2hao.petmanager.PetManager;
import com.github.bohe2hao.petmanager.others.PetOthers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class PetData extends SavedData {

  public static PetData self;

  public static boolean isEmpty() {
    return self == null;
  }

  public static void create() {
    DimensionDataStorage data = Optional.ofNullable(ServerLifecycleHooks.getCurrentServer())
      .map(a -> a.overworld().getDataStorage())
      .orElse(null);
    if (data == null) {
      PM_LOGGER.error("PetData:null server");
      throw new IllegalStateException("PetData:inappropriateVisitingTime");
    }
    self = data.computeIfAbsent(new SavedData.Factory<>(PetData::new, PetData::load), PetManager.MODID);
  }

  public static PetData getInstance() {
    if (self == null) {
      PM_LOGGER.error("PetData:inappropriateVisitingTime");
      throw new IllegalStateException("PetData:inappropriateVisitingTime");
    }
    return self;
  }

  @Override
  public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
    CompoundTag petData = new CompoundTag();
    CompoundTag playerData = new CompoundTag();
    infoData.keySet().forEach(k -> petData.put(k.toString(), infoData.get(k).packAsNBT()));

    playerInfoData.keySet().forEach(k -> playerData.put(k.toString(), playerInfoData.get(k).packAsNbt()));

    compoundTag.put("pet", petData);
    compoundTag.put("player", playerData);
    return compoundTag;
  }

  public static PetData load(CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
    return new PetData().loadNBT(compoundTag);
  }

  public PetData loadNBT(CompoundTag compoundTag) {
    CompoundTag petData = compoundTag.getCompound("pet");
    petData.getAllKeys().forEach(k -> infoData.put(UUID.fromString(k), PetInfo.rebuild(petData.getCompound(k))));

    CompoundTag playerData = compoundTag.getCompound("player");
    playerData
      .getAllKeys()
      .forEach(k -> playerInfoData.put(UUID.fromString(k), PlayerInfo.rebuild(playerData.getCompound(k))));

    return this;
  }

  public boolean isRegisteredPlayer(ServerPlayer player) {
    return playerInfoData.containsKey(player.getUUID());
  }

  public void registerPlayerInfo(ServerPlayer player) {
    playerInfoData.put(player.getUUID(), new PlayerInfo());
    setDirty();
  }

  private final MinecraftServer server;
  private final ConcurrentHashMap<UUID, PetInfo> infoData = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, PlayerInfo> playerInfoData = new ConcurrentHashMap<>();

  public PlayerInfo getPlayerInfo(Player player) {
    return playerInfoData.computeIfAbsent(player.getUUID(), uuid -> new PlayerInfo());
  }

  @Nullable
  private PetInfo createPetInfo(Entity pet, UUID owner) {
    CompoundTag data = new CompoundTag();
    pet.save(data);
    if (data.isEmpty()) return null;
    ListTag pos = data.getList("Pos", Tag.TAG_DOUBLE);
    ChunkPos chunkPos = new ChunkPos(BlockPos.containing(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2)));
    return new PetInfo(
      owner,
      pet.level().dimension().location().toString(),
      data,
      chunkPos,
      System.currentTimeMillis()
    );
  }

  private PetInfo updateInfo(PetInfo info, Entity pet) {
    if (!info.getUUID().equals(pet.getUUID())) return info;
    PetInfo newInfo = createPetInfo(pet, info.owner);
    if (newInfo == null) return info;
    if (info.isCanceled()) newInfo.switchCanceled();
    return newInfo;
  }

  public void putInfoData(Entity pet, PetInfo info) {
    infoData.put(pet.getUUID(), info);
    setDirty();
  }

  public void newPetRegistered(Entity pet, Player owner) {
    putInfoData(pet, createPetInfo(pet, owner.getUUID()));
  }

  public boolean isRegistered(UUID pet) {
    return infoData.containsKey(pet);
  }

  public boolean playerPetNotMatch(UUID pet, UUID player) {
    return (!infoData.containsKey(pet) || !infoData.get(pet).owner.equals(player));
  }

  public Set<PetInfo> getPetInfosByPlayer(Player player) {
    UUID owner = player.getUUID();
    Set<UUID> found = new HashSet<>();
    Set<PetInfo> infos = new HashSet<>();
    //选出玩家的宠物
    Set<UUID> uuids = infoData
      .keySet()
      .stream()
      .filter(k -> infoData.get(k).owner.equals(owner))
      .collect(Collectors.toSet());
    //找出注销宠物
    uuids.forEach(u -> {
      PetInfo info = infoData.get(u);
      if (info.isCanceled()) {
        infos.add(info);
        found.add(u);
      }
    });

    //选出在线宠物的资料
    Set<UUID> other = uuids
      .stream()
      .filter(k -> !found.contains(k))
      .collect(Collectors.toSet());

    PetOthers.entity
      .findThemFromServer(server, other)
      .forEach(entity -> {
        CompoundTag data = new CompoundTag();
        entity.save(data);
        PetInfo info = createPetInfo(entity, player.getUUID());
        if (info == null) return;
        infos.add(info.setInfoState(PetInfo.State.onLine));
        found.add(entity.getUUID());
      });
    //选出不在线的宠物离线资料
    infoData
      .keySet()
      .stream()
      .filter(k -> !found.contains(k))
      .forEach(k -> infos.add(infoData.get(k).setInfoState(PetInfo.State.offLine)));
    return infos;
  }

  public PetInfo getOnlinePriorityPetInfo(Entity pet) {
    PetInfo info = infoData.get(pet.getUUID());
    if (info == null) return null;
    PetInfo newInfo = createPetInfo(pet, info.owner);
    if (newInfo == null) return info;
    return newInfo.setInfoState(PetInfo.State.onLine);
  }

  @Nullable
  public PetInfo getPetInfo(UUID petUUID) {
    return infoData.get(petUUID);
  }

  @Nullable
  public PetInfo getPetInfo(Entity pet) {
    return infoData.get(pet.getUUID());
  }

  //构造函数
  public PetData() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null) {
      String nullError = "PetData:server is null";
      PM_LOGGER.error(nullError);
      throw new RuntimeException(new Exception(nullError));
    }
    this.server = server;
  }

  public void tryToUpdatePetInfo(Entity pet) {
    PetInfo info = getPetInfo(pet);
    if (info == null) return;
    if (info.isCanceled()) return;
    Level level = pet.level();
    boolean isDimensionChange = !level.dimension().location().toString().equals(info.dimension);
    boolean isChangePos = !pet.chunkPosition().equals(info.getChunkPos());
    boolean isTimeEnough = System.currentTimeMillis() - info.lastUpdate > 50000;
    if (isTimeEnough || isDimensionChange) {
      PetInfo newInfo = updateInfo(info, pet);
      putInfoData(pet, newInfo);
      debug("petmanager:Data Dirty,dimensionChange:" + isDimensionChange);
    } else if (isChangePos) {
      info.changePos(pet.chunkPosition());
      debug("petmanager:Pos Dirty");
      setDirty();
    }
  }

  @Override
  public void setDirty() {
    super.setDirty();
  }
}
