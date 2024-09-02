package example.queue.module.testData;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import example.queue.module.data.QueueEvent;

import java.util.HashMap;


public class LikeArticleAlarmEvent extends QueueEvent {


    private static String QUEUE = "LIKE_ARTICLE_ALARM_EVENT";

    public static String queue() {
        return QUEUE;
    }


    @JsonCreator
    protected LikeArticleAlarmEvent(
            @JsonProperty("queueName")  String queueName,
            @JsonProperty("specs")HashMap<String, String> specs) {
        super(queueName, specs);
    }

    public static LikeArticleAlarmEvent of(Long userId, Long articleId) {
        var specs = new HashMap<String, String>();
        specs.put("userId", String.valueOf(userId));
        specs.put("articleId", String.valueOf(articleId));

        return new LikeArticleAlarmEvent(QUEUE, specs);
    }

    public Long getUserId(){
        return Long.valueOf(super.getSpecs().get("userId"));
    }

    public Long getArticleId(){
        return Long.valueOf(super.getSpecs().get("articleId"));
    }

}
