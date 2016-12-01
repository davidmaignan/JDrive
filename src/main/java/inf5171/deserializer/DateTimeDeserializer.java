package inf5171.deserializer;

import com.google.api.client.util.DateTime;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by david on 2016-05-22.
 */
public class DateTimeDeserializer implements JsonDeserializer<DateTime> {
    @Override
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return new DateTime(json.getAsJsonPrimitive().getAsString());
    }
}
