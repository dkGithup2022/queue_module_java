package example.queue.module.counter;

import example.queue.module.RedisTestHelper;
import example.queue.module.api.CounterWriter;
import example.queue.module.data.CounterEvent;
import example.queue.module.testData.ArticleViewUpEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class RedisCounterWriterTest {


    @Autowired
    CounterWriter countWriter;

    @Autowired
    RedissonClient redisson;

    @Autowired
    RedisTestHelper redisHelper;

    @BeforeEach
    void clearAll() {
        redisHelper.clear();
    }

    @Test
    @DisplayName("성공- 아무것도 없을때, 이벤트 카운터 증가 1")
    void write_count_success() {
        var evt = ArticleViewUpEvent.newEvent( 1L);
        countWriter.countUpEvent(evt, 1);
        // 해당 필드의 값을 가져오기
        assertEquals(1, read(evt));
    }

    @Test
    @DisplayName("성공-  기존 이벤트 카운터 증가-> 두번하기  2")
    void write_count_success_02() {
        var evt = ArticleViewUpEvent.newEvent( 1L);
        countWriter.countUpEvent(evt, 1);
        countWriter.countUpEvent(evt, 1);

        // 해당 필드의 값을 가져오기
        assertEquals(2, read(evt));
    }



    public Long read(CounterEvent  event) {
        var script = redisson.getScript(StringCodec.INSTANCE);

        var bucket =  event.bucket();
        var field = event.field();
        return script.eval(RScript.Mode.READ_ONLY, luaScript, RScript.ReturnType.VALUE, Collections.singletonList(bucket), field);
    }

    String luaScript = """
            local hashKey = KEYS[1];
            local field = ARGV[1];
            local current = tonumber(redis.call('HGET', hashKey, field));
            return current;
            """;
}