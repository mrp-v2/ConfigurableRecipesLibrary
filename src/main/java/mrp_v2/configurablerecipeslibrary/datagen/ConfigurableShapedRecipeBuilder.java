package mrp_v2.configurablerecipeslibrary.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mrp_v2.configurablerecipeslibrary.item.crafting.ConfigurableShapedRecipe;
import mrp_v2.configurablerecipeslibrary.item.crafting.IngredientOverride;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ConfigurableShapedRecipeBuilder extends ShapedRecipeBuilder
{
    final Set<DataGenIngredientOverride> overrides;

    public ConfigurableShapedRecipeBuilder(IItemProvider resultIn, int countIn)
    {
        super(resultIn, countIn);
        this.overrides = new HashSet<>();
    }

    public static ConfigurableShapedRecipeBuilder configurableShapedRecipe(IItemProvider resultIn)
    {
        return configurableShapedRecipe(resultIn, 1);
    }

    public static ConfigurableShapedRecipeBuilder configurableShapedRecipe(IItemProvider resultIn, int countIn)
    {
        return new ConfigurableShapedRecipeBuilder(resultIn, countIn);
    }

    public DataGenIngredientOverride.Builder<ConfigurableShapedRecipeBuilder> addOverride(String condition)
    {
        return new DataGenIngredientOverride.Builder<>(this, this.overrides, condition);
    }

    @Override public ConfigurableShapedRecipeBuilder key(Character symbol, ITag<Item> tagIn)
    {
        super.key(symbol, tagIn);
        return this;
    }

    @Override public ConfigurableShapedRecipeBuilder key(Character symbol, IItemProvider itemIn)
    {
        super.key(symbol, itemIn);
        return this;
    }

    @Override public ConfigurableShapedRecipeBuilder key(Character symbol, Ingredient ingredientIn)
    {
        super.key(symbol, ingredientIn);
        return this;
    }

    @Override public ConfigurableShapedRecipeBuilder patternLine(String patternIn)
    {
        super.patternLine(patternIn);
        return this;
    }

    @Override public ConfigurableShapedRecipeBuilder addCriterion(String name, ICriterionInstance criterionIn)
    {
        super.addCriterion(name, criterionIn);
        return this;
    }

    @Override public ConfigurableShapedRecipeBuilder setGroup(String groupIn)
    {
        super.setGroup(groupIn);
        return this;
    }

    /**
     * Copied from {@link ShapedRecipeBuilder#build(Consumer, ResourceLocation)}.
     * Modified to use {@link Result} instead of {@link ShapedRecipeBuilder.Result}.
     */
    @Override public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id)
    {
        this.validate(id);
        this.advancementBuilder.withParentId(new ResourceLocation("recipes/root"))
                .withCriterion("has_the_recipe", RecipeUnlockedTrigger.create(id))
                .withRewards(AdvancementRewards.Builder.recipe(id))
                .withRequirementsStrategy(IRequirementsStrategy.OR);
        consumerIn.accept(
                new Result(id, this.result, this.count, this.group == null ? "" : this.group, this.pattern, this.key,
                        this.advancementBuilder, new ResourceLocation(id.getNamespace(),
                        "recipes/" + this.result.getGroup().getPath() + "/" + id.getPath()), this.overrides));
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
