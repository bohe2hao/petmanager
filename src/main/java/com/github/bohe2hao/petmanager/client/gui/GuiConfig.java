package com.github.bohe2hao.petmanager.client.gui;

import static com.github.bohe2hao.petmanager.others.GuiTool.*;

import java.awt.*;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiConfig {

  public static EntityRenderDispatcher entityRenderDispatcher;

  public static Font getFont() {
    return Minecraft.getInstance().font;
  }

  public static void update() {
    entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
  }

  //area
  public static class Area {

    public static final Rectangle MODEL = new Rectangle(-25, -60, 70, 80);
    public static final Rectangle ROLLING = new Rectangle(-129, -81, PET_BUTTON_WIDTH + 1, PET_BUTTON_LIST_CLIP_HEIGHT);

    public static final Rectangle BACKGROUND = new Rectangle(
      -png.PET_SCREEN_PIC.width / 2,
      -png.PET_SCREEN_PIC.height / 2,
      png.PET_SCREEN_PIC.width,
      png.PET_SCREEN_PIC.height
    );

    static {
      BACKGROUND.grow(50, 30);
    }

    public static final Rectangle STATUS_BAR = new Rectangle(BACKGROUND.x, BACKGROUND.y - 15, BACKGROUND.width, 15);
    public static final Rectangle SET_HOME = new Rectangle(45, 50, 80, 30);
    public static final Rectangle FRIENDLY_FIRE = new Rectangle(
      SET_HOME.x,
      SET_HOME.y - SET_HOME.height - 10,
      SET_HOME.width,
      SET_HOME.height
    );
    public static final Rectangle OPEN_SETTING = new Rectangle(150, 79, 20, 20);
    public static final Rectangle RECALL_BUTTON = new Rectangle(55, 20, 70, 30);
    public static final Rectangle RENAME_SURE_BUTTON = new Rectangle(110, -63, 20, 20);
    public static final Rectangle CANCEL_PET = new Rectangle(-180, 98, 18, 18);
    public static final Rectangle GO_HOME = new Rectangle(-23, 20, 70, 30);
    public static final Rectangle PET_OR_CANCELED_PET = new Rectangle(150, 54, 20, 20);
  }

  //int
  public static final int PET_BUTTON_LIST_CLIP_HEIGHT = 164;
  public static final int PET_BUTTON_X = -129;
  public static final int PET_BUTTON_WIDTH = 94;
  public static final int PET_BUTTON_HEIGHT = 20;
  public static final int BOTTON_INTERVAL = 2;

  public static final int COLOR_BABY = 0xFFB6C1; // 浅粉色
  public static final int COLOR_FIRE = 0xFF4500; // 火焰橙色
  public static final int COLOR_WHITE = new Color(1f, 1f, 1f, 1f).getRGB();
  public static final int COLOR_GREEN = new Color(0.4f, 0.9f, 0.4f, 0.5f).getRGB();
  public static final int COLOR_BLACK = new Color(0.2f, 0.2f, 0.2f, 0.95f).getRGB();

  //renderData
  public static final RenderData HEART = RenderDataBuilder(png.FULL_HEART);

  public static final RenderData BUTTON_OPPOSITE = RenderDataBuilder(png.OPPOSITE_BUTTON_PIC)
    .setRenderMode(renderMode.NINE_GRID)
    .setDividingLine(4, 4, 4, 4);

  public static final RenderData BUTTON = RenderDataBuilder(png.BUTTON_PIC)
    .setRenderMode(renderMode.NINE_GRID)
    .setDividingLine(4, 4, 4, 4);

  public static final RenderData BUTTON_DARKER = RenderDataBuilder(png.BUTTON_DARKER_PIC)
    .setRenderMode(renderMode.NINE_GRID)
    .setDividingLine(4, 4, 4, 4);

  public static final RenderData PET_SCREEN_BACKGROUND = RenderDataBuilder(png.PET_SCREEN_PIC).setWidthHeight(
    new Point(300, 200)
  );

  //itemStack
  public static final ItemStack NAME_TAG = new ItemStack(Items.NAME_TAG);
  public static final ItemStack SADDLE = new ItemStack(Items.SADDLE);
  //Component
  public static final Component RECALL_COMPONENT = Component.translatable("petmanager.screen.recall");

  //menu
  static final Map<WidgetsManager.Action, WidgetGroupConfig> FOCUS_CONFIG_MAP = Map.of(
    WidgetsManager.Action.INIT,
    WidgetGroupConfig.getBuilder()
      .enable(WidgetGroupConfig.PETS)
      .disable(WidgetGroupConfig.PET_MENU, WidgetGroupConfig.SETTING_MENU)
      .build(),
    WidgetsManager.Action.SWITCH_UNCANCELED_PET,
    WidgetGroupConfig.getBuilder()
      .enable(WidgetGroupConfig.PETS, WidgetGroupConfig.PET_MENU)
      .disable(WidgetGroupConfig.SETTING_MENU)
      .build(),
    WidgetsManager.Action.OPEN_SETTING,
    WidgetGroupConfig.getBuilder().enable(WidgetGroupConfig.SETTING_MENU).disable(WidgetGroupConfig.PET_MENU).build(),
    WidgetsManager.Action.PRESS_PET_BUTTON,
    WidgetGroupConfig.getBuilder().enable(WidgetGroupConfig.PET_MENU).disable(WidgetGroupConfig.SETTING_MENU).build()
  );

  public static final class png {

    public static final String TEXTURES_DIR = "petmanager:textures/";

    public static final PNGInfo FULL_HEART = new PNGInfo("minecraft:textures/gui/sprites/hud/heart/full.png", 9, 9);

    public static final PNGInfo BUTTON_PIC = new PNGInfo(TEXTURES_DIR + "button.png", 13, 13);

    public static final PNGInfo BUTTON_DARKER_PIC = new PNGInfo(TEXTURES_DIR + "button_darker.png", 13, 13);

    public static final PNGInfo OPPOSITE_BUTTON_PIC = new PNGInfo(TEXTURES_DIR + "button_opposite.png", 13, 13);

    public static final PNGInfo PET_SCREEN_PIC = new PNGInfo(TEXTURES_DIR + "pet_screen.png", 193, 134);
  }
}
