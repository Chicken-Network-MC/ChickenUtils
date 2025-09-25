package com.chickennw.utils.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        String json = GSON.toJson(src.serialize());

        if (json.startsWith("\"")) json = json.substring(1);
        if (json.endsWith("\"")) json = json.substring(0, json.length() - 1);

        return new JsonPrimitive(json);
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        String element = jsonElement.getAsString();

        if (element.startsWith("\"")) element = element.substring(1);
        if (element.endsWith("\"")) element = element.substring(0, element.length() - 1);

        Map<String, Object> map = GSON.fromJson(element, new TypeToken<Map<String, Object>>() {
        }.getType());
        return ItemStack.deserialize(map);
    }

    public static String toJson(ItemStack itemStack) {
        return GSON.toJson(itemStack);
    }

    public static ItemStack fromJson(String json) {
        return GSON.fromJson(json, ItemStack.class);
    }
}
