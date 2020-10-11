package mrp_v2.configurablerecipeslibrary.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mrp_v2.configurablerecipeslibrary.item.crafting.ConfigurableShapedRecipe;
import mrp_v2.configurablerecipeslibrary.item.crafting.IngredientOverride;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurableShapedRecipeBuilder extends ShapedRecipeBuilder
{
    final Set<DataGenIngredientOverride> overrides;

    public ConfigurableShapedRecipeBuilder(IItemProvider resultIn, int countIn)
    {
        super(resultIn, countIn);
        this.overrides = new HashSet<>();
    }

    public DataGenIngredientOverride.Builder addOverride(String condition)
    {
        return new DataGenIngredientOverride.Builder(this, condition);
    }

    public class Result extends ShapedRecipeBuilder.Result
    {
        private final Set<DataGenIngredientOverride> overrides;

        public Result(ResourceLocation idIn, Item resultIn, int countIn, String groupIn, List<String> patternIn,
                Map<Character, Ingredient> keyIn, Advancement.Builder advancementBuilderIn,
                ResourceLocation advancementIdIn, Set<DataGenIngredientOverride> overrides)
        {
            super(idIn, resultIn, countIn, groupIn, patternIn, keyIn, advancementBuilderIn, advancementIdIn);
            this.overrides = overrides;
        }

        @Override public void serialize(JsonObject json)
        {
            super.serialize(json);
            if (overrides.size() < 1)
            {
                LogManager.getLogger().warn("Recipe " + getID() + " is configurable, but does not have any overrides!");
            }
            JsonArray overridesArray = new JsonArray();
            for (DataGenIngredientOverride override : this.overrides)
            {
                overridesArray.add(override.serialize());
            }
            json.add(IngredientOverride.OVERRIDES_KEY, overridesArray);
        }

        @Override public IRecipeSerializer<?> getSerializer()
        {
            return ConfigurableShapedRecipe.Serializer.INSTANCE;
        }
    }
}
