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
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;

import static dev.willowv.solcc.SpiceofLifeCarrotCake.SOLCC_DATA;
import static dev.willowv.solcc.SolccUtils.getUneatenFoods;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = SpiceofLifeCarrotCake.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = SpiceofLifeCarrotCake.MOD_ID, value = Dist.CLIENT)
public class SpiceofLifeCarrotCakeClient {
    public SpiceofLifeCarrotCakeClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        SpiceofLifeCarrotCake.LOGGER.debug("Initialized client");
    }

    // Payload handler
    public static void handleDataOnMain(final SolccData data, final IPayloadContext context) {
        Player localPlayer = context.player();
        localPlayer.setData(SOLCC_DATA, data);
        SpiceofLifeCarrotCake.LOGGER.debug("Received data: {}", data.getUniqueFoodsEaten());
    }

    @SubscribeEvent
    static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player localPlayer = event.getEntity();
        if(localPlayer != null && stack.get(DataComponents.FOOD) != null) {
            ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
            SolccData data = localPlayer.getData(SOLCC_DATA);
            Set<ResourceLocation> uneatenFoods = getUneatenFoods(data.getUniqueFoodsEaten());
            if (uneatenFoods.contains(itemKey)) {
                event.getToolTip().add(1,
                        Component.translatable("item.tooltip.solcc.never_eaten").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
            }
        }
    }
}
