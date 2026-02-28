package com.github.bohe2hao.petmanager.data;

import com.github.bohe2hao.petmanager.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import oshi.util.tuples.Pair;

public class PlayerInfo {

    boolean friendFire = true;
    GlobalPos home;
    long lastAction = System.currentTimeMillis();

    static final String FRIEND_FIRE = "friendFire";
    static final String LAST_ACTION = "lastAction";
    static final String HOME = "home";
    static final String HOME_DIMENSION = "homeDimension";

    public void switchFriendFire() {
        friendFire = !friendFire;
        setCd();
    }

    public boolean isFriendFireEnable() {
        return friendFire;
    }

    @Nonnull
    public Pair<GlobalPos, Boolean> setHome(Player player) {
        BlockPos playerPosBlock = player.getOnPos();
        BlockPos homeBlock = new BlockPos(playerPosBlock.getX(),
                                          playerPosBlock.getY() + 2,
                                          playerPosBlock.getZ());
        GlobalPos TargetPos = GlobalPos.of(player.level().dimension(),
                                           homeBlock);
        if (player.level().getBlockState(homeBlock).is(Blocks.AIR)) {
            home = TargetPos;
            setCd();
            return new Pair<>(home, true);
        }
        setCd();
        return new Pair<>(TargetPos, false);
    }

    @Nullable
    public GlobalPos getHome() {
        return home;
    }

    public void setCd() {
        lastAction = System.currentTimeMillis();
        PetData.getInstance().setDirty();
    }

    public boolean isCoolDownEnough() {
        return ((System.currentTimeMillis() - lastAction) > Config.PLAYER_OPERATES_COOLDOWN_TIME.get() * 1000);
    }

    public CompoundTag packAsNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(FRIEND_FIRE, friendFire);
        tag.putLong(LAST_ACTION, lastAction);
        if (home != null) {
            CompoundTag homeTag = new CompoundTag();
            homeTag.putString(HOME_DIMENSION,
                              home.dimension().location().toString());
            homeTag.putInt("x", home.pos().getX());
            homeTag.putInt("y", home.pos().getY());
            homeTag.putInt("z", home.pos().getZ());
            tag.put(HOME, homeTag);
        }
        return tag;
    }

    public static PlayerInfo rebuild(CompoundTag nbt) {
        PlayerInfo info = new PlayerInfo();
        info.friendFire = nbt.getBoolean(FRIEND_FIRE);
        info.lastAction = nbt.getLong(LAST_ACTION);
        if (nbt.contains(HOME)) {
            CompoundTag homeTag = nbt.getCompound(HOME);
            ResourceKey<Level> dimension = ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.parse(homeTag.getString(HOME_DIMENSION))
            );
            info.home = GlobalPos.of(dimension, new BlockPos(homeTag.getInt(
                    "x"), homeTag.getInt("y"), homeTag.getInt("z")));
        }
        return info;
    }
}
