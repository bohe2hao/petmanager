package com.github.bohe2hao.petmanager.network;

import static com.github.bohe2hao.petmanager.client.gui.PetWidget.STATUS_BAR;

import com.github.bohe2hao.petmanager.PetManager;
import com.github.bohe2hao.petmanager.others.PetOthers;
import com.github.bohe2hao.petmanager.others.RenderBlock;
import com.github.bohe2hao.petmanager.client.ClientPetData;
import com.github.bohe2hao.petmanager.data.PetData;
import com.github.bohe2hao.petmanager.data.PetInfo;
import com.github.bohe2hao.petmanager.data.PlayerInfo;
import io.netty.buffer.ByteBuf;
import java.awt.*;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

//信息
record PMPayload(@Nonnull PetNetwork.Action action, CompoundTag nbt) implements CustomPacketPayload {
    public static final Type<PMPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PetManager.MODID, "pm_payload")
    );
    @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public String getAction() {
    return this.action.name();
  }

  public static PMPayload rebuild(String action, CompoundTag info) {
    return new PMPayload(PetNetwork.Action.valueOf(action), info);
  }


  public static final StreamCodec<ByteBuf, PMPayload> CODEC = StreamCodec.composite(
          ByteBufCodecs.STRING_UTF8,
          PMPayload::getAction,
          ByteBufCodecs.COMPOUND_TAG,
          PMPayload::nbt,
          PMPayload::rebuild
  );

  PMPayload(PetNetwork.Action action) {
    this(action, new CompoundTag());
  }

  static PMPayload buildFeedback(MutableComponent component, HolderLookup.Provider provider) {
    CompoundTag nbt = new CompoundTag();
    nbt.putString(PetNetwork.Action.FEEDBACK.name(), Component.Serializer.toJson(component, provider));
    return new PMPayload(PetNetwork.Action.FEEDBACK, nbt);
  }

  public static void hand(PMPayload pack, IPayloadContext context) {
    if (context.flow().isClientbound()) {
      clientWork(pack, context);
    } else {
      serverWork(pack, context);
    }
  }

    private static void clientWork(PMPayload pack, IPayloadContext context) {
    context.enqueueWork(() -> {
      if (pack.action() == PetNetwork.Action.FEEDBACK) {
        ClientPetData.getInstance().messageArrive(
          Objects.requireNonNull(
            Component.Serializer.fromJson(
              pack.nbt().getString(PetNetwork.Action.FEEDBACK.name()),
              context.player().registryAccess()
            )
          )
        );
      }

      if (pack.action() == PetNetwork.Action.HIGHLIGHT_BLOCK) {
        CompoundTag result = pack.nbt();
        BlockPos pos = new BlockPos(result.getInt("x"), result.getInt("y"), result.getInt("z"));
        boolean success = result.getBoolean("result");
        Color color = success ? new Color(0f, 1f, 0f, 0.8f) : new Color(1f, 0f, 0f, 0.8f);
        PetOthers.RENDER.add(0, 1, 5, 0.25f, event -> {
          RenderBlock.solid(
            event,
            pos,
            Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, true),
            color
          );
          RenderBlock.line(event, pos, color);
        });
      }

      if (pack.action() == PetNetwork.Action.PLAYER_INFO) {
        ClientPetData.getInstance().playerInfoArrived(pack.nbt());
      }

      if (pack.action() == PetNetwork.Action.NEW_PET_INFO) {
        ClientPetData.getInstance().petInfosArrived(pack.nbt());
      }

      if (pack.action() == PetNetwork.Action.IN_CD) {
        STATUS_BAR.inCd();
      }
    });
  }

  private static void serverWork(PMPayload pack, IPayloadContext context) {
    context.enqueueWork(() -> {
      if (pack.action() == PetNetwork.Action.OPEN_PET_SCREEN) {
        PetData database = PetData.getInstance();
        Set<PetInfo> found = database.getPetInfosByPlayer(context.player());
        found.forEach(i -> context.reply(new PMPayload(PetNetwork.Action.NEW_PET_INFO, i.packAsNBT())));
        context.reply(new PMPayload(PetNetwork.Action.PLAYER_INFO, database.getPlayerInfo(context.player()).packAsNbt()));
      }

      if (pack.action() == PetNetwork.Action.SET_HOME) {
        CompoundTag nbt = new CompoundTag();
        Pair<GlobalPos, Boolean> result = PetData.getInstance()
          .getPlayerInfo(context.player())
          .setHome(context.player());
        BlockPos pos = result.getA().pos();
        MutableComponent feedback = result.getB() ? FeedBack.SET_HOME_SUCCESS : FeedBack.SET_HOME_FAIL;
        FeedBack.of(context, feedback);
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        nbt.putBoolean("result", result.getB());
        context.reply(new PMPayload(PetNetwork.Action.HIGHLIGHT_BLOCK, nbt));
      }

      if (pack.action == PetNetwork.Action.SWITCH_FRIENDLY_FIRE) {
        PlayerInfo info = PetData.getInstance().getPlayerInfo(context.player());
        info.switchFriendFire();
        context.reply(new PMPayload(PetNetwork.Action.PLAYER_INFO, info.packAsNbt()));
      }
    });
  }
}
