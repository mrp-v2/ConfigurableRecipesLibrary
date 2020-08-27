package mrp_v2.configurablerecipeslibrary.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mrp_v2.configurablerecipeslibrary.item.crafting.util.EquatableMap;
import mrp_v2.configurablerecipeslibrary.util.Util;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;

import java.util.*;
import java.util.function.Supplier;

public class IngredientOverride implements Comparable<IngredientOverride>
{
    /**
     * A map of <c>String</c> values used as the <c>condition</c> of recipe JSONs to the <c>Supplier{@literal <Boolean>}</c> each string indicates.
     * All mappings need to be added before recipes are loaded. This can be done from {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}.
     * This map is thread safe.
     */
    private static final Map<String, Supplier<Boolean>> conditionMap = Collections.synchronizedMap(new HashMap<>());
    private static final String OVERRIDES_KEY = "overrides";
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

    /**
     * @param condition The id of the condition as used in recipe JSONs.
     * @param mapping The {@literal Supplier<Boolean>} for the condition.
     *
     * @return True if the condition mapping was successfully added.
     */
    public static boolean addConditionMapping(String condition, Supplier<Boolean> mapping)
    {
        return conditionMap.putIfAbsent(condition, mapping) == null;
    }

    public static Set<IngredientOverride> getOverridesFromJson(JsonObject json)
    {
        return json.has(OVERRIDES_KEY) ?
                IngredientOverride.deserializeOverrides(JSONUtils.getJsonArray(json, OVERRIDES_KEY)) :
                new HashSet<>();
    }

    public static Set<IngredientOverride> deserializeOverrides(JsonArray json)
    {
        Set<IngredientOverride> overrides = new HashSet<>();
        Util.doForEachJsonObject(json, (obj) -> overrides.add(IngredientOverride.deserializeOverride(obj)));
        return overrides;
    }

    private static IngredientOverride deserializeOverride(JsonObject json)
    {
        String condition = JSONUtils.getString(json, "condition");
        if (!conditionMap.containsKey(condition))
        {
            throw new IllegalStateException("No Supplier<Boolean> exists for condition '" + condition + "'");
        }
        return new IngredientOverride(JSONUtils.getInt(json, "priority", 0), conditionMap.get(condition),
                IngredientOverride.deserializeIngredientOverrides(JSONUtils.getJsonArray(json, "overrides")));
    }

    private static EquatableMap<Ingredient, Ingredient> deserializeIngredientOverrides(JsonArray json)
    {
        EquatableMap<Ingredient, Ingredient> map = new EquatableMap<>(IngredientOverride::ingredientsEqual);
        Util.doForEachJsonObject(json, (obj) ->
        {
            Set<Ingredient> originals = deserializeIngredientList(JSONUtils.getJsonArray(obj, "originals"));
            Ingredient replacement = Ingredient.deserialize(obj.get("replacement"));
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
