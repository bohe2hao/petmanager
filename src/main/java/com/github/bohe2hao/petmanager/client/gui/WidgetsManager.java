package com.github.bohe2hao.petmanager.client.gui;

import static com.github.bohe2hao.petmanager.client.gui.PetWidget.*;
import static com.github.bohe2hao.petmanager.client.gui.WidgetsManager.WidgetGroup.*;

import com.github.bohe2hao.petmanager.client.gui.other.RePosAble;
import com.github.bohe2hao.petmanager.client.gui.widgets.PetButton;
import com.github.bohe2hao.petmanager.event.ModEvent;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetsManager {

  public enum Action {
    INIT,
    SWITCH_UNCANCELED_PET,
    OPEN_SETTING,
    PRESS_PET_BUTTON;

    ModEvent.Event event;
  }

  public enum WidgetGroup {
    PET_BUTTONS,
    PET_MENU,
    SETTING_MENU;

    private boolean visibility = true;
    public final Set<AbstractWidget> group = new HashSet<>();

    void add(AbstractWidget widget) {
      widget.visible = this.visibility;
      group.add(widget);
    }

    private void setVisibility(Boolean visibility) {
      if (visibility == null) return;
      this.visibility = visibility;
      group.forEach(w -> w.visible = visibility);
    }
  }

  public final Set<RePosAble> rePosable = new HashSet<>();

  public static void applyActionConfig(Action actionConfig) {
    WidgetGroupConfig config = GuiConfig.FOCUS_CONFIG_MAP.get(actionConfig);
    SETTING_MENU.setVisibility(config.settingMenu);
    PET_MENU.setVisibility(config.petMenu);
    PET_BUTTONS.setVisibility(config.pets);
    if (actionConfig == Action.SWITCH_UNCANCELED_PET) {
      ModEvent.publish(ModEvent.Event.SWITCH_UNCANCELED_PET);
    }
  }

  public void clearPetButtons() {
    rePosable.removeIf(b -> b instanceof PetButton);
    PET_BUTTONS.group.clear();
  }

  public void addReposAble(RePosAble rePosAble) {
    rePosable.add(rePosAble);
  }

  public void rePos() {
    rePosable.forEach(RePosAble::repos);
  }

  WidgetsManager() {
    applyActionConfig(Action.INIT);
    rePosable.add(PET_OR_CANCELED_PET);
    rePosable.add(OPEN_SETTING);
    rePosable.add(RENAME_BOX);
    rePosable.add(RENAME_SURE_BUTTON);
    rePosable.add(RECALL_BUTTON);
    rePosable.add(GO_HOME);
    rePosable.add(STATUS_BAR);
    rePosable.add(SET_HOME);
    rePosable.add(CANCEL_PET);
    rePosable.add(FRIEND_FIRE);
  }

  static {
    PET_MENU.add(RENAME_BOX);
    PET_MENU.add(RENAME_SURE_BUTTON);
    PET_MENU.add(RECALL_BUTTON);
    PET_MENU.add(GO_HOME);
    PET_MENU.add(INFO_WINDOW);
    PET_MENU.add(CANCEL_PET);

    SETTING_MENU.add(SET_HOME);
    SETTING_MENU.add(PET_OR_CANCELED_PET);
    SETTING_MENU.add(FRIEND_FIRE);
  }
}
