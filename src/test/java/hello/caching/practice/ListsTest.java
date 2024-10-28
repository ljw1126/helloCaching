package hello.caching.practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ListsTest {

    private static JedisPool jedisPool;

    @BeforeAll
    static void beforeAll() {
        jedisPool = new JedisPool("localhost", 6379);
    }

    @DisplayName("RPUSH, RPOP 으로 Lists를 Stack으로 사용할 수 있다")
    @Test
    void stack() { // LIFO
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.rpush("stack1", "aaa");
            jedis.rpush("stack1", "bbb");
            jedis.rpush("stack1", "ccc");

            List<String> stack1 = jedis.lrange("stack1", 0, -1);
            assertThat(stack1).hasSize(3);

            String v1 = jedis.rpop("stack1");
            String v2 = jedis.rpop("stack1");
            String v3 = jedis.rpop("stack1");

            assertThat(v1).isEqualTo("ccc");
            assertThat(v2).isEqualTo("bbb");
            assertThat(v3).isEqualTo("aaa");
        }
    }

    @DisplayName("RPUSH, LPOP 으로 Lists를 Queue로 사용할 수 있다")
    @Test
    void queue() { // FIFO
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.rpush("queue1", "aaa");
            jedis.rpush("queue1", "bbb");
            jedis.rpush("queue1", "ccc");

            List<String> stack1 = jedis.lrange("queue1", 0, -1);
            assertThat(stack1).hasSize(3);

            String v1 = jedis.lpop("queue1");
            String v2 = jedis.lpop("queue1");
            String v3 = jedis.lpop("queue1");

            assertThat(v1).isEqualTo("aaa");
            assertThat(v2).isEqualTo("bbb");
            assertThat(v3).isEqualTo("ccc");
        }
    }

    @DisplayName("Stack에 데이터가 없으면, 10 초간 블록 대기 후 없으면 종료, 있으면 반환한다")
    @Test
    void brpop() {
        try(Jedis jedis = jedisPool.getResource()) {
            List<String> brpop = jedis.brpop(10, "queue:blocking");
            if(brpop != null) {
                brpop.forEach(System.out::println);
            }
        }
    }

    @DisplayName("큐에 데이터가 없으면, 10초간 블록 대기 후 없으면 종료, 있으면 반환한다")
    @Test
    void blpop() { // Queue 사용
        try(Jedis jedis = jedisPool.getResource()) {
            List<String> blpop = jedis.blpop(10, "queue:blocking");
            if(blpop != null) {
                blpop.forEach(System.out::println);
            }
        }
    }

    @AfterAll
    static void afterAll() {
        if(jedisPool != null && !jedisPool.isClosed()) {
            Jedis jedis = jedisPool.getResource();
            jedis.flushAll();
            jedis.close();
            jedisPool.close();
        }
    }
}
