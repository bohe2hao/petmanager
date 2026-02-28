package com.github.bohe2hao.petmanager.client.gui.widgets;

import static com.github.bohe2hao.petmanager.others.GuiTool.*;
import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.*;

import com.github.bohe2hao.petmanager.Config;
import com.github.bohe2hao.petmanager.client.gui.other.RePosAble;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class RenameBox extends EditBox implements RePosAble {

  //名称输入框
  public Point relativePosition;

  public RenameBox(int x, int y) {
    super(getFont(), getCenterX() + x, getCenterY() + y, 100, 15, Component.literal("reName"));
    relativePosition = new Point(x, y);
    this.setBordered(false);
    this.setTextColor(Config.getPreferredColor());
  }

  @Override
  public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    PoseStack pose = guiGraphics.pose();
    pose.pushPose();
    pose.translate(getX(), getY(), 0);
    guiGraphics.fill(0, 0, width, height, COLOR_BLACK);
    guiGraphics.renderOutline(0, 0, width, height, Color.black.getRGB());
    guiGraphics.renderItem(NAME_TAG, -23, 0);
    pose.popPose();
    hoveredEffect(this, guiGraphics);
  }

  @Override
  public void repos() {
    RePosAble.repos(this, relativePosition);
  }
}
