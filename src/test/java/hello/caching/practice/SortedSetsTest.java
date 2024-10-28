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
import redis.clients.jedis.resps.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// https://redis.io/docs/latest/develop/data-types/sorted-sets/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SortedSetsTest {
    private static JedisPool jedisPool;

    @BeforeAll
    static void beforeAll() {
        jedisPool = new JedisPool("localhost", 6379);

        try(Jedis jedis = jedisPool.getResource()) {
            Map<String, Double> scoreMap = new HashMap<>();
            scoreMap.put("user1", (double) 100);
            scoreMap.put("user2", (double) 75);
            scoreMap.put("user3", (double) 50);
            scoreMap.put("user4", (double) 25);
            scoreMap.put("user5", (double) 0);

            jedis.zadd("game1:scores", scoreMap);
        }
    }

    @Order(1)
    @Test
    void zrange() {
        try(Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.zrange("game1:scores", 0, Long.MAX_VALUE); // 오름차순

            assertThat(result).hasSize(5)
                            .containsExactly("user5", "user4", "user3", "user2", "user1");
        }
    }

    @Order(2)
    @Test
    void zrangeWithScores() {
        try(Jedis jedis = jedisPool.getResource()) {
            List<Tuple> result = jedis.zrangeWithScores("game1:scores", 0, Long.MAX_VALUE);// 오름차순

            assertThat(result).hasSize(5)
                    .extracting(Tuple::getElement, Tuple::getScore)
                    .containsExactlyInAnyOrder(
                    org.assertj.core.groups.Tuple.tuple("user1", 100.0),
                            org.assertj.core.groups.Tuple.tuple("user2", 75.0),
                            org.assertj.core.groups.Tuple.tuple("user3", 50.0),
                            org.assertj.core.groups.Tuple.tuple("user4", 25.0),
                            org.assertj.core.groups.Tuple.tuple("user5", 0.0)
                    );
        }
    }

    @Order(3)
    @Test
    void zcard() {
        try(Jedis jedis = jedisPool.getResource()) {
            long count = jedis.zcard("game1:scores");

            assertThat(count).isEqualTo(5);
        }
    }

    @Order(4)
    @Test
    void zincrby() {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.zincrby("game1:scores", 200.0, "user4");

            List<String> result = jedis.zrevrange("game1:scores", 0, 0); // desc
            assertThat(result).hasSize(1)
                    .containsExactly("user4");
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
