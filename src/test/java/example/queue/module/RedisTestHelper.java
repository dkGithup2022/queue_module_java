package example.queue.module;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class RedisTestHelper {

    private final RedissonClient redissonClient;

    public RedisTestHelper(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void clear(){
        redissonClient.getKeys().flushdb();
    }

}

