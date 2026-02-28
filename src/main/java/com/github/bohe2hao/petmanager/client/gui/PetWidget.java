package com.github.bohe2hao.petmanager.client.gui;

import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.*;
import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.Area.FRIENDLY_FIRE;
import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.Area.MODEL;

import com.github.bohe2hao.petmanager.others.GuiTool;
import com.github.bohe2hao.petmanager.client.ClientPetData;
import com.github.bohe2hao.petmanager.client.PetGui;
import com.github.bohe2hao.petmanager.client.gui.widgets.ActionButton;
import com.github.bohe2hao.petmanager.client.gui.widgets.RenameBox;
import com.github.bohe2hao.petmanager.client.gui.widgets.StatusBar;
import com.github.bohe2hao.petmanager.event.ModEvent;
import com.github.bohe2hao.petmanager.network.PetNetwork;
import com.github.bohe2hao.petmanager.data.PetInfo;
import com.github.bohe2hao.petmanager.data.PlayerInfo;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class PetWidget {

  //widgets
  static final ActionButton SET_HOME = new ActionButton(
    Area.SET_HOME,
    new ItemStack(Items.RED_BED),
    Component.translatable("petmanager.screen.setting.setHome")
  ) {
    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      PetNetwork.setHome();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
      super.renderWidget(guiGraphics, i, i1, v);
      PlayerInfo playerInfo = ClientPetData.getInstance().getPlayerInfo();
      if (playerInfo != null) {
        if (new Rectangle(getX(), getY(), getWidth(), getHeight()).contains(i, i1)) {
          java.util.List<Component> info =
            playerInfo.getHome() == null
              ? java.util.List.of(Component.translatable("petmanager.info.noHome"))
              : List.of(
                  Component.literal(playerInfo.getHome().dimension().location().toString()),
                  Component.literal(playerInfo.getHome().pos().toString())
                );
          guiGraphics.renderComponentTooltip(getFont(), info, i, i1);
        }
      }
    }
  };

  public static float moreModelSize;

  public static void effect() {
    moreModelSize = 0.2f;
  }

  static final AbstractWidget INFO_WINDOW = new AbstractWidget(0, 0, 0, 0, Component.empty()) {
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float v) {
      PoseStack pose = guiGraphics.pose();
        PetInfo info=ClientPetData.getInstance().getFocusPet();
      LivingEntity entity = (LivingEntity)info.clientEntity;
      int centerX = GuiTool.getCenterX();
      int centerY = GuiTool.getCenterY();
      Rectangle model = new Rectangle(MODEL);
      model.translate(centerX, centerY);
      if (model.contains(mouseX, mouseY)) {
        guiGraphics.renderComponentTooltip(getFont(), PetGui.getEntityTooltip(entity), mouseX, mouseY);
      }
      //渲染信息
      float health = entity.getHealth();
      float maxHealth = entity.getMaxHealth();
      int currentHealthColor = health == maxHealth ? Color.green.getRGB() : Color.orange.getRGB();
      guiGraphics.drawString(
        getFont(),
        String.valueOf(entity.getHealth()),
        centerX + 50,
        centerY - 40,
        currentHealthColor
      );
      GuiTool.render(guiGraphics, HEART, new Point(centerX + 38, centerY - 41));

      //维度
      pose.pushPose();
      pose.translate(centerX + 50,centerY-20,100);
      pose.scale(0.65f,0.65f,0.65f);
        String dimension=info.dimension.toUpperCase().replace("MINECRAFT:","");
        GuiTool.drawCenteredScaledText(guiGraphics,Component.literal(dimension),0,-10,80,20,Color.ORANGE.getRGB(),true);
        guiGraphics.renderItem(new ItemStack(Items.GRASS_BLOCK),-20,-10);
        pose.popPose();

        //更新时间
        pose.pushPose();
        pose.translate(centerX+50,centerY-30,100);
        pose.scale(0.65f,0.65f,0.65f);
        String lastUpdate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(info.lastUpdate));
        GuiTool.drawCenteredScaledText(guiGraphics,Component.literal(lastUpdate),0,30,120,50,Color.ORANGE.getRGB(),true);
        guiGraphics.renderItem(new ItemStack(Items.CLOCK),-20,45);
        pose.popPose();

        //位置
      String posInfo = (int) entity.position().x + " " + (int) entity.position().y + " " + (int) entity.position().z;
      pose.pushPose();
      pose.translate(centerX + 50, centerY - 10, 100);
      pose.scale(0.65f, 0.65f, 0.65f);
      guiGraphics.renderItem(new ItemStack(Items.COMPASS), -20, -5);
      guiGraphics.drawString(
        getFont(),
        Component.literal(posInfo).withStyle(Style.EMPTY.withBold(true)),
        0,
        0,
        Color.ORANGE.getRGB()
      );
      pose.popPose();
      //渲染实体
      pose.pushPose();
      pose.translate(centerX + 10, centerY + 20, -100);
      float scale = GuiTool.getEntityScale(new Vec3(40, 60, 1000), entity) * (1f + moreModelSize);
      pose.scale(scale, scale, scale);
      if (moreModelSize > 0) moreModelSize -= 0.01f;
      Quaternionf rotateX = Axis.XP.rotationDegrees(180);
      Quaternionf rotateY = Axis.YP.rotationDegrees(180);
      pose.mulPose(rotateX);
      pose.mulPose(rotateY);
      Lighting.setupForEntityInInventory();
      entityRenderDispatcher.render(
        entity,
        0,
        0,
        0,
        0,
        0.1f,
        pose,
        guiGraphics.bufferSource(),
        LightTexture.FULL_BRIGHT
      );
      pose.popPose();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
  };

  static final ActionButton OPEN_SETTING = new ActionButton(Area.OPEN_SETTING, new ItemStack(Items.ANVIL)) {
    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      WidgetsManager.applyActionConfig(WidgetsManager.Action.OPEN_SETTING);
    }
  };

  static final ActionButton RECALL_BUTTON = new ActionButton(
    Area.RECALL_BUTTON,
    new ItemStack(Items.GOAT_HORN),
    RECALL_COMPONENT
  ) {
    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      PetNetwork.recallPet(ClientPetData.getInstance().getFocusPet().getUUID());
    }
  };

  public static final RenameBox RENAME_BOX = new RenameBox(8, -60);

  static final ActionButton RENAME_SURE_BUTTON = new ActionButton(
    Area.RENAME_SURE_BUTTON,
    new ItemStack(Items.EXPERIENCE_BOTTLE)
  ) {
    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
      super.renderWidget(guiGraphics, i, i1, v);
      guiGraphics.drawString(getFont(), "1", getX(), getY(), COLOR_GREEN);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      PetNetwork.renamePet(ClientPetData.getInstance().getFocusPet().getUUID(), RENAME_BOX.getValue());
    }
  };

  static final ActionButton CANCEL_PET = new ActionButton(Area.CANCEL_PET, new ItemStack(Items.TNT_MINECART)) {
    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      PetNetwork.switchCanceled(ClientPetData.getInstance().getFocusPet().getUUID());
    }
  };

  public static final StatusBar STATUS_BAR = new StatusBar(Area.STATUS_BAR);

  static final ActionButton GO_HOME = new ActionButton(
    Area.GO_HOME,
    new ItemStack(Items.RED_BED),
    Component.translatable("petmanager.screen.setting.goToHome")
  ) {
    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      PetNetwork.goHome(ClientPetData.getInstance().getFocusPet().getUUID());
    }
  };

  static final ActionButton PET_OR_CANCELED_PET = new ActionButton(
    Area.PET_OR_CANCELED_PET,
    new ItemStack(Items.LAVA_BUCKET)
  ) {
    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      boolean last = ClientPetData.getInstance().focusOnCanceledPet;
      ClientPetData.getInstance().focusOnCanceledPet = !last;
      this.icon = !ClientPetData.getInstance().focusOnCanceledPet
        ? new ItemStack(Items.LAVA_BUCKET)
        : new ItemStack(Items.WATER_BUCKET);
      ModEvent.publish(ModEvent.Event.SWITCH_UNCANCELED_PET);
    }
  };

  static final ActionButton FRIEND_FIRE = new ActionButton(
    FRIENDLY_FIRE,
    new ItemStack(Items.CACTUS),
    Component.translatable("petmanager.screen.setting.friendFire")
  ) {
    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      PetNetwork.switchFriendlyFire();
    }

    void updateIcon() {
      icon = ClientPetData.getInstance().getPlayerInfo().isFriendFireEnable()
        ? new ItemStack(Items.CACTUS)
        : new ItemStack(Items.SHIELD);
    }

    {
      ModEvent.subscribe(ModEvent.Event.PLAYER_INFO_ARRIVED, this::updateIcon);
    }
  };
}
