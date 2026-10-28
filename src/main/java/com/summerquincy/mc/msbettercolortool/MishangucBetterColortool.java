package com.summerquincy.mc.msbettercolortool;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MishangucBetterColortool implements ModInitializer {
    @SuppressWarnings("unused")
    public static final String MOD_ID = "msbettercolortool";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @SuppressWarnings("unchecked")
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment)
                        -> dispatcher.register(Commands.literal("mscolortool")
                        .then(Commands.argument("color_rgb", StringArgumentType.string()).executes(
                                ctx -> {
                                    Player player = ctx.getSource().getPlayer();
                                    ItemStack item;
                                    if (player != null) {
                                        item = player.getMainHandItem();
                                    } else {
                                        return -1;
                                    }
                                    if (item.is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mishanguc", "color_tool")))) {
                                        int color;
                                        try {
                                            color = Integer.parseInt(ctx.getArgument("color_rgb", String.class), 16);
                                            if (color < 0 || color > 0xffffff) throw new NumberFormatException();
                                            item.set((DataComponentType<Integer>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("mishanguc", "color")), color);
                                        } catch (NumberFormatException e) {
                                            player.sendSystemMessage(Component.translatable("message.msbettercolortool.invalid").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                                            return -1;
                                        } catch (Exception e) {
                                            return -1;
                                        }
                                        player.sendSystemMessage(Component.translatable("message.msbettercolortool.success").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
                                    } else {
                                        player.sendSystemMessage(Component.translatable("message.msbettercolortool.wrong_item").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                                    }
                                    return 0;
                                }
                        ))));


        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();
            if (!world.isClientSide && hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
                if (BuiltInRegistries.ITEM.getKey(mainHandItem.getItem())
                        .equals(ResourceLocation.fromNamespaceAndPath("mishanguc", "color_tool")) &&
                        BuiltInRegistries.ITEM.getKey(offHandItem.getItem())
                                .equals(ResourceLocation.fromNamespaceAndPath("rgbblocks", "paint_bucket"))

                ) {
                    Integer paint_bucket_color = offHandItem.get((DataComponentType<Integer>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("rgbblocks", "color")));
                    if (paint_bucket_color != null) {
                        if (paint_bucket_color == 0) paint_bucket_color = -1;
                        int color = paint_bucket_color + (1 << 24);
                        mainHandItem.set((DataComponentType<Integer>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("mishanguc", "color")), color);
                        player.sendSystemMessage(Component.translatable("message.msbettercolortool.success").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
                        return InteractionResultHolder.success(mainHandItem);
                    }
                }
            }
            return InteractionResultHolder.pass(mainHandItem);
        });
    }
}
