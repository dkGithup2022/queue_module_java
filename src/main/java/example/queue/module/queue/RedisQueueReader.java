package example.queue.module.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.queue.module.LuaScriptUtils;
import example.queue.module.api.QueueReader;
import example.queue.module.data.QueueEvent;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RedisQueueReader implements QueueReader {


    Logger log = LoggerFactory.getLogger(RedisQueueReader.class);
    private final RedissonClient redisson;


    @Qualifier("redisObjectMapper")
    private final ObjectMapper objectMapper;

    public RedisQueueReader(RedissonClient redisson, ObjectMapper objectMapper) {
        this.redisson = redisson;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<QueueEvent> read(String queueName, Class<? extends QueueEvent> clazz, int from, int to) {
        log.trace("Try read queue : {}, cast :{} , from :{}, size: {}", queueName, clazz.toString(), from, to);
        List<String> result = callReadScript(queueName, from, to);

        List<QueueEvent> converted = convertToEvent(result, clazz);
        log.trace("queue read finished with {} item", converted.size());
        return converted;
    }

    @Override
    public List<QueueEvent> pop(String queueName, Class<? extends QueueEvent> clazz, int count) {
        log.trace("Try pop queue : {}, cast :{} , count : {}", queueName, clazz.toString(), count);

        List<String> result = callPopScript(queueName, count);

        List<QueueEvent> converted = convertToEvent(result, clazz);
        log.trace("queue read finished with {} item", converted.size());
        return converted;
    }


    private List<String> callReadScript(String queueName, int from, int to) {
        var script = redisson.getScript(StringCodec.INSTANCE);
        return script.eval(RScript.Mode.READ_ONLY, readScript, RScript.ReturnType.MULTI, List.of(queueName), from, to);
    }


    private List<String> callPopScript(String queueName, int count) {
        var script = redisson.getScript(StringCodec.INSTANCE);
        return script.eval(RScript.Mode.READ_ONLY, popScript, RScript.ReturnType.MULTI, List.of(queueName), count);
    }

    private List<QueueEvent> convertToEvent(List<String> result, Class<? extends QueueEvent> clazz) {
        List<QueueEvent> evts = new ArrayList<>();
        int convertErrorCount = 0;
        for (String piece : result) {
            try {
                evts.add(objectMapper.readValue(LuaScriptUtils.makeReadable(piece), clazz));
            } catch (Exception e) {
                convertErrorCount++;
                log.error("Can not Convert String to obj , class :{} , obj :{}", clazz.toString(), piece.toString());
                log.error(e.getLocalizedMessage());
            }
        }
        if (convertErrorCount != 0) {
            log.error("Error occured on converting event ");
            log.error("Fail to convert {} event out of {}", convertErrorCount, result.size());
        }

        return evts;
    }

    final String readScript = """
            local listKey = KEYS[1];
            local from = tonumber(ARGV[1]);
            local to = tonumber(ARGV[2]);
            local current = redis.call('LRANGE', listKey, from, to)
            return current;
            """;


    String popScript = """
            local listKey = KEYS[1]
            local count = tonumber(ARGV[1])
            local result = {}

            for i = 1, count do
                local element = redis.call('RPOP', listKey)
                if not element then
                    break
                end
                table.insert(result, element)
            end

            return result
            """;

}
