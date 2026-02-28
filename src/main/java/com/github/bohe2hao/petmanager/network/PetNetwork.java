package com.github.bohe2hao.petmanager.network;

import com.github.bohe2hao.petmanager.PetManager;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PetManager.MODID)
public class PetNetwork {

  public static final String VERSION = "1.0.0";

  @SubscribeEvent
  public static void register(final RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar register = event.registrar(VERSION);
    register.playBidirectional(PetPayload.TYPE, PetPayload.CODEC, PetPayload::hand);
    register.playBidirectional(PMPayload.TYPE, PMPayload.CODEC, PMPayload::hand);
  }

  //---
  public static void onOpenPetScreen() {
    PacketDistributor.sendToServer(new PMPayload(Action.OPEN_PET_SCREEN));
  }

  public static void renamePet(UUID pet, String newName) {
    CompoundTag nbt = new CompoundTag();
    nbt.putString(PetAbout.RENAME.name(), newName);
    PacketDistributor.sendToServer(new PetPayload(PetAbout.RENAME, pet, nbt));
  }

  public static void goHome(UUID pet) {
    PacketDistributor.sendToServer(new PetPayload(PetAbout.GO_HOME, pet, null));
  }

  public static void setHome() {
    PacketDistributor.sendToServer(new PMPayload(Action.SET_HOME));
  }

  public static void recallPet(UUID pet) {
    PacketDistributor.sendToServer(new PetPayload(PetAbout.RECALL, pet, null));
  }

  public static void switchCanceled(UUID pet) {
    PacketDistributor.sendToServer(new PetPayload(PetAbout.SWITCH_CANCELED, pet, null));
  }

  public static void switchFriendlyFire() {
    PacketDistributor.sendToServer(new PMPayload(Action.SWITCH_FRIENDLY_FIRE));
  }

  //---

  public enum PetAbout {
    RECALL,
    RENAME,
    GO_HOME,
    SWITCH_CANCELED,
    //来自服务器
    RECALL_SUCCESS,
    ENTITY_SYNC
  }

  public enum Action {
    //来自服务端
    HIGHLIGHT_BLOCK,
    PLAYER_INFO,
    IN_CD,
    CLIENT_REBUILD,
    //来自客户端
    FEEDBACK,
    SET_HOME,
    OPEN_PET_SCREEN,
    SWITCH_FRIENDLY_FIRE,
    NEW_PET_INFO
  }
}
