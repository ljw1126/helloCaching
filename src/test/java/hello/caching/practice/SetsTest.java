package hello.caching.practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SetsTest {

    private static JedisPool jedisPool;

    @BeforeAll
    static void beforeAll() {
        jedisPool = new JedisPool("localhost", 6379);
    }

    @Order(1)
    @Test
    void sadd() {
        try (Jedis jedis = jedisPool.getResource()) {
            long count = jedis.sadd("users:1:follow", "100", "200", "300");

            assertThat(count).isEqualTo(3);
        }
    }

    @Order(2)
    @Test
    void smembers() {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> result = jedis.smembers("users:1:follow");
            assertThat(result)
                    .hasSize(3)
                    .contains("100", "200", "300");
        }
    }

    @Order(3)
    @Test
    void srem() {
        try (Jedis jedis = jedisPool.getResource()) {
            long count = jedis.srem("users:1:follow", "100");
            assertThat(count).isEqualTo(1);

            Set<String> result = jedis.smembers("users:1:follow");
            assertThat(result)
                    .hasSize(2)
                    .contains("200", "300");
        }
    }

    @Order(4)
    @Test
    void sismember() {
        try (Jedis jedis = jedisPool.getResource()) {
            boolean result1 = jedis.sismember("users:1:follow", "100");
            boolean result2 = jedis.sismember("users:1:follow", "200");

            assertThat(result1).isFalse();
            assertThat(result2).isTrue();
        }
    }

    @Order(5)
    @Test
    void scard() {
        try (Jedis jedis = jedisPool.getResource()) {
            long count = jedis.scard("users:1:follow");
            assertThat(count).isEqualTo(2);
        }
    }

    @DisplayName("sinter는 O(N * M) 시간복잡도 가짐, 교집합")
    @Order(6)
    @Test
    void sinter() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sadd("users:2:follow", "200", "400", "600");
            Set<String> result = jedis.sinter("users:1:follow", "users:2:follow");

            assertThat(result).hasSize(1)
                    .contains("200");
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
