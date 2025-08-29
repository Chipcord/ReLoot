package net.chip.reloot.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

@Mod.EventBusSubscriber
public class RefillScheduler {

    private static final List<ScheduledRefill> scheduledRefills = new ArrayList<>();

    public static void schedule(ServerLevel level, List<BlockPos> chestPositions, ResourceLocation lootTable, boolean replace, int delaySeconds) {
        ScheduledRefill task = new ScheduledRefill(level, chestPositions, lootTable, replace, delaySeconds * 20);
        task.spawnCountdownEntities();
        scheduledRefills.add(task);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<ScheduledRefill> iterator = scheduledRefills.iterator();

        while (iterator.hasNext()) {
            ScheduledRefill task = iterator.next();
            task.ticksRemaining--;

            if (task.ticksRemaining % 20 == 0) {
                task.updateCountdownEntities();
            }

            if (task.ticksRemaining <= 0) {
                int refilled = 0;

                for (BlockPos pos : task.chestPositions) {
                    BlockEntity be = task.level.getBlockEntity(pos);
                    if (be instanceof ChestBlockEntity chest) {
                        if (task.replace) {
                            chest.clearContent();
                        }
                        chest.setLootTable(task.lootTable, task.level.random.nextLong());
                        chest.setChanged();
                        refilled++;
                    }
                }

                task.ticksRemaining = task.initialTicks;

                task.updateCountdownEntities();
            }
        }
    }

    private static class ScheduledRefill {
        final ServerLevel level;
        final List<BlockPos> chestPositions;
        final ResourceLocation lootTable;
        final boolean replace;
        final int initialTicks;
        int ticksRemaining;

        final Map<BlockPos, UUID> countdownEntities = new HashMap<>();

        ScheduledRefill(ServerLevel level, List<BlockPos> chestPositions, ResourceLocation lootTable, boolean replace, int ticksRemaining) {
            this.level = level;
            this.chestPositions = chestPositions;
            this.lootTable = lootTable;
            this.replace = replace;
            this.initialTicks = ticksRemaining;
            this.ticksRemaining = ticksRemaining;
        }

        void spawnCountdownEntities() {
            for (BlockPos pos : chestPositions) {
                ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);

                stand.setInvisible(true);
                stand.setNoGravity(true);
                stand.setCustomNameVisible(true);
                stand.setCustomName(Component.literal(formatSeconds(ticksRemaining / 20)));
                stand.setInvulnerable(true);
                stand.setSilent(true);
                stand.setPos(pos.getX() + 0.5, pos.getY() - 1.0, pos.getZ() + 0.5);

                level.addFreshEntity(stand);
                countdownEntities.put(pos, stand.getUUID());
            }
        }

        void updateCountdownEntities() {
            int secondsRemaining = ticksRemaining / 20;

            for (Map.Entry<BlockPos, UUID> entry : countdownEntities.entrySet()) {
                UUID uuid = entry.getValue();
                ArmorStand stand = (ArmorStand) level.getEntity(uuid);
                if (stand != null) {
                    stand.setCustomName(Component.literal(formatSeconds(secondsRemaining)));
                }
            }
        }

        void removeCountdownEntities() {
            for (UUID uuid : countdownEntities.values()) {
                var entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.discard();
                }
            }
            countdownEntities.clear();
        }

        private String formatSeconds(int seconds) {
            return seconds + "s";
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        if (level.getBlockState(pos).is(Blocks.CHEST)) {
            for (ScheduledRefill task : scheduledRefills) {
                if (task.level == level && task.countdownEntities.containsKey(pos)) {
                    UUID uuid = task.countdownEntities.get(pos);
                    var entity = level.getEntity(uuid);
                    if (entity != null) {
                        entity.discard();
                    }
                    task.countdownEntities.remove(pos);
                    task.chestPositions.remove(pos);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        clearAllScheduledRefills();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            clearAllScheduledRefills();
        }
    }

    public static void clearAllScheduledRefills() {
        for (ScheduledRefill task : scheduledRefills) {
            task.removeCountdownEntities();
        }
        scheduledRefills.clear();
    }

}
