package com.summerquincy.mc.msbettercolortool;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public class MishangucBetterColortool implements ModInitializer {
    @SuppressWarnings("unused")
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
                                    if (item.isOf(Registries.ITEM.get(Identifier.of("mishanguc", "color_tool")))) {
                                        NbtCompound tag = item.getNbt();
                                        if (tag == null) tag = new NbtCompound();
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
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack mainHandItem = player.getMainHandStack();
            ItemStack offHandItem = player.getMainHandStack();
            if (!world.isClient && hand == Hand.MAIN_HAND && player.isSneaking()) {
                if (Registries.ITEM.getId(mainHandItem.getItem())
                        .equals(Identifier.of("mishanguc", "color_tool")) &&
                        Registries.ITEM.getId(offHandItem.getItem())
                                .equals(Identifier.of("rgbblocks", "paint_bucket"))

                ) {
                    Integer paint_bucket_color = null;
                    if (offHandItem.getNbt() != null) {
                        paint_bucket_color = offHandItem.getNbt().getInt("rgbblocks:color");
                    }
                    if (paint_bucket_color == null || paint_bucket_color == 0) paint_bucket_color = -1;
                    int color = paint_bucket_color + (1 << 24);
                    NbtCompound tag = mainHandItem.getNbt();
                    if (tag == null) tag = new NbtCompound();
                    tag.putInt("color", color);
                    mainHandItem.setNbt(tag);
                    player.sendMessage(Text.translatable("message.msbettercolortool.success").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                    return TypedActionResult.success(mainHandItem);
                }
            }
            return TypedActionResult.pass(mainHandItem);
        });
    }
}
