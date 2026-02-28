package com.github.bohe2hao.petmanager.client.gui.widgets;

import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.*;

import com.github.bohe2hao.petmanager.Config;
import com.github.bohe2hao.petmanager.client.ClientPetData;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class StatusBar extends BaseWidget {

  int color = new Color(0.1f, 0.1f, 0.1f, 0.6f).getRGB();
  double cdWidth = 0;
  Timer timer = new Timer(
    16,
    new ActionListener() {
      boolean started = false;

      public void actionPerformed(ActionEvent e) {
        if (!started) {
          started = true;
          cdWidth = width;
        }
        cdWidth -= ((double) width) / ((Config.PLAYER_OPERATES_COOLDOWN_TIME.get() * 1000) / 16);
        if (cdWidth <= 0) {
          timer.stop();
          cdWidth = 0;
          started = false;
        }
      }
    }
  );

  public StatusBar(Rectangle area) {
    super(area.x, area.y, area.width, area.height, Component.literal("stateBar"));
  }

  public void inCd() {
    timer.start();
  }

  @Override
  protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
    guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, color);
    guiGraphics.fill(getX(), getY(), (int) (getX() + cdWidth), getY() + height, Config.getPreferredColor());
    guiGraphics.drawString(
      getFont(),
      ClientPetData.getInstance().getMessage(),
      getX() + 4,
      getY() + 4,
      Color.LIGHT_GRAY.getRGB()
    );
    guiGraphics.renderOutline(getX(), getY(), width, height, Color.black.getRGB());
  }

  @Override
  protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
}
