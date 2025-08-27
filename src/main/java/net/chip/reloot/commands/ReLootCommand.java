package net.chip.reloot.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.world.level.chunk.LevelChunk;

public class ReLootCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("reloot")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("generateNewLoot")
                                .then(Commands.argument("loottable", ResourceLocationArgument.id())
                                        .executes(ctx -> convert(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "loottable"),
                                                50,
                                                false
                                        ))
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                                .executes(ctx -> convert(
                                                        ctx.getSource(),
                                                        ResourceLocationArgument.getId(ctx, "loottable"),
                                                        IntegerArgumentType.getInteger(ctx, "radius"),
                                                        false
                                                ))
                                                .then(Commands.argument("mode", StringArgumentType.word())
                                                        .executes(ctx -> convert(
                                                                ctx.getSource(),
                                                                ResourceLocationArgument.getId(ctx, "loottable"),
                                                                IntegerArgumentType.getInteger(ctx, "radius"),
                                                                StringArgumentType.getString(ctx, "mode").equalsIgnoreCase("replace")
                                                        ))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("regenerateAllLoot")
                                .then(Commands.argument("loottable", ResourceLocationArgument.id())
                                        .executes(ctx -> convertAllLoadedChunks(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "loottable"),
                                                false
                                        ))
                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .executes(ctx -> convertAllLoadedChunks(
                                                        ctx.getSource(),
                                                        ResourceLocationArgument.getId(ctx, "loottable"),
                                                        StringArgumentType.getString(ctx, "mode").equalsIgnoreCase("replace")
                                                ))
                                        )
                                )
                        )
        );
    }

    private static int convert(CommandSourceStack source, ResourceLocation lootTable, int radius, boolean replace) {
        ServerLevel level = source.getLevel();
        BlockPos center = BlockPos.containing(source.getPosition());

        int converted = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity chest) {
                if (replace) {
                    chest.clearContent();
                }
                chest.setLootTable(lootTable, level.random.nextLong());
                chest.setChanged();
                converted++;
            }
        }

        String mode = replace ? "replace" : "keep";
        String message = "Converted " + converted + " chests within radius " + radius + " using loot table " + lootTable + " (" + mode + ")";
        source.sendSuccess(() -> Component.literal(message), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int convertAllLoadedChunks(CommandSourceStack source, ResourceLocation lootTable, boolean replace) {
        ServerLevel level = source.getLevel();
        int converted = 0;

        BlockPos center = BlockPos.containing(source.getPosition());
        int chunkRadius = 64;

        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;

                if (!level.hasChunk(chunkX, chunkZ)) continue;

                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof ChestBlockEntity chest) {
                        if (replace) {
                            chest.clearContent();
                        }
                        chest.setLootTable(lootTable, level.random.nextLong());
                        chest.setChanged();
                        converted++;
                    }
                }
            }
        }

        String mode = replace ? "replace" : "keep";
        String message = "Converted " + converted + " chests in loaded chunks using loot table " + lootTable + " (" + mode + ")";
        source.sendSuccess(() -> Component.literal(message), true);

        return Command.SINGLE_SUCCESS;
    }
}
