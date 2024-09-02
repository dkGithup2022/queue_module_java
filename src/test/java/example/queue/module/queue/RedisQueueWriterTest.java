package example.queue.module.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.queue.module.LuaScriptUtils;
import example.queue.module.RedisTestHelper;
import example.queue.module.api.QueueWriter;
import example.queue.module.data.QueueEvent;
import example.queue.module.testData.LikeArticleAlarmEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisQueueWriterTest {

    @Autowired
    QueueWriter queueAppender;

    @Autowired
    RedissonClient redisson;

    @Autowired
    RedisTestHelper redisHelper;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void clearRedis() {
        redisHelper.clear();
    }

    @Test
    void publishEvent_success() throws JsonProcessingException {
        var evt = LikeArticleAlarmEvent.of(1L, 1L);

        queueAppender.append(evt);

        var evts = read();

        assertEquals(1,  Long.valueOf(((LikeArticleAlarmEvent) evts.getFirst() ).getArticleId()));
        assertEquals(1,  Long.valueOf(((LikeArticleAlarmEvent) evts.getFirst() ).getUserId()));

    }


    public List<QueueEvent> read() throws JsonProcessingException {
        var script = redisson.getScript(StringCodec.INSTANCE);
        List<String> result = script.eval(RScript.Mode.READ_ONLY, luaScript, RScript.ReturnType.MULTI, java.util.List.of(LikeArticleAlarmEvent.queue()));

        List<QueueEvent> evts = new ArrayList<>();
        for (String piece : result) {
            evts.add(objectMapper.readValue(LuaScriptUtils.makeReadable(piece), LikeArticleAlarmEvent.class));
        }

        return evts;
    }

    String luaScript = """
            local listKey = KEYS[1];
            local current = redis.call('LRANGE', 'LIKE_ARTICLE_ALARM_EVENT', 0, -1)
            return current;
            """;

}