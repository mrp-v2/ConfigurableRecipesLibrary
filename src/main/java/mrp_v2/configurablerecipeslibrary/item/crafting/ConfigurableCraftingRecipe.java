package mrp_v2.configurablerecipeslibrary.item.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public abstract class ConfigurableCraftingRecipe implements ICraftingRecipe
{
    static final Map<String, ConditionBuilder.BooleanSupplier> conditionMap =
            Collections.synchronizedMap(new HashMap<>());
    public static int MAX_WIDTH = 3;
    public static int MAX_HEIGHT = 3;
    protected final ResourceLocation id;
    protected final String group;
    protected final ItemStack recipeOutput;
    protected final NonNullList<Ingredient> recipeItems;
    protected final TreeSet<IngredientOverride> overrides;

    protected ConfigurableCraftingRecipe(ResourceLocation id, String group, ItemStack recipeOutput,
            NonNullList<Ingredient> recipeItems, Set<IngredientOverride> overrides)
    {
        this.id = id;
        this.group = group;
        this.recipeOutput = recipeOutput;
        this.recipeItems = recipeItems;
        this.overrides = this.organizeOverrides(overrides);
    }

    private TreeSet<IngredientOverride> organizeOverrides(Set<IngredientOverride> overrides)
    {
        HashMap<Integer, HashSet<IngredientOverride>> overridesByPriority = new HashMap<>();
        for (IngredientOverride override : overrides)
        {
            if (overridesByPriority.containsKey(override.getPriority()))
            {
                overridesByPriority.get(override.getPriority()).add(override);
            } else
            {
                HashSet<IngredientOverride> newSet = new HashSet<>();
                newSet.add(override);
                overridesByPriority.put(override.getPriority(), newSet);
            }
        }
        for (int priority : overridesByPriority.keySet())
        {
            HashSet<Ingredient> foundKeys = new HashSet<>();
            for (IngredientOverride override : overridesByPriority.get(priority))
            {
                for (Ingredient ingredient : override.getOverriddenIngredients())
                {
                    if (!foundKeys.add(ingredient))
                    {
                        throw new RuntimeException(
                                "Cannot have multiple IngredientOverrides with the same priority that modify the same Ingredient!");
                    }
                }
            }
        }
        return new TreeSet<>(overrides);
    }

    /**
     * The Map being modified is thread-safe, making this method thread-safe.
     *
     * @param condition The id of the condition as used in recipe JSONs.
     * @param mapping The {@literal Supplier<Boolean>} for the condition.
     *
     * @return True if the condition mapping was successfully added.
     */
    public static boolean addConditionMapping(String condition, ConditionBuilder.BooleanSupplier mapping)
    {
        return ConfigurableCraftingRecipe.conditionMap.putIfAbsent(condition, mapping) == null;
    }

    @Override public ItemStack getCraftingResult(CraftingInventory inv)
    {
        return this.recipeOutput.copy();
    }

    @Override public ItemStack getRecipeOutput()
    {
        return this.recipeOutput;
    }

    @Override public NonNullList<Ingredient> getIngredients()
    {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(this.recipeItems);
        for (IngredientOverride override : this.overrides)
        {
            override.apply(ingredients);
        }
        return ingredients;
    }

    @Override public String getGroup()
    {
        return this.group;
    }

    @Override public ResourceLocation getId()
    {
        return this.id;
    }
}
