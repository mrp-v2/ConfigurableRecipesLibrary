package mrp_v2.configurablerecipeslibrary.item.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Set;
import java.util.TreeSet;

public abstract class ConfigurableCraftingRecipe implements ICraftingRecipe
{
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

    protected abstract TreeSet<IngredientOverride> organizeOverrides(Set<IngredientOverride> overrides);

    @Override public ItemStack getCraftingResult(CraftingInventory inv)
    {
        return this.recipeOutput.copy();
    }

    @Override public ItemStack getRecipeOutput()
    {
        return this.recipeOutput;
    }

    @Override public abstract NonNullList<Ingredient> getIngredients();

    @Override public String getGroup()
    {
        return this.group;
    }

    @Override public ResourceLocation getId()
    {
        return this.id;
    }
}
