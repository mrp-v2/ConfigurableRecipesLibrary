package mrp_v2.configurablerecipeslibrary.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mrp_v2.configurablerecipeslibrary.util.Util;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

class IngredientOverride implements Comparable<IngredientOverride>
{
    private static final String OVERRIDES_KEY = "overrides";
    private static final String ORIGINAL_KEY = "original";
    private static final String CONDITION_KEY = "condition";
    private static final String REPLACEMENT_KEY = "replacement";
    private static final String PRIORITY_KEY = "priority";
    private final int priority;
    private final Supplier<Boolean> conditionSupplier;
    private final EquatableMap<Ingredient, Ingredient> ingredientOverrides;

    private IngredientOverride(int priority, Supplier<Boolean> conditionSupplier,
            EquatableMap<Ingredient, Ingredient> ingredientOverrides)
    {
        this.priority = priority;
        this.conditionSupplier = conditionSupplier;
        this.ingredientOverrides = ingredientOverrides;
    }

    static Set<IngredientOverride> getOverridesFromJson(JsonObject json)
    {
        return json.has(OVERRIDES_KEY) ?
                IngredientOverride.deserializeOverrides(JSONUtils.getJsonArray(json, OVERRIDES_KEY)) :
                new HashSet<>();
    }

    static Set<IngredientOverride> deserializeOverrides(JsonArray json)
    {
        Set<IngredientOverride> overrides = new HashSet<>();
        Util.doForEachJsonObject(json, (obj) -> overrides.add(IngredientOverride.deserializeOverride(obj)));
        return overrides;
    }

    private static IngredientOverride deserializeOverride(JsonObject json)
    {
        if (!json.has(CONDITION_KEY))
        {
            throw new JsonSyntaxException(Util.makeMissingJSONElementException(CONDITION_KEY));
        }
        if (!json.has(OVERRIDES_KEY))
        {
            throw new JsonSyntaxException(Util.makeMissingJSONElementException(OVERRIDES_KEY));
        }
        JsonElement overrides = json.get(OVERRIDES_KEY);
        if (!overrides.isJsonArray())
        {
            throw new JsonSyntaxException("Expected an array but got a " + overrides.getClass().getName());
        }
        return new IngredientOverride(JSONUtils.getInt(json, PRIORITY_KEY, 0),
                ConditionBuilder.build(JSONUtils.getString(json, CONDITION_KEY)),
                IngredientOverride.deserializeIngredientOverrides(overrides.getAsJsonArray()));
    }

    private static EquatableMap<Ingredient, Ingredient> deserializeIngredientOverrides(JsonArray json)
    {
        EquatableMap<Ingredient, Ingredient> map = new EquatableMap<>(IngredientOverride::ingredientsEqual);
        Util.doForEachJsonObject(json, (obj) ->
        {
            Set<Ingredient> originals;
            if (!obj.has(ORIGINAL_KEY))
            {
                throw new JsonSyntaxException(Util.makeMissingJSONElementException(ORIGINAL_KEY));
            }
            JsonElement element = obj.get(ORIGINAL_KEY);
            if (element.isJsonObject())
            {
                originals = new HashSet<>(1);
                originals.add(Ingredient.deserialize(element));
            } else if (element.isJsonArray())
            {
                originals = deserializeIngredientList(element.getAsJsonArray());
            } else
            {
                throw new JsonSyntaxException("Expected an object or array but got a " + element.getClass().getName());
            }
            if (!obj.has(REPLACEMENT_KEY))
            {
                throw new JsonSyntaxException(Util.makeMissingJSONElementException(REPLACEMENT_KEY));
            }
            Ingredient replacement = Ingredient.deserialize(obj.get(REPLACEMENT_KEY));
            originals.forEach((original) ->
            {
                if (map.put(original, replacement) != null)
                {
                    throw new JsonSyntaxException(
                            "Cannot have multiple replacement ingredients for the same original ingredient!");
                }
            });
        });
        return map;
    }

    private static Set<Ingredient> deserializeIngredientList(JsonArray json)
    {
        Set<Ingredient> ingredients = new HashSet<>();
        Util.doForEachJsonObject(json, (obj) -> ingredients.add(Ingredient.deserialize(obj)));
        return ingredients;
    }

    private static boolean ingredientsEqual(Ingredient a, Ingredient b)
    {
        return a.serialize().toString().equals(b.serialize().toString());
    }

    @Override public int compareTo(IngredientOverride o)
    {
        return this.priority - o.priority;
    }

    int getPriority()
    {
        return this.priority;
    }

    void apply(NonNullList<Ingredient> original)
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

    Set<Ingredient> getOverriddenIngredients()
    {
        return this.ingredientOverrides.keySet();
    }
}
