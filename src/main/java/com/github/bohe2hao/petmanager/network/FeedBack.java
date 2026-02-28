package com.github.bohe2hao.petmanager.network;

import java.awt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.network.handling.IPayloadContext;

class FeedBack {

  public static final MutableComponent ACTION_IS_COOLING_DOWN = Component.translatable(
    "petmanager.feedback.ActionIsCoolingDown"
  ).withColor(Color.orange.getRGB());
  public static final MutableComponent TELEPORT_FAIL = Component.translatable(
    "petmanager.feedback.teleportFail"
  ).withColor(Color.red.getRGB());
  public static final MutableComponent TELEPORT_SUCCESS = Component.translatable(
    "petmanager.feedback.teleportSuccess"
  ).withColor(Color.GREEN.getRGB());

  public static final MutableComponent NO_EXP = Component.translatable("petmanager.info.noEnoughExp").withStyle(
    Style.EMPTY.withColor(Color.red.getRGB())
  );
  public static final MutableComponent PET_OFF_LINE = Component.translatable("petmanager.info.petOffLine").withStyle(
    Style.EMPTY.withColor(Color.red.getRGB())
  );
  public static final MutableComponent NULL_NAME = Component.translatable("petmanager.info.nullName").withStyle(
    Style.EMPTY.withColor(Color.red.getRGB())
  );
  public static final MutableComponent RENAME_SUCCESS = Component.translatable(
    "petmanager.screen.state.renameSuccess"
  ).withStyle(Style.EMPTY.withColor(Color.GREEN.getRGB()));

  public static final MutableComponent NO_HOME = Component.translatable("petmanager.info.noHome").withStyle(
    Style.EMPTY.withColor(Color.red.getRGB())
  );

  public static final MutableComponent SET_HOME_SUCCESS = Component.translatable(
    "petmanager.info.setHomeSuccess"
  ).withStyle(Style.EMPTY.withColor(Color.GREEN.getRGB()));

  public static final MutableComponent SET_HOME_FAIL = Component.translatable("petmanager.info.setHomeFail").withStyle(
    Style.EMPTY.withColor(Color.red.getRGB())
  );

  public static final MutableComponent GO_HOME_SUCCESSFUL=Component.translatable("petmanager.info.goHomeSuccessful").withStyle(Style.EMPTY.withColor(Color.GREEN.getRGB()));

  public static final MutableComponent GO_HOME_FAILED=Component.translatable("petmanager.info.goHomeFailed").withStyle(Style.EMPTY.withColor(Color.red.getRGB()));

  public static final MutableComponent CANCELED_SUCCESSFUL =Component.translatable("petmanager.info.canceldSuccessful").withStyle(Style.EMPTY.withColor(Color.GREEN.getRGB()));

  public static void of(IPayloadContext context, MutableComponent component) {
    context.reply(PMPayload.buildFeedback(component, context.player().registryAccess()));
  }

  public static void of(IPayloadContext context, MutableComponent component, MutableComponent senToPlayer) {
    context.reply(PMPayload.buildFeedback(component, context.player().registryAccess()));
    context.player().sendSystemMessage(senToPlayer);
  }
}
