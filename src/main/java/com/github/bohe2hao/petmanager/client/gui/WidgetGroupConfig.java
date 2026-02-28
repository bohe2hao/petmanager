package com.github.bohe2hao.petmanager.client.gui;

public class WidgetGroupConfig {

  public static final int PETS = 0;
  public static final int PET_MENU = 1;
  public static final int SETTING_MENU = 2;

  Boolean pets = null;
  Boolean petMenu = null;
  Boolean settingMenu = null;

  public static FocusConfigBuilder getBuilder() {
    return new FocusConfigBuilder();
  }

  //builder
  public static class FocusConfigBuilder {

    private final WidgetGroupConfig config = new WidgetGroupConfig();

    public FocusConfigBuilder enable(int... enable) {
      return setFocusState(enable, true);
    }

    public FocusConfigBuilder disable(int... disable) {
      return setFocusState(disable, false);
    }

    private FocusConfigBuilder setFocusState(int[] focusTypes, boolean state) {
      for (int i : focusTypes) {
        switch (i) {
          case WidgetGroupConfig.PETS -> this.config.pets = state;
          case WidgetGroupConfig.PET_MENU -> this.config.petMenu = state;
          case WidgetGroupConfig.SETTING_MENU -> this.config.settingMenu = state;
        }
      }
      return this;
    }

    public WidgetGroupConfig build() {
      return config;
    }
  }
}
