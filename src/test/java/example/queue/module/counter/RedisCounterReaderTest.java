package example.queue.module.counter;

import example.queue.module.RedisTestHelper;
import example.queue.module.api.CounterReader;
import example.queue.module.api.CounterWriter;
import example.queue.module.testData.ArticleViewUpEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class RedisCounterReaderTest {


    @Autowired
    CounterReader countReader;

    @Autowired
    CounterWriter writer;

    @Autowired
    RedissonClient redisson;

    @Autowired
    RedisTestHelper redisHelper;


    @BeforeEach
    void clearAll() {
        redisHelper.clear();
    }

    @Test
    void read_on_empty_bucket() {
        var pairs = countReader.popBucket("NOT_EXIST_BUCKET");

        assertEquals(0, pairs.size());
    }


    @Test
    void read_single_field_bucket() {

        String field = "1";

        var evt1 = ArticleViewUpEvent.newEvent(1L);
        var evt2 = ArticleViewUpEvent.newEvent(1L);
        var evt3 = ArticleViewUpEvent.newEvent(1L);

        writer.countUpEvent(evt1, 1);
        writer.countUpEvent(evt2, 1);
        writer.countUpEvent(evt3, 1);

        var pairs = countReader.popBucket(ArticleViewUpEvent.eventbucket());
        assertEquals(1, pairs.size());
        assertEquals(field, pairs.getFirst().field());
        assertEquals(3, pairs.getFirst().value());

    }

    @Test
    void read_multiple_field_in_bucket() {

        String field1 = "1";
        String field2 = "2";
        String field3 = "3";

        var evt1 = ArticleViewUpEvent.newEvent(1L);

        var evt2 = ArticleViewUpEvent.newEvent(2L);
        var evt3 = ArticleViewUpEvent.newEvent(2L);

        var evt4 = ArticleViewUpEvent.newEvent(3L);
        var evt5 = ArticleViewUpEvent.newEvent(3L);
        var evt6 = ArticleViewUpEvent.newEvent(3L);

        writer.countUpEvent(evt1, 1);
        writer.countUpEvent(evt2, 1);
        writer.countUpEvent(evt3, 1);
        writer.countUpEvent(evt4, 1);
        writer.countUpEvent(evt5, 1);
        writer.countUpEvent(evt6, 1);

        var pairs = countReader.popBucket(ArticleViewUpEvent.eventbucket());

        var pair1 = pairs.stream().filter(e -> e.field().equals(field1)).toList().getFirst();
        var pair2 = pairs.stream().filter(e -> e.field().equals(field2)).toList().getFirst();
        var pair3 = pairs.stream().filter(e -> e.field().equals(field3)).toList().getFirst();


        assertEquals(1, pair1.value());
        assertEquals(2, pair2.value());
        assertEquals(3, pair3.value());
    }

}