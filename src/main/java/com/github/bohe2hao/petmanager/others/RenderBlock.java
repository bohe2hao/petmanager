package com.github.bohe2hao.petmanager.others;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class RenderBlock {

    public static void line(RenderLevelStageEvent event, BlockPos pos, Color color) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
    PoseStack pose = event.getPoseStack();
    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
    Position camera = event.getCamera().getPosition();

    pose.pushPose();
    pose.translate(-camera.x() + pos.getX(), -camera.y() + pos.getY(), -camera.z() + pos.getZ());

    LevelRenderer.renderVoxelShape(
      pose,
      vertexConsumer,
      Shapes.block(),
      0,
      0,
      0,
      color.getRed() / 255f,
      color.getGreen() / 255f,
      color.getBlue() / 255f,
      color.getAlpha() / 255f,
      true
    );

    pose.popPose();
  }

  public static void solid(RenderLevelStageEvent event, BlockPos pos, BlockState blockState, Color color) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
      return;
    }
    PoseStack pose = event.getPoseStack();
    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

    bufferSource.endBatch();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.depthMask(false);
    RenderSystem.depthFunc(GL11.GL_LEQUAL);
    RenderSystem.setShaderColor(
      color.getRed() / 255f,
      color.getGreen() / 255f,
      color.getBlue() / 255f,
      color.getAlpha() / 255f
    );
    VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
    Position camera = event.getCamera().getPosition();

    pose.pushPose();
    pose.translate(-camera.x() + pos.getX(), -camera.y() + pos.getY(), -camera.z() + pos.getZ());
    BakedModel model = blockRenderDispatcher.getBlockModel(blockState);
    blockRenderDispatcher
      .getModelRenderer()
      .renderModel(
        pose.last(),
        vertexConsumer,
        blockState,
        model,
        0,
        0,
        0,
        LightTexture.FULL_BRIGHT,
        OverlayTexture.NO_OVERLAY,
        ModelData.EMPTY,
        RenderType.translucent()
      );
    pose.popPose();
    bufferSource.endBatch();
    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    RenderSystem.depthMask(true);
    RenderSystem.disableBlend();
  }
}
