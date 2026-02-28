package com.github.bohe2hao.petmanager.client.gui;

import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.*;
import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.Area.BACKGROUND;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.*;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.CANCEL_PET;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.GO_HOME;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.OPEN_SETTING;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.PET_OR_CANCELED_PET;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.RECALL_BUTTON;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.RENAME_SURE_BUTTON;
import static com.github.bohe2hao.petmanager.client.gui.PetWidget.STATUS_BAR;
import static com.github.bohe2hao.petmanager.client.gui.WidgetsManager.WidgetGroup.*;

import com.github.bohe2hao.petmanager.others.GuiTool;
import com.github.bohe2hao.petmanager.client.ClientPetData;
import com.github.bohe2hao.petmanager.client.PetManagerClient;
import com.github.bohe2hao.petmanager.client.gui.widgets.PetButton;
import com.github.bohe2hao.petmanager.event.ModEvent;
import com.github.bohe2hao.petmanager.network.PetNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PetScreen extends Screen {

  private double ScrollingOffsetValue = 0;
  //数据
  final WidgetsManager SCREEN_DATA = new WidgetsManager();

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (super.keyPressed(keyCode, scanCode, modifiers)) {
      return true;
    } else {
      if (
        (keyCode == PetManagerClient.OPEN_SCREEN_KEY.getKey().getValue() ||
          keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) &&
        !RENAME_BOX.isFocused()
      ) {
        onClose();
        return true;
      }
      return false;
    }
  }

  //重新排序
  public void rebuildPetButtons() {
    SCREEN_DATA.clearPetButtons();
    int count = ClientPetData.getInstance().getPetInfosLength();
    Set<PetButton> needRemove = renderables
      .stream()
      .filter(r -> r instanceof PetButton)
      .map(r -> (PetButton) r)
      .collect(Collectors.toSet());
    needRemove.forEach(this::removeWidget);
    int offset = 0;
    for (int i = 0; i < count; i++) {
      boolean canceled = ClientPetData.getInstance().getInstance(i).isCanceled();
      boolean focusCancel = ClientPetData.getInstance().focusOnCanceledPet;
      if (canceled == focusCancel) {
        PetButton petButton = new PetButton(i, offset);
        addRenderableWidgets(petButton);
        PET_BUTTONS.add(petButton);
        SCREEN_DATA.addReposAble(petButton);
        continue;
      }
      offset += 1;
    }
    ScrollingOffsetValue = 0;
  }

  @Override
  public void resize(@NotNull Minecraft minecraft, int width, int height) {
    super.resize(minecraft, width, height);
    init();
  }

  @Override
  protected void init() {
    GuiConfig.update();
    rebuildPetButtons();
    addRenderableWidgets(
      RECALL_BUTTON,
      RENAME_SURE_BUTTON,
      OPEN_SETTING,
      PET_OR_CANCELED_PET,
      GO_HOME,
      CANCEL_PET,
      RENAME_BOX,
      STATUS_BAR,
      SET_HOME,
      FRIEND_FIRE
    );
    PetNetwork.onOpenPetScreen();
    ClientPetData.getInstance().focusOnCanceledPet = false;
    SCREEN_DATA.rePos();
    PetWidget.effect();
    ClientPetData.getInstance().messageArrive(Component.translatable("petmanager.screen.state.openScreen"));
    RENAME_BOX.setValue("");
  }

  void subscribe(boolean enable) {
    Runnable run = enable
      ? () -> {
          ModEvent.subscribe(ModEvent.Event.PET_INFO_ARRIVED, this::rebuildPetButtons);
          ModEvent.subscribe(ModEvent.Event.SWITCH_UNCANCELED_PET, this::rebuildPetButtons);
        }
      : () -> {
          ModEvent.unsubscribe(ModEvent.Event.PET_INFO_ARRIVED, this::rebuildPetButtons);
          ModEvent.unsubscribe(ModEvent.Event.SWITCH_UNCANCELED_PET, this::rebuildPetButtons);
        };
    run.run();
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  public PetScreen() {
    super(Component.translatable("petmanager.gui.myPet"));
    init();
    subscribe(true);
  }

  @Override
  public void onClose() {
    super.onClose();
    subscribe(false);
    ClientPetData.getInstance().nullFocus() ;
    ClientPetData.getInstance().clear();
  }

  public void addRenderableWidgets(AbstractWidget... widget) {
    for (AbstractWidget w : widget) {
      addWidget(w);
      addRenderableOnly(w);
    }
  }

  @Override
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    int centerX = GuiTool.getCenterX();
    int centerY = GuiTool.getCenterY();

    PoseStack pose = guiGraphics.pose();
    for (Renderable r : renderables) {
      if (r instanceof PetButton) {
        Rectangle rolling = new Rectangle(Area.ROLLING);
        rolling.translate(centerX, centerY);
        guiGraphics.enableScissor(rolling.x, rolling.y, (int) rolling.getMaxX(), (int) rolling.getMaxY() + 1);
        r.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();
      } else {
        r.render(guiGraphics, mouseX, mouseY, partialTick);
      }
    }

    if (ClientPetData.getInstance().getFocusPet() == null) {
      int x = centerX + 15;
      int y = centerY - 10;
      Component context = Component.translatable("petmanager.screen.noSelected").withStyle(Style.EMPTY.withBold(true));
      Component sign = Component.literal("?").withStyle(Style.EMPTY.withBold(true));
      int color = Color.DARK_GRAY.getRGB();
      float fontScale = 0.6f;
      float signScale = 1.8f;

      guiGraphics.renderOutline(x, y, 20, 20, color);

      pose.pushPose();
      pose.scale(fontScale, fontScale, fontScale);
      guiGraphics.drawString(font, context, (int) ((x + 22) / fontScale), (int) ((y + 8) / fontScale), color, false);
      pose.popPose();

      pose.pushPose();
      pose.scale(signScale, signScale, signScale);
      guiGraphics.drawString(font, sign, (int) ((x + 5) / signScale), (int) ((y + 5) / signScale), color, false);
      pose.popPose();
    } else {
      INFO_WINDOW.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    if (ClientPetData.getInstance().focusOnCanceledPet) {
      Rectangle area = new Rectangle(BACKGROUND);
      area.translate(centerX, centerY);
      guiGraphics.fill(
        area.x,
        area.y,
        (int) area.getMaxX(),
        (int) area.getMaxY(),
        new Color(1f, 0f, 0f, 0.2f).getRGB()
      );
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    double offset = scrollY * 12;
    // 预计算边界值
    int scrollLimit =
      (PET_BUTTON_HEIGHT + BOTTON_INTERVAL) * PET_BUTTONS.group.size() + 30 - PET_BUTTON_LIST_CLIP_HEIGHT;
    double newOffsetValue = ScrollingOffsetValue + offset;

    boolean arrivedEdge = newOffsetValue > 0 || newOffsetValue < -scrollLimit;
    Rectangle area = new Rectangle(Area.ROLLING);
    area.translate(GuiTool.getCenterX(), GuiTool.getCenterY());
    boolean notInArea = !area.contains((int) mouseX, (int) mouseY);
    // 边界检查
    if (arrivedEdge || notInArea) {
      return false;
    } else {
      // 更新按钮位置
      for (AbstractWidget button : PET_BUTTONS.group) {
        button.setY((int) (button.getY() + offset));
      }
      ScrollingOffsetValue = newOffsetValue;
      return true;
    }
  }

  @Override
  public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    Rectangle area = new Rectangle(BACKGROUND);
    area.translate(GuiTool.getCenterX(), GuiTool.getCenterY());
    GuiTool.render(guiGraphics, PET_SCREEN_BACKGROUND, area);
  }
}
