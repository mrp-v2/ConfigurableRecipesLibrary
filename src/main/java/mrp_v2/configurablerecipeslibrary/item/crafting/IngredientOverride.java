package mrp_v2.configurablerecipeslibrary.item.crafting;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class IngredientOverride implements Comparable<IngredientOverride>
{
    public static Map<String, Supplier<Boolean>> conditionMap = Maps.newHashMap();
    protected final int priority;
    private final Supplier<Boolean> conditionSupplier;
    private final Map<Ingredient, Ingredient> ingredientOverrides;

    public IngredientOverride(int priority, Supplier<Boolean> conditionSupplier,
            Map<Ingredient, Ingredient> ingredientOverrides)
    {
        this.priority = priority;
        this.conditionSupplier = conditionSupplier;
        this.ingredientOverrides = ingredientOverrides;
    }

    public static Set<IngredientOverride> deserializeOverrides(JsonArray json)
    {
        Set<IngredientOverride> overrides = Sets.newHashSet();
        json.forEach((element) ->
        {
            if (!element.isJsonObject())
            {
                throw new JsonSyntaxException("Expected a JsonObject but got a " + element.getClass().getName());
            }
            JsonObject obj = element.getAsJsonObject();
            overrides.add(IngredientOverride.deserializeOverride(obj));
        });
        return overrides;
    }

    public static IngredientOverride deserializeOverride(JsonObject json)
    {
        int priority = JSONUtils.getInt(json, "priority", 0);
        String condition = JSONUtils.getString(json, "condition");
        Map<Ingredient, Ingredient> keyOverrides =
                IngredientOverride.deserializeIngredientOverrides(JSONUtils.getJsonArray(json, "ingredient_overrides"));
        return new IngredientOverride(priority, conditionMap.get(condition), keyOverrides);
    }

    public static Map<Ingredient, Ingredient> deserializeIngredientOverrides(JsonArray json)
    {
        Map<Ingredient, Ingredient> map = Maps.newHashMap();
        json.forEach((element) ->
        {
            if (!element.isJsonObject())
            {
                throw new JsonSyntaxException("Expected a JsonObject but got a " + element.getClass().getName());
            }
            JsonObject obj = element.getAsJsonObject();
            Ingredient original = Ingredient.deserialize(obj.get("original"));
            Ingredient replacement = Ingredient.deserialize(obj.get("replacement"));
            if (map.put(original, replacement) != null)
            {
                throw new JsonSyntaxException(
                        "Cannot have multiple replacement ingredients for the same original ingredient!");
            }
        });
        return map;
    }

    @Override public int compareTo(IngredientOverride o)
    {
        return this.priority - o.priority;
    }

    public int getPriority()
    {
        return this.priority;
    }

    public void apply(NonNullList<Ingredient> original)
    {
        if (conditionSupplier.get())
        {
            for (int i = 0; i < original.size(); i++)
            {
                if (this.ingredientOverrides.containsKey(original.get(i)))
                {
                    original.set(i, this.ingredientOverrides.get(original.get(i)));
                }
            }
        }
    }

    public Set<Ingredient> getOverriddenIngredients()
    {
        return this.ingredientOverrides.keySet();
    }
}
