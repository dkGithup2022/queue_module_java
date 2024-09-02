package example.queue.module.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    /*
    * 이게 멀티 모듈 기반의 조각을 가져온거라, 사실 redis 에 대한 connection 설정 의존성을 여기서 가지고 있는게 bad 한 판단임
    * 멀티 모듈 환경이라면 redis config 에 대한 module 도 별도 모듈에서 가지길 바람
    * */


    /*
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        .... do some config
        return Redisson.create(config);
    }
     */


    @Bean
    @Qualifier("redisObjectMapper")
    public ObjectMapper customObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // JSR310 모듈 등록 -> LocalDateTime 파싱 기능 추가.
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
