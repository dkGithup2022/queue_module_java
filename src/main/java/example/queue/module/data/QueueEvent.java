package example.queue.module.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.HashMap;

@JsonSerialize(using = QueueEventSerializer.class)
public abstract class QueueEvent implements Serializable {

    String queueName;

    HashMap<String, String> specs;

    protected QueueEvent(String queueName, HashMap<String, String> specs) {
        this.queueName = queueName;
        this.specs = specs;
    }

    @JsonProperty("queueName")
    public String queueName() {
        return queueName;
    }


    protected HashMap<String, String> getSpecs() {
        return specs;
    }

    @Override
    public String toString() {
        return "QueueEvent{" +
                "queueName='" + queueName + '\'' +
                ", specs=" + specs.toString() +
                '}';
    }
}
