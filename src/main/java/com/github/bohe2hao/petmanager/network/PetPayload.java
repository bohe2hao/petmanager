package com.github.bohe2hao.petmanager.network;

import com.github.bohe2hao.petmanager.PetManager;
import com.github.bohe2hao.petmanager.others.PetOthers;
import com.github.bohe2hao.petmanager.data.PetData;
import com.github.bohe2hao.petmanager.data.PetInfo;
import com.github.bohe2hao.petmanager.data.PlayerInfo;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PetPayload(
  PetNetwork.PetAbout action,
  @Nonnull UUID pet,
  @Nullable CompoundTag info
) implements CustomPacketPayload {
  public static final Type<PetPayload> TYPE = new Type<>(
    ResourceLocation.fromNamespaceAndPath(PetManager.MODID, "pet_payload")
  );

  public String getAction() {
    return this.action.name();
  }

  public String getPet() {
    return pet.toString();
  }

  public Optional<CompoundTag> getInfo() {
    return Optional.ofNullable(info);
  }

  public static PetPayload reBuild(String action, String pet, @Nullable CompoundTag info) {
    UUID petUUID = UUID.fromString(pet);
    return new PetPayload(PetNetwork.PetAbout.valueOf(action), petUUID, info);
  }

  public static final StreamCodec<ByteBuf, PetPayload> CODEC = StreamCodec.composite(
          ByteBufCodecs.STRING_UTF8,
          PetPayload::getAction,
          ByteBufCodecs.STRING_UTF8,
          PetPayload::getPet,
          ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG),
          PetPayload::getInfo,
          (action, pet, info) -> reBuild(action, pet, info.orElse(null))
  );

  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void hand(PetPayload pack, IPayloadContext context) {
    if (context.flow().isServerbound()) {
      context.enqueueWork(() -> {
        PlayerInfo playerInfo = PetData.getInstance().getPlayerInfo(context.player());
        if (!playerInfo.isCoolDownEnough()) {
          FeedBack.of(context, FeedBack.ACTION_IS_COOLING_DOWN);
          return;
        }
        Optional<OnServerWork> work = OnServerWork.safeGet(pack, context);
        switch (pack.action()) {
          case RECALL -> work.ifPresent(OnServerWork::recall);
          case RENAME -> work.ifPresent(OnServerWork::rename);
          case GO_HOME -> work.ifPresent(OnServerWork::goHome);
          case SWITCH_CANCELED -> work.ifPresent(OnServerWork::switchCanceled);
        }
      });
    }
  }
}

class OnServerWork {

  @Nonnull
  PetData petData = PetData.getInstance();

  Player player;
  PetPayload actionPack;
  ChunkPos infoChunkPos;
  ServerLevel playerLevel;
  IPayloadContext context;
  UUID petUUID;
  PetInfo info;
  MinecraftServer server;

  @Nullable
  ServerLevel infoLevel;

  @Nullable
  Entity foundPet;

  Runnable recallSuccess = () -> FeedBack.of(context, FeedBack.TELEPORT_SUCCESS);
  Runnable recallFail = () -> FeedBack.of(context, FeedBack.TELEPORT_FAIL);
  Runnable goHomeSuccess =()->FeedBack.of(context, FeedBack.GO_HOME_SUCCESSFUL);
  Runnable goHomeFail =()->FeedBack.of(context, FeedBack.GO_HOME_FAILED);

  void sendError(String errorReason) {
    player.sendSystemMessage(Component.literal("Unexpected error, maybe you should try " + "again:" + errorReason));
  }

  void recallGlowAndReply(Entity entity) {
    PetOthers.glow(entity);
    recallSuccess.run();
  }

  void goHomeGlowAndReply(Entity entity) {
    PetOthers.glow(entity);
    goHomeSuccess.run();
  }

  static Optional<OnServerWork> safeGet(PetPayload actionPack, IPayloadContext context) {
    PlayerInfo playerInfo = PetData.getInstance().getPlayerInfo(context.player());
    playerInfo.setCd();
    OnServerWork work = new OnServerWork();
    work.player = context.player();
    context.reply(new PMPayload(PetNetwork.Action.IN_CD));
    if (PetData.getInstance().playerPetNotMatch(actionPack.pet(), work.player.getUUID())) return Optional.empty();
    work.context = context;
    work.actionPack = actionPack;
    work.petUUID = actionPack.pet();
    work.info = work.petData.getPetInfo(work.petUUID);
    work.server = context.player().getServer();

    if (work.server == null || work.info == null) return Optional.empty();
    if (!(work.player.level() instanceof ServerLevel s)) return Optional.empty();
    work.infoLevel = work.server.getLevel(
      ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(work.info.dimension))
    );
    work.playerLevel = s;
    work.infoChunkPos = work.info.getChunkPos();
    work.foundPet = PetOthers.entity.findFromServer(
      Objects.requireNonNull(context.player().getServer()),
      actionPack.pet()
    );

    return Optional.of(work);
  }

  private OnServerWork() {}

  void rename() {
    int allExperience = player.totalExperience;
    PetPayload pack = actionPack;

    if (allExperience < 5) {
      FeedBack.of(context, FeedBack.NO_EXP, FeedBack.NO_EXP);
      return;
    }
    if (foundPet == null) {
      FeedBack.of(context, FeedBack.PET_OFF_LINE, FeedBack.PET_OFF_LINE);
      return;
    }
    if (pack.info() == null) {
      FeedBack.of(context, FeedBack.NULL_NAME, FeedBack.NULL_NAME);
      return;
    }
    //校验完成
    player.giveExperiencePoints(-5);
    foundPet.setCustomName(Component.literal(pack.info().getString(PetNetwork.PetAbout.RENAME.name())));
    FeedBack.of(context, FeedBack.RENAME_SUCCESS, FeedBack.RENAME_SUCCESS);

    context.reply(
      new PMPayload(PetNetwork.Action.NEW_PET_INFO, PetData.getInstance().getOnlinePriorityPetInfo(foundPet).packAsNBT())
    );
  }

  void goHome() {
    PlayerInfo playerInfo = petData.getPlayerInfo(player);
    GlobalPos home = playerInfo.getHome();
    if (home == null) {
      FeedBack.of(context, FeedBack.NO_HOME, FeedBack.NO_HOME);
      return;
    }
    if (infoLevel == null) {
      sendError("infoLevelNull");
      return;
    }
    new PetTeleportPack(
      server,
      infoLevel.dimension(),
      infoChunkPos,
      playerInfo.getHome().dimension(),
      Vec3.atLowerCornerOf(playerInfo.getHome().pos()),
      petUUID,
      this::goHomeGlowAndReply,
      goHomeFail
    ).teleportEnhanced();
  }

  void recall() {
    if (infoLevel == null) {
      sendError("infoLevelNull");
      return;
    }
    new PetTeleportPack(
      server,
      infoLevel.dimension(),
      infoChunkPos,
      playerLevel.dimension(),
      player.position(),
      petUUID,
      this::recallGlowAndReply,
      recallFail
    ).teleportEnhanced();
  }

  void switchCanceled() {
    info.switchCanceled();
    PetOthers.SERVER.add(0.25f, 0, unused -> {
      PetInfo info = PetData.getInstance().getPetInfo(petUUID);
      if (info == null) return;
      context.reply(new PMPayload(PetNetwork.Action.NEW_PET_INFO, info.packAsNBT()));
      FeedBack.of(context,FeedBack.CANCELED_SUCCESSFUL);
    });
  }
}
