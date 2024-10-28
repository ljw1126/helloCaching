package hello.caching.practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// https://redis.io/docs/latest/develop/data-types/hashes/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HashesTest {
    private static JedisPool jedisPool;

    @BeforeAll
    static void beforeAll() {
        jedisPool = new JedisPool("localhost", 6379);

        try(Jedis jedis = jedisPool.getResource()) {
            jedis.hset("users:1:info", Map.of("name", "tester", "age", "99"));
            jedis.hset("users:1:info", "email", "tester@gmail.com");
        }
    }

    @Order(1)
    @Test
    void hGet() {
        try(Jedis jedis = jedisPool.getResource()) {
            String email = jedis.hget("users:1:info", "email");
            assertThat(email).isEqualTo("tester@gmail.com");
        }
    }

    @Order(2)
    @Test
    void hGetAll() {
        try(Jedis jedis = jedisPool.getResource()) {
            Map<String, String> result = jedis.hgetAll("users:1:info");
            assertThat(result).extracting("name", "age", "email")
                    .containsExactly("tester", "99", "tester@gmail.com");
        }
    }

    @Order(3)
    @Test
    void hDel() {
        try(Jedis jedis = jedisPool.getResource()) {
            long count = jedis.hdel("users:1:info", "email");
            assertThat(count).isEqualTo(1);

            String email = jedis.hget("users:1:info", "email");
            assertThat(email).isNull();
        }
    }

    @Order(4)
    @Test
    void test() {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.hincrBy("users:1:info", "visits", 2);

            String visits = jedis.hget("users:1:info", "visits");
            assertThat(visits).isEqualTo("2");
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
