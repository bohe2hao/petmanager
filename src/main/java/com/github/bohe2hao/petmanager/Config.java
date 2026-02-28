package com.github.bohe2hao.petmanager;

import java.awt.*;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

  private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

  private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

  public static final ModConfigSpec.DoubleValue PLAYER_OPERATES_COOLDOWN_TIME = BUILDER.comment(
    "玩家操作间隔,最小0.5s,最大1分钟"
  ).defineInRange("playerOperatesCooldownTime", 0.5, 0.5, 60);

  public static final ModConfigSpec.DoubleValue PREFERRED_COLOR_RED = CLIENT_BUILDER.comment(
    "玩家偏好颜色_红"
  ).defineInRange("color_red", 0.6f, 0f, 1f);

  public static final ModConfigSpec.DoubleValue PREFERRED_COLOR_GREEN = CLIENT_BUILDER.comment(
    "玩家偏好颜色_绿"
  ).defineInRange("color_green", 0.2f, 0f, 1f);

  public static final ModConfigSpec.DoubleValue PREFERRED_COLOR_BLUE = CLIENT_BUILDER.comment(
    "玩家偏好颜色_蓝"
  ).defineInRange("color_blue", 0.2f, 0f, 1f);

  static final ModConfigSpec SPEC = BUILDER.build();

  static final ModConfigSpec SPEC_CLIENT = CLIENT_BUILDER.build();

  public static int getPreferredColor() {
    return new Color(
      PREFERRED_COLOR_RED.get().floatValue(),
      PREFERRED_COLOR_GREEN.get().floatValue(),
      PREFERRED_COLOR_BLUE.get().floatValue(),
      0.5f
    ).getRGB();
  }
}
