package com.summerquincy.mc.msbettercolortool;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class MishangucBetterColortool implements ModInitializer {
    public static final String MOD_ID = "msbettercolortool";

//    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment)
                        -> dispatcher.register(CommandManager.literal("mscolortool")
                        .then(CommandManager.argument("color_rgb", StringArgumentType.string()).executes(
                                ctx -> {
                                    PlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                    ItemStack item = player.getMainHandStack();
                                    if (item.isOf(Registries.ITEM.get(new Identifier("mishanguc", "color_tool")))) {
                                        NbtCompound tag = new NbtCompound();
                                        int color;
                                        try {
                                            color = Integer.parseInt(ctx.getArgument("color_rgb", String.class), 16);
                                            if (color < 0 || color > 0xffffff) throw new NumberFormatException();
                                        } catch (NumberFormatException e) {
                                            player.sendMessage(Text.translatable("message.msbettercolortool.invalid").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                            return -1;
                                        }
                                        tag.putInt("color", color);
                                        item.setNbt(tag);
                                        player.sendMessage(Text.translatable("message.msbettercolortool.success").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                                    } else {
                                        player.sendMessage(Text.translatable("message.msbettercolortool.wrong_item").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                    }
                                    return 0;
                                }
                        ))));
    }

    @SuppressWarnings("unused")
    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
