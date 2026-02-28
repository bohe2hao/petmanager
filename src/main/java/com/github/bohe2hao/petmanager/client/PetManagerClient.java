package com.github.bohe2hao.petmanager.client;

import com.github.bohe2hao.petmanager.PetManager;
import com.github.bohe2hao.petmanager.others.PetOthers;
import com.github.bohe2hao.petmanager.client.gui.PetScreen;
import com.github.bohe2hao.petmanager.data.PetInfo;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@Mod(value = PetManager.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PetManager.MODID, value = Dist.CLIENT)
public class PetManagerClient {

  public PetManagerClient(ModContainer container) {
    container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
  }

  public static final KeyMapping OPEN_SCREEN_KEY = new KeyMapping(
    "petmanager.keyboard.openScreen",
    KeyConflictContext.IN_GAME,
    InputConstants.Type.KEYSYM,
    GLFW.GLFW_KEY_R,
    "key.categories.petmanager"
  );

  @SubscribeEvent
  public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
    event.register(OPEN_SCREEN_KEY);
  }

  public static void switchPetScreen() {
    Minecraft.getInstance().setScreen(new PetScreen());
  }

  @SubscribeEvent
  public static void onClientTick(ClientTickEvent.Pre event) {
    PetOthers.CLIENT.tick(null);
    if (OPEN_SCREEN_KEY.consumeClick()) {
      switchPetScreen();
    }
  }

  @SubscribeEvent
  public static void onRender(RenderLevelStageEvent event) {
    PetOthers.RENDER.tick(event);
  }

  public static PetInfo clientBuildPetInfo(CompoundTag nbt) {
    PetInfo info = PetInfo.rebuild(nbt);
    ClientLevel level = Minecraft.getInstance().level;
    if (level != null) {
      info.clientEntity = EntityType.create(info.entityData, level).orElse(null);
    }
    if (info.clientEntity != null) {
      info.clientEntity.setYHeadRot(-135);
      info.clientEntity.setYBodyRot(-135);
    }
    return info;
  }
}
