/**
 * Spice of Life: Carrot Cake - a Minecraft mod encouraging diverse food consumption
 *     Copyright (C) 2026  willowv
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * To contact me: eps.willow@gmail.com
 */

package dev.willowv.solcc;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

@Mod(SpiceofLifeCarrotCake.MOD_ID)
public class SpiceofLifeCarrotCake {
    public static final String MOD_ID = "solcc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation healthBonusId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "health_bonus");
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    public static final Supplier<AttachmentType<SolccData>> SOLCC_DATA = ATTACHMENT_TYPES.register(
            "solcc_data", () -> AttachmentType.serializable(SolccData::new).copyOnDeath().build()
    );

    public SpiceofLifeCarrotCake(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.debug("Initialized common");
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ATTACHMENT_TYPES.register(modEventBus);
    }

    @EventBusSubscriber(modid= MOD_ID)
    private class EventHandler {
        @SubscribeEvent
        public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
            if(event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide()) {
                LOGGER.debug("onPlayerWakeUp");
                Set<ResourceLocation> uniqueFoodsEaten;
                if(player.hasData(SOLCC_DATA)) {
                    SolccData data = player.getData(SOLCC_DATA);
                    uniqueFoodsEaten = data.getUniqueFoodsEaten();
                } else {
                    uniqueFoodsEaten = new HashSet<>();
                }

                LOGGER.debug("Current Data: {}", uniqueFoodsEaten);
                Set<ResourceLocation> uniqueFoodsUneaten = SolccUtils.getUneatenFoods(uniqueFoodsEaten);
                LOGGER.debug("Uneaten Foods: {}", uniqueFoodsUneaten);
                uniqueFoodsUneaten.stream()
                        .skip(new Random().nextInt((uniqueFoodsUneaten.size())))
                        .findFirst()
                        .ifPresent(itemKey -> {
                            Item food = BuiltInRegistries.ITEM.get(itemKey);
                            player.sendSystemMessage(
                                    Component.translatable("player.solcc.craving", food.getDefaultInstance().getDisplayName())
                                            .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
                        });
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Player entity = event.getEntity();
            if(entity instanceof ServerPlayer player && !entity.level().isClientSide()) {
                LOGGER.debug("onPlayerLoggedIn");
                if(player.hasData(SOLCC_DATA)) {
                    SolccData data = player.getData(SOLCC_DATA);
                    PacketDistributor.sendToPlayer(player, data);
                    LOGGER.debug("Sent data: {}", data.getUniqueFoodsEaten());
                }
            }
        }

        @SubscribeEvent
        public static void onLivingEntityUseItem(LivingEntityUseItemEvent.Finish event) {
            LivingEntity entity = event.getEntity();
            if(entity instanceof ServerPlayer player && !entity.level().isClientSide()) {
                LOGGER.debug("onLivingEntityUseItem");
                // Check if the player has eaten something and if so, update their progress
                ItemStack stack = event.getItem();
                ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
                FoodProperties props = stack.get(DataComponents.FOOD);
                if(props != null && SolccUtils.isValidFood(props)) {
                    LOGGER.debug("{} ate {}", player.getDisplayName(), itemKey);
                    SolccData data = player.getData(SOLCC_DATA);
                    Set<ResourceLocation> uniqueFoodsEaten = data.getUniqueFoodsEaten();
                    LOGGER.debug("Existing data: {}", uniqueFoodsEaten);
                    if(!uniqueFoodsEaten.contains(itemKey)) {

                        float uniqueSaturationOld = SolccUtils.getTotalSaturation(uniqueFoodsEaten.stream());

                        // Update unique foods
                        uniqueFoodsEaten.add(itemKey);
                        data.setUniqueFoodsEaten(uniqueFoodsEaten);
                        player.setData(SOLCC_DATA, data);
                        PacketDistributor.sendToPlayer(player, data);
                        LOGGER.debug("Sent updated data: {}", data.getUniqueFoodsEaten());

                        // Update health bonus
                        float uniqueSaturationCurrent = SolccUtils.getTotalSaturation(uniqueFoodsEaten.stream());
                        float uniqueSaturationMax = SolccUtils.getTotalSaturation(BuiltInRegistries.ITEM.stream().map(BuiltInRegistries.ITEM::getKey));
                        float milestoneSize = uniqueSaturationMax / (Config.MAX_BONUS_HEARTS.getAsInt() + 1);
                        double healthBonusOld = Math.floor(uniqueSaturationOld / milestoneSize) * 2;
                        double healthBonus = Math.floor(uniqueSaturationCurrent / milestoneSize) * 2;
                        LOGGER.debug("Saturation: {}/{}", uniqueSaturationCurrent, uniqueSaturationMax);
                        LOGGER.debug("Health bonus: +{}", healthBonus);
                        if(healthBonus > healthBonusOld) {
                            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 0.5f);
                            player.sendSystemMessage(
                                    Component.translatable("player.solcc.health_bonus")
                                            .withStyle(ChatFormatting.GOLD), false);
                        } else {
                            double progress = (uniqueSaturationCurrent % milestoneSize) / milestoneSize;
                            String progressPercent = String.format("%.1f", progress * 100);
                            player.sendSystemMessage(
                                    Component.translatable("player.solcc.progress", progressPercent)
                                            .withStyle(ChatFormatting.DARK_AQUA), false);
                        }

                        AttributeModifier modifier = new AttributeModifier(
                                healthBonusId,
                                healthBonus,
                                AttributeModifier.Operation.ADD_VALUE);

                        AttributeMap attributes = player.getAttributes();
                        AttributeInstance healthAttribute = attributes.getInstance(Attributes.MAX_HEALTH);
                        assert healthAttribute != null;
                        healthAttribute.addOrReplacePermanentModifier(modifier);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(SolccData.TYPE, SolccData.STREAM_CODEC, SpiceofLifeCarrotCakeClient::handleDataOnMain);
        }
    }
}
