package mrp_v2.configurablerecipeslibrary.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.function.Consumer;

class Util
{
    static void doForEachJsonObject(JsonArray json, Consumer<JsonObject> objectConsumer)
    {
        json.forEach((element) ->
        {
            if (!element.isJsonObject())
            {
                throw new JsonSyntaxException("Expected a JsonObject but got a " + element.getClass().getName());
            }
            objectConsumer.accept(element.getAsJsonObject());
        });
    }

    static String makeMissingJSONElementException(String element)
    {
        return "Missing JSON element '" + element + "'";
    }
}
