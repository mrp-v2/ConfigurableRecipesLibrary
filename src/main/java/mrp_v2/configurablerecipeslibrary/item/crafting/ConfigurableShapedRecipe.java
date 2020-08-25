package mrp_v2.configurablerecipeslibrary.item.crafting;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mrp_v2.configurablerecipeslibrary.ConfigurableRecipesLibrary;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.*;

public class ConfigurableShapedRecipe extends ConfigurableCraftingRecipe implements IShapedRecipe<CraftingInventory>
{
    public static int MAX_WIDTH = 3;
    public static int MAX_HEIGHT = 3;
    protected final int recipeWidth;
    protected final int recipeHeight;

    protected ConfigurableShapedRecipe(ResourceLocation id, String group, ItemStack recipeOutput,
            NonNullList<Ingredient> recipeItems, int recipeWidth, int recipeHeight)
    {
        this(id, group, recipeOutput, recipeItems, recipeWidth, recipeHeight, Sets.newHashSet());
    }

    protected ConfigurableShapedRecipe(ResourceLocation id, String group, ItemStack recipeOutput,
            NonNullList<Ingredient> recipeItems, int recipeWidth, int recipeHeight, Set<IngredientOverride> overrides)
    {
        super(id, group, recipeOutput, recipeItems, overrides);
        this.recipeWidth = recipeWidth;
        this.recipeHeight = recipeHeight;
    }

    protected static Map<String, Ingredient> deserializeKey(JsonObject json)
    {
        Map<String, Ingredient> map = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : json.entrySet())
        {
            if (entry.getKey().length() != 1)
            {
                throw new JsonSyntaxException(
                        "Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey()))
            {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }
            map.put(entry.getKey(), Ingredient.deserialize(entry.getValue()));
        }
        map.put(" ", Ingredient.EMPTY);
        return map;
    }

    protected static String[] patternFromJson(JsonArray jsonArr)
    {
        String[] strings = new String[jsonArr.size()];
        if (strings.length > MAX_HEIGHT)
        {
            throw new JsonSyntaxException("Invalid pattern: too many rows, " + MAX_HEIGHT + " is maximum");
        } else if (strings.length == 0)
        {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        } else
        {
            for (int i = 0; i < strings.length; ++i)
            {
                String s = JSONUtils.getString(jsonArr.get(i), "pattern[" + i + "]");
                if (s.length() > MAX_WIDTH)
                {
                    throw new JsonSyntaxException("Invalid pattern: too many columns, " + MAX_WIDTH + " is maximum");
                }
                if (i > 0 && strings[0].length() != s.length())
                {
                    throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                }
                strings[i] = s;
            }
            return strings;
        }
    }

    static String[] shrink(String... toShrink)
    {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;
        for (int i1 = 0; i1 < toShrink.length; ++i1)
        {
            String s = toShrink[i1];
            i = Math.min(i, firstNonSpace(s));
            int j1 = lastNonSpace(s);
            j = Math.max(j, j1);
            if (j1 < 0)
            {
                if (k == i1)
                {
                    ++k;
                }
                ++l;
            } else
            {
                l = 0;
            }
        }
        if (toShrink.length == l)
        {
            return new String[0];
        } else
        {
            String[] strings = new String[toShrink.length - l - k];
            for (int k1 = 0; k1 < strings.length; ++k1)
            {
                strings[k1] = toShrink[k1 + k].substring(i, j + 1);
            }
            return strings;
        }
    }

    private static int firstNonSpace(String str)
    {
        int i;
        for (i = 0; i < str.length() && str.charAt(i) == ' '; ++i)
        {
        }
        return i;
    }

    private static int lastNonSpace(String str)
    {
        int i;
        for (i = str.length() - 1; i >= 0 && str.charAt(i) == ' '; --i)
        {
        }
        return i;
    }

    protected static NonNullList<Ingredient> deserializeIngredients(String[] pattern, Map<String, Ingredient> keys,
            int patternWidth, int patternHeight)
    {
        NonNullList<Ingredient> ingredients = NonNullList.withSize(patternWidth * patternHeight, Ingredient.EMPTY);
        Set<String> strings = Sets.newHashSet(keys.keySet());
        strings.remove(" ");
        for (int i = 0; i < pattern.length; ++i)
        {
            for (int j = 0; j < pattern[i].length(); ++j)
            {
                String s = pattern[i].substring(j, j + 1);
                Ingredient ingredient = keys.get(s);
                if (ingredient == null)
                {
                    throw new JsonSyntaxException(
                            "Pattern references symbol '" + s + "' but it's not defined in the key");
                }
                strings.remove(s);
                ingredients.set(j + patternWidth * i, ingredient);
            }
        }
        if (!strings.isEmpty())
        {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + strings);
        } else
        {
            return ingredients;
        }
    }

    @Override public int getRecipeWidth()
    {
        return this.recipeWidth;
    }

    @Override public int getRecipeHeight()
    {
        return this.recipeHeight;
    }

    @Override public boolean matches(CraftingInventory inv, World worldIn)
    {
        return false;
    }

    @Override public boolean canFit(int width, int height)
    {
        return false;
    }

    @Override public IRecipeSerializer<?> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    @Override protected TreeSet<IngredientOverride> organizeOverrides(Set<IngredientOverride> overrides)
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

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
            implements IRecipeSerializer<ConfigurableShapedRecipe>
    {
        public static final Serializer INSTANCE = new Serializer();
        private static final String OVERRIDES_KEY = "overrides";

        private Serializer()
        {
            this.setRegistryName(ConfigurableRecipesLibrary.ID, "crafting_shaped_configurable");
        }

        @Override public ConfigurableShapedRecipe read(ResourceLocation recipeId, JsonObject json)
        {
            String group = JSONUtils.getString(json, "group", "");
            Map<String, Ingredient> key = ConfigurableShapedRecipe.deserializeKey(JSONUtils.getJsonObject(json, "key"));
            String[] pattern = ConfigurableShapedRecipe.shrink(
                    ConfigurableShapedRecipe.patternFromJson(JSONUtils.getJsonArray(json, "pattern")));
            int patternWidth = pattern[0].length();
            int patternHeight = pattern.length;
            NonNullList<Ingredient> ingredients =
                    ConfigurableShapedRecipe.deserializeIngredients(pattern, key, patternWidth, patternHeight);
            ItemStack recipeOutput = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            Set<IngredientOverride> overrides = json.has(OVERRIDES_KEY) ?
                    IngredientOverride.deserializeOverrides(JSONUtils.getJsonArray(json, OVERRIDES_KEY)) :
                    Sets.newHashSet();
            return new ConfigurableShapedRecipe(recipeId, group, recipeOutput, ingredients, patternWidth, patternHeight,
                    overrides);
        }

        @Nullable @Override public ConfigurableShapedRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            int recipeWidth = buffer.readVarInt();
            int recipeHeight = buffer.readVarInt();
            String group = buffer.readString(32767);
            NonNullList<Ingredient> recipeItems = NonNullList.withSize(recipeWidth * recipeHeight, Ingredient.EMPTY);
            for (int k = 0; k < recipeItems.size(); ++k)
            {
                recipeItems.set(k, Ingredient.read(buffer));
            }
            ItemStack recipeOutput = buffer.readItemStack();
            return new ConfigurableShapedRecipe(recipeId, group, recipeOutput, recipeItems, recipeWidth, recipeHeight);
        }

        @Override public void write(PacketBuffer buffer, ConfigurableShapedRecipe recipe)
        {
            buffer.writeVarInt(recipe.recipeWidth);
            buffer.writeVarInt(recipe.recipeHeight);
            buffer.writeString(recipe.group);
            for (Ingredient ingredient : recipe.getIngredients())
            {
                ingredient.write(buffer);
            }
            buffer.writeItemStack(recipe.recipeOutput);
        }
    }
}
