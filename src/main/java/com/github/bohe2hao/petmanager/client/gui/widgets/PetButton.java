package com.github.bohe2hao.petmanager.client.gui.widgets;

import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.*;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.RENAME_BOX;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.effect;

import com.github.bohe2hao.petmanager.Config;
import com.github.bohe2hao.petmanager.others.GuiTool;
import com.github.bohe2hao.petmanager.client.ClientPetData;
import com.github.bohe2hao.petmanager.client.gui.WidgetsManager;
import com.github.bohe2hao.petmanager.data.PetInfo;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class PetButton extends BaseWidget {

  public final int index;

  public PetButton(int index, int offset) {
    super(
      PET_BUTTON_X,
      (PET_BUTTON_HEIGHT + BOTTON_INTERVAL) * index - 83 - offset * (PET_BUTTON_HEIGHT + BOTTON_INTERVAL),
      PET_BUTTON_WIDTH,
      PET_BUTTON_HEIGHT,
      Component.empty()
    );
    this.index = index;
  }

  @Override
  protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
    PetInfo info = ClientPetData.getInstance().getInstance(index);
    boolean focused = ClientPetData.getInstance().getFocusPet() == info;
    //渲染背景
    super.renderWidget(guiGraphics, i, i1, v);
    Entity entity = info.clientEntity;
    //选中效果
    if (focused) {
      GuiTool.render(guiGraphics, BUTTON_DARKER, new Point(getX(), getY()), new Point(width, height));
      guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, Config.getPreferredColor());
    }
    //渲染模型框
    Point modelXY = new Point(getX() + 2, getY() + 2);
    GuiTool.render(guiGraphics, BUTTON_OPPOSITE, new Point(modelXY.x, modelXY.y), new Point(16, 16));
    //渲染模型
    {
      PoseStack poseStack = guiGraphics.pose();
      EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      poseStack.pushPose();
      poseStack.translate(getX() + PET_BUTTON_WIDTH / 8.5, getY() + PET_BUTTON_HEIGHT - 5, 100.0); // Z轴偏移确保实体在GUI上层

      float scale = GuiTool.getEntityScale(new Vec3(12, 12, 12), entity);

      poseStack.scale(scale, scale, scale); // 缩放比例，调整实体大小

      //设置实体旋转角度（使其在GUI中旋转）
      Quaternionf xQuaternion = Axis.XP.rotationDegrees(180.0F);
      Quaternionf yQuaternion = Axis.YP.rotationDegrees(180.0F);
      poseStack.mulPose(xQuaternion);
      poseStack.mulPose(yQuaternion);

      // 禁用光照（GUI渲染通常不需要光照）
      Lighting.setupForEntityInInventory();
      dispatcher.render(entity, 0, 0, 0, 0, 0.3f, poseStack, guiGraphics.bufferSource(), LightTexture.FULL_BRIGHT);
      poseStack.popPose();
    }
    //名称
    {
      guiGraphics.drawString(
        getFont(),
        info.getDisplayName(),
        (int) (getX() + PET_BUTTON_WIDTH / 2.5f),
        getY() + PET_BUTTON_HEIGHT / 3,
        COLOR_BLACK,
        false
      );
    }
    //离线效果
    if (info.getInfoState() == PetInfo.State.offLine) {
      guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, new Color(0.1f, 0.1f, 0.1f, 0.5f).getRGB());
    }

    //可骑乘图标
    if (entity instanceof PlayerRideable) {
      guiGraphics.renderItem(SADDLE, getX() + 18, getY());
    }

    //血量
    {
    }
  }

  @Override
  public void onClick(double mouseX, double mouseY, int button) {
    WidgetsManager.applyActionConfig(WidgetsManager.Action.PRESS_PET_BUTTON);
    ClientPetData.getInstance().setFocus(index);
    RENAME_BOX.setSuggestion(ClientPetData.getInstance().getFocusPet().getDisplayName().getString());
    effect();
  }

  @Override
  public void onRelease(double mouseX, double mouseY) {}

  @Override
  protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
}
