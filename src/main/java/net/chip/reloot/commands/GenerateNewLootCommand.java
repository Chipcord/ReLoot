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
import net.minecraft.resources.ResourceLocation;

public class GenerateNewLootCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("generateNewLoot")
                .requires(source -> source.hasPermission(2)) // OP only
                .then(Commands.argument("loottable", ResourceLocationArgument.id())
                        .executes(ctx -> convert(
                                ctx.getSource(),
                                ResourceLocationArgument.getId(ctx, "loottable"),
                                50 // Default radius
                        ))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                .executes(ctx -> convert(
                                        ctx.getSource(),
                                        ResourceLocationArgument.getId(ctx, "loottable"),
                                        IntegerArgumentType.getInteger(ctx, "radius")
                                ))
                        )
                )

        );
    }

    private static int convert(CommandSourceStack source, ResourceLocation lootTable, int radius) {
        ServerLevel level = source.getLevel();
        BlockPos center = BlockPos.containing(source.getPosition());

        int converted = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity chest) {
                chest.setLootTable(lootTable, level.random.nextLong());
                chest.setChanged();
                converted++;
            }
        }

        String message = "Converted " + converted + " chests to use loot table " + lootTable;
        source.sendSuccess(() -> Component.literal(message), true);

        return Command.SINGLE_SUCCESS;
    }
}
