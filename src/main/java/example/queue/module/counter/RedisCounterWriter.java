package example.queue.module.counter;

import example.queue.module.api.CounterWriter;
import example.queue.module.data.CounterEvent;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RedisCounterWriter implements CounterWriter {

    Logger log = LoggerFactory.getLogger(RedisCounterWriter.class);

    private final RedissonClient redisson;

    public RedisCounterWriter(RedissonClient redisson) {
        this.redisson = redisson;
    }


    @Override
    public void countUpEvent(CounterEvent event, int count) {
        log.info("Try publish Event : {} ", event.toString());

        var script = redisson.getScript(StringCodec.INSTANCE);
        var bucket = event.bucket();
        var field = event.field();
        var currentValue = executeScript(count, script, bucket, field);

        log.info("event published|  bucket : {} , field: {} , value :{}", bucket, field, currentValue);
    }

    private Long executeScript(int count, RScript script, String bucket, String field) {
        return script.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.VALUE,
                Arrays.asList(bucket),
                field,
                String.valueOf(count)
        );
    }

    String luaScript = """
        local hashKey = KEYS[1];
        local field = ARGV[1];
        local increment = tonumber(ARGV[2]);
        local current = tonumber(redis.call('HGET', hashKey, field));
        if not current then current = 0 end;
        local newValue = current + increment;
        redis.call('HSET', hashKey, field, newValue);
        return newValue;
        """;

}
