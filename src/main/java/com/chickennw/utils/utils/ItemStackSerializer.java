package com.chickennw.utils.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

@SuppressWarnings("unused")
public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    private static final Gson GSON = new GsonBuilder().create();

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(GSON.toJson(src.serialize()));
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> map = GSON.fromJson(jsonElement.getAsString(), new TypeToken<Map<String, Object>>() {}.getType());
        return ItemStack.deserialize(map);
    }

    public static String toJson(ItemStack itemStack) {
        return GSON.toJson(itemStack.serialize());
    }

    public static ItemStack fromJson(String json) {
        Map<String, Object> map = GSON.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
        return ItemStack.deserialize(map);
    }
}