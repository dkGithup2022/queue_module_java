package example.queue.module.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class QueueEventSerializer extends JsonSerializer<QueueEvent> {

    @Override
    public void serialize(QueueEvent value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("queueName", value.queueName());

        gen.writeObjectFieldStart("specs");
        for (Map.Entry<String, String> entry : value.getSpecs().entrySet()) {
            gen.writeStringField(entry.getKey(), entry.getValue());
        }
        gen.writeEndObject();

        gen.writeEndObject();
    }
}
