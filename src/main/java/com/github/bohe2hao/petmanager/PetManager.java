package com.github.bohe2hao.petmanager;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(PetManager.MODID)
public class PetManager {

  public static final String MODID = "petmanager";
  public static final Logger PM_LOGGER = LogUtils.getLogger();

  public PetManager(IEventBus modEventBus, ModContainer modContainer) {
    modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

    modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC_CLIENT);
  }
}
