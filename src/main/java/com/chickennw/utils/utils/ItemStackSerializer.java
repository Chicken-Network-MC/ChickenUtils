package com.chickennw.utils.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> serialized = src.serialize();

        Gson simpleGson = new GsonBuilder().create();
        String json = simpleGson.toJson(serialized);

        return new JsonPrimitive(json);
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        String element = jsonElement.getAsString();

        Gson simpleGson = new GsonBuilder().create();
        Map<String, Object> map = simpleGson.fromJson(element, new TypeToken<Map<String, Object>>() {
        }.getType());

        return ItemStack.deserialize(map);
    }

    public static String toJson(ItemStack itemStack) {
        Map<String, Object> serialized = itemStack.serialize();
        Gson gson = new GsonBuilder().create();
        return gson.toJson(serialized);
    }

    public static ItemStack fromJson(String json) {
        Gson gson = new GsonBuilder().create();
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        return ItemStack.deserialize(map);
    }
}