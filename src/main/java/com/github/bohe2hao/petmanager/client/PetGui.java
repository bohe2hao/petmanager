package com.github.bohe2hao.petmanager.client;

import static com.github.bohe2hao.petmanager.client.gui.GuiConfig.*;

import com.github.bohe2hao.petmanager.Config;
import com.github.bohe2hao.petmanager.client.gui.widgets.BaseWidget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PetGui {

  //--打开宠物界面按钮
  public static class petMenuOpenButton extends BaseWidget {

    public petMenuOpenButton() {
      super(0, 0, 20, 20, Component.empty());
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      PetManagerClient.switchPetScreen();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
  }

  //获取实体提示
  public static List<Component> getEntityTooltip(Entity entity) {
    List<Component> tooltipLines = new ArrayList<>();
    if (!(entity instanceof LivingEntity lEntity)) return tooltipLines;
    float health = lEntity.getHealth();
    float maxHealth = lEntity.getMaxHealth();
    boolean isBaby = lEntity.isBaby();
    boolean isOnFire = lEntity.isOnFire();
    AttributeInstance attackDamage = lEntity.getAttribute(Attributes.ATTACK_DAMAGE);
    AttributeInstance movementSpeed = lEntity.getAttribute(Attributes.MOVEMENT_SPEED);

    // 生命值
    tooltipLines.add(
      Component.translatable("petmanager.screen.health")
        .withStyle(Style.EMPTY.withBold(true).withColor(Config.getPreferredColor()))
        .append(
          Component.literal(String.format(": %.1f/%.1f", health, maxHealth)).withStyle(
            Style.EMPTY.withBold(false).withColor(COLOR_WHITE)
          )
        )
    );

    // 攻击力
    if (attackDamage != null) {
      tooltipLines.add(
        Component.translatable("petmanager.screen.attack")
          .withStyle(Style.EMPTY.withBold(true).withColor(Config.getPreferredColor()))
          .append(
            Component.literal(String.format(": %.1f", attackDamage.getValue())).withStyle(
              Style.EMPTY.withBold(false).withColor(COLOR_WHITE)
            )
          )
      );
    }

    // 移动速度
    if (movementSpeed != null) {
      tooltipLines.add(
        Component.translatable("petmanager.screen.speed")
          .withStyle(Style.EMPTY.withBold(true).withColor(Config.getPreferredColor()))
          .append(
            Component.literal(String.format(": %.2f", movementSpeed.getValue())).withStyle(
              Style.EMPTY.withBold(false).withColor(COLOR_WHITE)
            )
          )
      );
    }

    // 状态
    if (isBaby || isOnFire) {
      MutableComponent status = Component.translatable("petmanager.screen.status")
        .withStyle(Style.EMPTY.withBold(true).withColor(Config.getPreferredColor()))
        .append(Component.literal(": "));

      if (isBaby) {
        status.append(Component.translatable("petmanager.screen.baby").withStyle(Style.EMPTY.withColor(COLOR_BABY)));
      }
      if (isOnFire) {
        if (isBaby) status.append(Component.literal(" | "));
        status.append(Component.translatable("petmanager.screen.on_fire").withStyle(Style.EMPTY.withColor(COLOR_FIRE)));
      }
      tooltipLines.add(status);
    }
    return tooltipLines;
  }
}
