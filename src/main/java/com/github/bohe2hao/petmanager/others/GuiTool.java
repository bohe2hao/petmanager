package com.github.bohe2hao.petmanager.others;

import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GuiTool {

  public static class PNGInfo {

    ResourceLocation resourceLocation;
    public int width;
    public int height;

    public PNGInfo(String namespaceAndPath, int width, int height) {
      this.resourceLocation = ResourceLocation.parse(namespaceAndPath);
      this.width = width;
      this.height = height;
    }
  }

  public static void hoveredEffect(AbstractWidget widget, GuiGraphics guiGraphics) {
    hoveredEffect(widget, guiGraphics, Color.WHITE);
  }

  public static void hoveredEffect(AbstractWidget widget, GuiGraphics guiGraphics, Color color) {
    if (widget.isHovered()) {
      guiGraphics.renderOutline(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), color.getRGB());
    }
  }

    public static RenderData RenderDataBuilder(PNGInfo png) {
    return new RenderData(png, renderMode.COMPLETE_PIC);
  }

  public static void drawCenteredScaledText(
    GuiGraphics guiGraphics,
    Component text,
    int rectX,
    int rectY,
    int rectWidth,
    int rectHeight,
    int color,
    boolean shadow
  ) {
    Font font = Minecraft.getInstance().font;
    int textWidth = font.width(text);
    int textHeight = font.lineHeight;

    float scale = Math.min((float) (rectWidth - 4) / textWidth, (float) (rectHeight - 2) / textHeight);

    int scaledWidth = (int) (textWidth * scale);
    int scaledHeight = (int) (textHeight * scale);

    guiGraphics.pose().pushPose();
    guiGraphics
      .pose()
      .translate(rectX + (float) (rectWidth - scaledWidth) / 2, rectY + (float) (rectHeight - scaledHeight) / 2, 0);
    guiGraphics.pose().scale(scale, scale, 1.0f);

    guiGraphics.drawString(font, text, 0, 0, color, shadow);
    guiGraphics.pose().popPose();
  }

  public static Point getCenterPoint() {
    int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
    int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
    return new Point(screenWidth / 2, screenHeight / 2);
  }

  public static int getCenterX() {
    return getCenterPoint().x;
  }

  public static int getCenterY() {
    return getCenterPoint().y;
  }

  public enum renderMode {
    COMPLETE_PIC,
    NINE_GRID
  }

    //纹理
  public static class RenderData {

    //指向纹理及其宽高
    PNGInfo png;

    renderMode mode;
    Point widthHeight;

    private int upLine = 1;
    private int downLine = 1;
    private int leftLine = 1;
    private int rightLine = 1;

    /**
     * 设置纹理的分割线距离，用于定义从图像各侧边缘向内预留的坐标距离。
     * 调用此方法会更新纹理的切割区域，通常用于UI精灵图的九宫格布局或自定义绘制边界。
     *
     * @param up    从图像顶部边缘向下的预留距离（像素）
     * @param down  从图像底部边缘向上的预留距离（像素）
     * @param left  从图像左侧边缘向右的预留距离（像素）
     * @param right 从图像右侧边缘向左的预留距离（像素）
     * @return 当前纹理对象（支持链式调用）
     */
    public RenderData setDividingLine(int up, int down, int left, int right) {
      upLine = up;
      downLine = down;
      leftLine = left;
      rightLine = right;
      return this;
    }

        public RenderData setWidthHeight(Point point) {
      widthHeight = point;
      return this;
    }

    public RenderData setRenderMode(renderMode mode) {
      this.mode = mode;
      return this;
    }

    RenderData(PNGInfo png, renderMode renderMode) {
      this.png = png;
      mode = renderMode;
      widthHeight = new Point(png.width, png.height);
    }
  }

    public static void render(GuiGraphics guiGraphics, RenderData renderData, Point leftTop) {
    render(guiGraphics, renderData, leftTop, renderData.widthHeight);
  }

  public static void render(GuiGraphics guiGraphics, RenderData renderData, Rectangle rectangle) {
    render(guiGraphics, renderData, new Point(rectangle.x, rectangle.y), new Point(rectangle.width, rectangle.height));
  }

  public static void render(GuiGraphics guiGraphics, RenderData renderData, Point leftTop, Point widthHeight) {
    renderMode mode = renderData.mode;
    switch (mode) {
      case COMPLETE_PIC ->
              renderComplete(guiGraphics, renderData, leftTop, widthHeight);
      case NINE_GRID ->
              renderNineGrid(guiGraphics, renderData, leftTop, widthHeight);
    }
  }

  /**
   * 计算实体缩放比例，使实体适合指定包围盒
   * @param box 目标包围盒尺寸 (minx=宽, miny=高, z=深)
   * @param entity 要缩放的实体
   * @return 缩放比例
   */
  public static float getEntityScale(Vec3 box, Entity entity) {
    // 获取实体的原始包围盒
    AABB entityBounds = entity.getBoundingBox();

    // 计算实体的原始尺寸
    double entityWidth = entityBounds.getXsize(); // X方向宽度
    double entityHeight = entityBounds.getYsize(); // Y方向高度
    double entityDepth = entityBounds.getZsize(); // Z方向深度

    // 计算每个轴的缩放比例
    double scaleX = box.x / entityWidth;
    double scaleY = box.y / entityHeight;
    double scaleZ = box.z / entityDepth;

    return (float) Math.min(Math.min(scaleX, scaleY), scaleZ);
  }

    public static void renderNineGrid(GuiGraphics guiGraphics, RenderData renderData, Point leftTop, Point widthHeight) {
    Point trueLeftTop = new Point(leftTop.x, leftTop.y);
    int x = trueLeftTop.x;
    int y = trueLeftTop.y;

    // 获取纹理的源尺寸和渲染尺寸
    int srcWidth = renderData.png.width;
    int srcHeight = renderData.png.height;
    int renderWidth = widthHeight.x;
    int renderHeight = widthHeight.y;

    // 获取分割线位置
    int up = renderData.upLine;
    int down = renderData.downLine;
    int left = renderData.leftLine;
    int right = renderData.rightLine;

    // 计算中间区域的尺寸
    int srcCenterWidth = srcWidth - left - right;
    int srcCenterHeight = srcHeight - up - down;
    int renderCenterWidth = renderWidth - left - right;
    int renderCenterHeight = renderHeight - up - down;

    // 1. 左上角
    if (left > 0 && up > 0) {
      guiGraphics.blit(renderData.png.resourceLocation, x, y, left, up, 0, 0, left, up, srcWidth, srcHeight);
    }

    // 2. 中上
    if (srcCenterWidth > 0 && up > 0 && renderCenterWidth > 0) {
      guiGraphics.blit(
        renderData.png.resourceLocation,
        x + left,
        y,
        renderCenterWidth,
        up,
        left,
        0,
        srcCenterWidth,
        up,
        srcWidth,
        srcHeight
      );
    }

    // 3. 右上角
    if (right > 0 && up > 0) {
      guiGraphics.blit(
        renderData.png.resourceLocation,
        x + renderWidth - right,
        y,
        right,
        up,
        srcWidth - right,
        0,
        right,
        up,
        srcWidth,
        srcHeight
      );
    }

    // 4. 中左
    if (left > 0 && srcCenterHeight > 0 && renderCenterHeight > 0) {
      guiGraphics.blit(
        renderData.png.resourceLocation,
        x,
        y + up,
        left,
        renderCenterHeight,
        0,
        up,
        left,
        srcCenterHeight,
        srcWidth,
        srcHeight
      );
    }

    // 5. 中心区域（可拉伸部分）
    if (srcCenterWidth > 0 && srcCenterHeight > 0 && renderCenterWidth > 0 && renderCenterHeight > 0) {
      guiGraphics.blit(
        renderData.png.resourceLocation,
        x + left,
        y + up,
        renderCenterWidth,
        renderCenterHeight,
        left,
        up,
        srcCenterWidth,
        srcCenterHeight,
        srcWidth,
        srcHeight
      );
    }

    // 6. 中右
    if (right > 0 && srcCenterHeight > 0 && renderCenterHeight > 0) {
      guiGraphics.blit(
        renderData.png.resourceLocation,
        x + renderWidth - right,
        y + up,
        right,
        renderCenterHeight,
        srcWidth - right,
        up,
        right,
        srcCenterHeight,
        srcWidth,
        srcHeight
      );
    }

    // 7. 左下角
    if (left > 0 && down > 0) {
      guiGraphics.blit(
        renderData.png.resourceLocation,
        x,
        y + renderHeight - down,
        left,
        down,
        0,
        srcHeight - down,
        left,
        down,
        srcWidth,
        srcHeight
      );
    }

    // 8. 中下
    if (srcCenterWidth > 0 && down > 0 && renderCenterWidth > 0) {
      guiGraphics.blit(
        renderData.png.resourceLocation,
        x + left,
        y + renderHeight - down,
        renderCenterWidth,
        down,
        left,
        srcHeight - down,
        srcCenterWidth,
        down,
        srcWidth,
        srcHeight
      );
    }

    // 9. 右下角
    if (right > 0 && down > 0) {
      guiGraphics.blit(
        renderData.png.resourceLocation,
        x + renderWidth - right,
        y + renderHeight - down,
        right,
        down,
        srcWidth - right,
        srcHeight - down,
        right,
        down,
        srcWidth,
        srcHeight
      );
    }
  }

    public static void renderComplete(GuiGraphics guiGraphics, RenderData renderData, Point leftTop, Point widthHeight) {
    guiGraphics.blit(
      renderData.png.resourceLocation,
      leftTop.x,
      leftTop.y,
      widthHeight.x,
      widthHeight.y,
      0,
      0,
      renderData.png.width,
      renderData.png.height,
      renderData.png.width,
      renderData.png.height
    );
  }
}
