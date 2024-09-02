package example.queue.module.counter;

import example.queue.module.api.CounterReader;
import example.queue.module.counter.dto.CounterPair;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class RedisCounterReader implements CounterReader {

    Logger log = LoggerFactory.getLogger(RedisCounterReader.class);
    private final RedissonClient redisson;

    public RedisCounterReader(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public List<CounterPair> popBucket(String bucket) {
        log.info("Try pop bucket : {}", bucket);
        var script = redisson.getScript(StringCodec.INSTANCE);

        // Execute the Lua script with the given bucket (key)
        List<Object> objList = script.eval(RScript.Mode.READ_ONLY, luaScript, RScript.ReturnType.MULTI, Collections.singletonList(bucket));
        var pairs = mapToPair(objList);
        log.info("bucket cleared: {}", bucket);
        return pairs;
    }

    @Override
    public CounterPair popField(String bucketName, String field) {
        return null;
    }

    private List<CounterPair> mapToPair(List<Object> objList) {
        if (objList == null || objList.isEmpty())
            return List.of();

        List<CounterPair> pairs = new ArrayList<>();
        for (int i = 0; i < objList.size(); i += 2) {
            try {
                var field = objToString(objList.get(i));
                var value = Long.valueOf(objToString(objList.get(i + 1)));
                pairs.add(new CounterPair(field, value));
            } catch (Exception e) {
                log.error("Can not convert to Pair | field: {}, value : {} ", objList.get(i), objList.get(i + 1));
            }
        }

        return pairs;
    }


    private String objToString(Object object) {
        return String.valueOf(object);
    }

    String luaScript = """
            local hashKey = KEYS[1];
            local result = redis.call('HGETALL', hashKey);
            redis.call('DEL', hashKey);
            return result;
            """;

}
