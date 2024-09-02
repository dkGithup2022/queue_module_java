package example.queue.module.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.queue.module.RedisTestHelper;
import example.queue.module.api.QueueReader;
import example.queue.module.api.QueueWriter;
import example.queue.module.testData.LikeArticleAlarmEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisQueueReaderTest {

    @Autowired
    QueueReader queueReader;

    @Autowired
    QueueWriter queueAppender;

    @Autowired
    RedissonClient redisson;

    @Autowired
    RedisTestHelper redisHelper;

    @Autowired
    ObjectMapper objectMapper;


    LikeArticleAlarmEvent TEST_EVENT = LikeArticleAlarmEvent.of(1L, 1L);

    @BeforeEach
    void clearRedis() {
        redisHelper.clear();
    }

    @Test
    @DisplayName("큐 조회 - 1건")
    void readQueue() {
        readyOneEvent();

        var evts = queueReader.read(LikeArticleAlarmEvent.queue(), LikeArticleAlarmEvent.class, 0, -1);

        assertEquals(LikeArticleAlarmEvent.queue(), evts.getFirst().queueName());
        assertEquals(1L, ((LikeArticleAlarmEvent) evts.getFirst()).getArticleId());
        assertEquals(1L, ((LikeArticleAlarmEvent) evts.getFirst()).getUserId());


    }

    @Test
    @DisplayName("큐 조회 - 1건, 아무것도 없을 때")
    void readQueue_when_no_data() {
        var evts = queueReader.read(LikeArticleAlarmEvent.queue(), LikeArticleAlarmEvent.class, 0, -1);
        assertEquals(0, evts.size());
    }

    @Test
    @DisplayName("큐 pop - 1건")
    void popQueue() {
        readyOneEvent();
        var evts = queueReader.pop(LikeArticleAlarmEvent.queue(), LikeArticleAlarmEvent.class, 1);

        assertEquals(LikeArticleAlarmEvent.queue(), evts.getFirst().queueName());
        assertEquals(1L, ((LikeArticleAlarmEvent) evts.getFirst()).getArticleId());
        assertEquals(1L, ((LikeArticleAlarmEvent) evts.getFirst()).getUserId());



    }

    @Test
    @DisplayName("큐 pop - 3건")
    void popQueue_3_pop() {
        for (int i = 0; i < 10; i++) {
            readyOneEvent();
        }

        var evts = queueReader.pop(LikeArticleAlarmEvent.queue(), LikeArticleAlarmEvent.class, 3);

        assertEquals(3, evts.size());
    }

    @Test
    @DisplayName("큐 pop - 아무것도 없을때")
    void popQueue_when_no_data() {
        var evts = queueReader.pop(LikeArticleAlarmEvent.queue(), LikeArticleAlarmEvent.class, 3);

        assertEquals(0, evts.size());
    }


    @Test
    @DisplayName("큐 pop - 큐보다 요청 크기가 클때")
    void popQueue_when_small_data() {

        for (int i = 0; i < 10; i++) {
            readyOneEvent();
        }
        var evts = queueReader.pop(LikeArticleAlarmEvent.queue(), LikeArticleAlarmEvent.class, 100);

        assertEquals(10, evts.size());
    }


    void readyOneEvent() {
        queueAppender.append(TEST_EVENT);
    }

}