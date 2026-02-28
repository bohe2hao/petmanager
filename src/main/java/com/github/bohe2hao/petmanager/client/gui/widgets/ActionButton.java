package com.github.bohe2hao.petmanager.client.gui.widgets;

import com.github.bohe2hao.petmanager.others.GuiTool;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ActionButton extends BaseWidget {
  protected Component words;
  protected ItemStack icon;

  public ActionButton(Rectangle area, ItemStack icon) {
    this(area, icon, Component.empty());
  }

  public ActionButton(Rectangle area, ItemStack icon, Component displayText) {
    super(area.x, area.y, area.width, area.height, displayText);
    this.words = displayText;
    this.icon = icon;
  }

  @Override
  protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
    super.renderWidget(guiGraphics, i, i1, v);
    //icon
    int offset = height / 2 - 8;
    guiGraphics.renderItem(icon, getX() + offset, getY() + offset);
    //info
    PoseStack pose = guiGraphics.pose();
    pose.pushPose();
    pose.translate(0, 0, 200);
    GuiTool.drawCenteredScaledText(
      guiGraphics,
      words,
      getX() + 20,
      getY(),
      width - 30,
      height,
      new Color(0.2f, 0.2f, 0.2f, 0.5f).getRGB(),
      false
    );
    pose.popPose();
  }

  @Override
  protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
}
