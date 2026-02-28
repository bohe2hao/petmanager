package com.github.bohe2hao.petmanager.client.gui.widgets;

import com.github.bohe2hao.petmanager.others.GuiTool;
import com.github.bohe2hao.petmanager.client.gui.GuiConfig;
import com.github.bohe2hao.petmanager.client.gui.other.RePosAble;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class BaseWidget extends AbstractWidget implements RePosAble {

  private final Point relativePos;


  private GuiTool.RenderData background = GuiConfig.BUTTON;
  Timer timer = new Timer(
    16,
    new ActionListener() {
      Rectangle base = null;
      int accumulation = 0;
      boolean begin = false;

      @Override
      public void actionPerformed(ActionEvent e) {
        if (!begin) {
          base = new Rectangle(getX(), getY(), width, height);
          begin = true;
        }
        Rectangle newRect = new Rectangle(getX(), getY(), width, height);
        newRect.grow(2, 1);
        setX(newRect.x);
        setY(newRect.y);
        setWidth(newRect.width);
        setHeight(newRect.height);
        accumulation += 1;
        if (accumulation == 5) {
          begin = false;
          timer.stop();
          accumulation = 0;
          setX(base.x);
          setY(base.y);
          setWidth(base.width);
          setHeight(base.height);
        }
      }
    }
  );

  @Override
  public void onClick(double mouseX, double mouseY, int button) {
    super.onClick(mouseX, mouseY, button);
    background = GuiConfig.BUTTON_DARKER;
    timer.start();
  }

  @Override
  public void onRelease(double mouseX, double mouseY) {
    super.onRelease(mouseX, mouseY);
    background = GuiConfig.BUTTON;
  }



  @Override
  protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
    GuiTool.render(guiGraphics, background, new Point(getX(), getY()), new Point(getWidth(), getHeight()));
    GuiTool.hoveredEffect(this, guiGraphics);
  }

  @Override
  public void repos() {
    setX(GuiTool.getCenterX() + relativePos.x);
    setY(GuiTool.getCenterY() + relativePos.y);
  }

  public BaseWidget(int x, int y, int width, int height, Component message) {
    super(GuiTool.getCenterX() + x, GuiTool.getCenterY() + y, width, height, message);
    relativePos = new Point(x, y);
  }
}
