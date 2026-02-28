package com.github.bohe2hao.petmanager.client;

import com.github.bohe2hao.petmanager.PetManager;
import com.github.bohe2hao.petmanager.others.PetOthers;
import com.github.bohe2hao.petmanager.event.ModEvent;
import com.github.bohe2hao.petmanager.data.PetInfo;
import com.github.bohe2hao.petmanager.data.PlayerInfo;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = PetManager.MODID, dist = Dist.CLIENT)
public class ClientPetData {

  private static ClientPetData self;

  private PetInfo focusPet;
  public boolean focusOnCanceledPet = false;
  private final List<PetInfo> petInfoLib = new ArrayList<>();
  private PlayerInfo playerInfo;
  private MutableComponent stateMessage;

  public static ClientPetData getInstance() {
    if (self == null) self = new ClientPetData();
    return self;
  }

  void sortInfos() {
    petInfoLib.sort(
      Comparator.comparing(PetInfo::isCanceled)
        .thenComparing(PetInfo::getInfoState)
        .thenComparing(i -> i.getDisplayName().getString())
    );
  }

  public PlayerInfo getPlayerInfo() {
    return playerInfo;
  }

  public void playerInfoArrived(CompoundTag nbt) {
    playerInfo = PlayerInfo.rebuild(nbt);
    ModEvent.publish(ModEvent.Event.PLAYER_INFO_ARRIVED);
  }

  public void petInfosArrived(CompoundTag nbt) {
    PetInfo petInfo = PetManagerClient.clientBuildPetInfo(nbt);
    petInfoLib.removeIf(p -> petInfo.getUUID().equals(p.getUUID()));
    petInfoLib.add(petInfo);
    sortInfos();
    ModEvent.publish(ModEvent.Event.PET_INFO_ARRIVED);
  }

  public void messageArrive(MutableComponent message) {
    stateMessage = message;
    PetOthers.CLIENT.add(1, 0, unused ->
      stateMessage = stateMessage.copy().withStyle(Style.EMPTY.withBold(false).withColor(Color.gray.getRGB()))
    );
  }

  public Component getMessage() {
    return stateMessage;
  }

  public int getPetInfosLength() {
    return petInfoLib.size();
  }

  public void nullFocus() {
    focusPet = null;
  }

  public PetInfo getInstance(int i) {
    return petInfoLib.get(i);
  }

  public void setFocus(int i) {
    focusPet = petInfoLib.get(i);
  }

  public PetInfo getFocusPet() {
    return focusPet;
  }

    public void clear() {
      petInfoLib.clear();
    }
}
