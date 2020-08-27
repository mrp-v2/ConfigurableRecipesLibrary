package mrp_v2.configurablerecipeslibrary.util;

import mrp_v2.configurablerecipeslibrary.ConfigurableRecipesLibrary;
import mrp_v2.configurablerecipeslibrary.item.crafting.ConfigurableShapedRecipe;
import mrp_v2.configurablerecipeslibrary.item.crafting.ConfigurableShapelessRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ConfigurableRecipesLibrary.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler
{
    @SubscribeEvent
    public static void registerRecipeSerializers(final RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry()
                .registerAll(ConfigurableShapedRecipe.Serializer.INSTANCE,
                        ConfigurableShapelessRecipe.Serializer.INSTANCE);
    }
}
