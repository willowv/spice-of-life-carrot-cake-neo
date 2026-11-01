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

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SolccData implements INBTSerializable<CompoundTag>, CustomPacketPayload {
    public static final Type<SolccData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpiceofLifeCarrotCake.MOD_ID, "solcc_data"));
    public static final StreamCodec<ByteBuf, SolccData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(SolccData::fromNBT, SolccData::toNBT);

    private Set<ResourceLocation> uniqueFoodsEaten = new HashSet<>();

    public Set<ResourceLocation> getUniqueFoodsEaten() {
        return uniqueFoodsEaten;
    }

    public void setUniqueFoodsEaten(Set<ResourceLocation> uniqueFoodsEaten) {
        this.uniqueFoodsEaten = uniqueFoodsEaten;
    }

    private CompoundTag toNBT() {
        List<StringTag> list = uniqueFoodsEaten.stream()
                .map(ResourceLocation::toString)
                .map(StringTag::valueOf)
                .toList();
        ListTag listTag = new ListTag();
        listTag.addAll(list);
        CompoundTag tag = new CompoundTag();
        tag.put("uniqueFoodsEaten", listTag);
        return tag;
    }

    private static SolccData fromNBT(CompoundTag compoundTag) {
        SolccData data = new SolccData();
        ListTag listTag = compoundTag.getList("uniqueFoodsEaten", Tag.TAG_STRING);
        data.uniqueFoodsEaten = listTag.stream().parallel()
                .map(Tag::getAsString)
                .map(ResourceLocation::parse)
                .collect(Collectors.toSet());
        return data;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return toNBT();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        this.uniqueFoodsEaten = fromNBT(compoundTag).getUniqueFoodsEaten();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
