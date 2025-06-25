package dev.mrsnowy.teleport_commands.utils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.*;

public class HashMapSerde<V> implements JsonDeserializer<HashMap<String, V>>, JsonSerializer<HashMap<String, V>> {
    private final String keyMember;
    private final Type ValueType;

    public HashMapSerde(String keyMember, Class<V> valueType) {
        this.keyMember = keyMember;
        this.ValueType = valueType;
    }

    @Override
    public HashMap<String, V> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        HashMap<String, V> output = new HashMap<>();

        if (!json.isJsonArray()) {
            return output;
        }
        Iterator<JsonElement> jsonIterator = json.getAsJsonArray().iterator();
        while (jsonIterator.hasNext()) {
            JsonElement jsonElement = jsonIterator.next();
            if (!jsonElement.isJsonObject())
                continue;
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            String key = jsonObj.get(this.keyMember).getAsString(); // don't delete the key!
            output.put(key, context.deserialize(jsonObj, ValueType));
        }

        return output;
    }

    @Override
    public JsonElement serialize(HashMap<String, V> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray output = new JsonArray(src.size());
        Iterator<V> valueIterator = src.values().iterator(); // we can ignore the key because we kept it in the value
                                                             // when deserializing
        while (valueIterator.hasNext()) {
            output.add(context.serialize(valueIterator.next(), ValueType));
        }
        return output;
    }
}
