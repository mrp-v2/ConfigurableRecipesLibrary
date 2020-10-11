package mrp_v2.configurablerecipeslibrary.datagen;

import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.util.IItemProvider;

public class ConfigurableShapelessRecipeBuilder extends ShapelessRecipeBuilder
{
    public ConfigurableShapelessRecipeBuilder(IItemProvider resultIn, int countIn)
    {
        super(resultIn, countIn);
    }
}
