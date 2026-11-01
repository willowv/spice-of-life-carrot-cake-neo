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

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue SATURATION_THRESHOLD = BUILDER
            .comment("Any food with saturation below this threshold does not count toward max health progress. Helpful for filtering out things like raw meat without having to individually specify them. The max here of 13 would make cooked steak not count, in case you only want very complex meals to be considered.")
            .defineInRange("saturationThreshold", 2d, 0d, 13d);

    public static final ModConfigSpec.IntValue MAX_BONUS_HEARTS = BUILDER
            .comment("This is the maximum amount of additional hearts the mod can grant. The bonus thresholds are calculated by dividing the total unique saturation of valid registered food items by this number + 1.")
            .defineInRange("maxBonusHearts", 10, 1, 20);

    public static final ModConfigSpec.BooleanValue INCLUDE_UNHEALTHY_FOOD = BUILDER
            .comment("Whether foods which have the potential to inflict negative effects on the player should contribute toward max health progress.")
            .define("includeUnhealthyFood", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
