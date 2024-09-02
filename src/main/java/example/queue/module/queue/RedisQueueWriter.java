package example.queue.module.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.queue.module.api.QueueWriter;
import example.queue.module.data.QueueEvent;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RedisQueueWriter implements QueueWriter {

    Logger log = LoggerFactory.getLogger(RedisQueueWriter.class);


    private final RedissonClient redisson;

    @Qualifier("redisObjectMapper")
    private final ObjectMapper objectMapper;

    public RedisQueueWriter(RedissonClient redisson, ObjectMapper objectMapper) {
        this.redisson = redisson;
        this.objectMapper = objectMapper;
    }


    @Override
    public void append(QueueEvent event) {
        log.trace("Try publish event : {}, content :{} ", event.toString());
        String json = toJsonString(event);
        var list = redisson.getList(event.queueName());
        list.add(json);
        log.trace("Event published");
    }


    private String toJsonString(QueueEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error(e.getLocalizedMessage());
            log.error("event 를 String으로 파싱할 수 없습니다");
            throw new RuntimeException("can not parse event to String");
        }
    }
}
