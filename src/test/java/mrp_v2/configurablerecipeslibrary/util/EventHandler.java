package mrp_v2.configurablerecipeslibrary.util;

import mrp_v2.configurablerecipeslibrary.ConfigurableRecipesLibrary;
import mrp_v2.configurablerecipeslibrary.item.crafting.IngredientOverride;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = ConfigurableRecipesLibrary.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandler
{
    @SubscribeEvent public static void setup(final FMLCommonSetupEvent event)
    {
        IngredientOverride.addConditionMapping("replace_ingots_with_blocks", () -> true);
        IngredientOverride.addConditionMapping("replace_dusts_with_blocks", () -> false);
        IngredientOverride.addConditionMapping("replace_sand_with_red_sand", () -> false);
        IngredientOverride.addConditionMapping("replace_sand_with_sandstone", () -> true);
    }
}
