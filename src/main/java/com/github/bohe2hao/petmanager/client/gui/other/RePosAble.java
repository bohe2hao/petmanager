package com.github.bohe2hao.petmanager.client.gui.other;

import com.github.bohe2hao.petmanager.others.GuiTool;
import java.awt.*;
import net.minecraft.client.gui.components.AbstractWidget;

public interface RePosAble {
  void repos();

  static void repos(AbstractWidget widget, Point relativePos) {
    widget.setX(GuiTool.getCenterX() + relativePos.x);
    widget.setY(GuiTool.getCenterY() + relativePos.y);
  }
}
