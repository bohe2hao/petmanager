package com.github.bohe2hao.petmanager.others;

import java.util.*;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class PetOthers {

  public static final ScheduledManager<Void> SERVER = new ScheduledManager<>();
  public static final ScheduledManager<Void> CLIENT = new ScheduledManager<>();
  public static final ScheduledManager<RenderLevelStageEvent> RENDER = new ScheduledManager<>();

  public static final TicketType<UUID> RECALL = TicketType.create("pet_recall", UUID::compareTo, 20);

  public static void glow(Entity entity) {
    if (entity instanceof LivingEntity Pet) {
      Pet.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
    }
  }

    public static class entity {

        @Nullable
      public static Entity findFromServer(@Nonnull MinecraftServer server, @Nonnull UUID needsToBeFound) {
        Iterable<ServerLevel> levels = server.getAllLevels();
        for (ServerLevel l : levels) {
          Entity lFound = l.getEntity(needsToBeFound);
          if (lFound != null) {
            return lFound;
          }
        }
        return null;
      }

        @Nonnull
      public static ArrayList<Entity> findThemFromServer(
        @Nonnull MinecraftServer server,
        @Nonnull Set<UUID> needsToBeFound
      ) {

        Objects.requireNonNull(server, "Server must not be null");
        Objects.requireNonNull(needsToBeFound, "UUID array must not be null");

        HashSet<UUID> notFoundYet = new HashSet<>(needsToBeFound);

        ArrayList<Entity> found = new ArrayList<>(needsToBeFound.size());


        for (ServerLevel level : server.getAllLevels()) {

          Iterator<UUID> iterator = notFoundYet.iterator();
          while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
              found.add(entity);
              iterator.remove();
            }
          }
          if (notFoundYet.isEmpty()) {
            break;
          }
        }
        return found;
      }
    }
}
