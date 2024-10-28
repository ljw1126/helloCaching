package hello.caching.practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

// https://redis.io/docs/latest/develop/data-types/bitmaps/
public class BitmapsTest {
    private static JedisPool jedisPool;

    @BeforeAll
    static void beforeAll() {
        jedisPool = new JedisPool("localhost", 6379);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setbit("request-somepage-241024", 100, true); // offset : 위치, value : boolean(0, 1)
            jedis.setbit("request-somepage-241024", 200, true);
            jedis.setbit("request-somepage-241024", 300, true);
        }
    }

    @Test
    void getbit() {
        try (Jedis jedis = jedisPool.getResource()) {
            boolean result = jedis.getbit("request-somepage-241024", 100);
            assertThat(result).isTrue();
        }
    }

    @Test
    void bitcount() {
        try (Jedis jedis = jedisPool.getResource()) {
            long result = jedis.bitcount("request-somepage-241024");
            assertThat(result).isEqualTo(3);
        }
    }

    @DisplayName("set보다 bitmaps가 더 적은 메모리를 차지한다")
    @Test
    void memoryUsage() {
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipelined = jedis.pipelined();
            IntStream.rangeClosed(0, 100_000)
                    .forEach(i -> {
                        pipelined.sadd("request-set-241024", String.valueOf(i), "1"); // set
                        pipelined.setbit("request-bimaps-241024", i, true); // bitmaps

                        if (i % 1000 == 0) {
                            pipelined.syncAndReturnAll();
                        }
                    });
            pipelined.syncAndReturnAll();

            Long setMemoryUsage = jedis.memoryUsage("request-set-241024"); // 4248728 byte
            Long bitmapsMemoryUsage = jedis.memoryUsage("request-bimaps-241024"); // 16448 byte

            assertThat(bitmapsMemoryUsage).isLessThan(setMemoryUsage);
        }
    }

    @AfterAll
    static void afterAll() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            Jedis jedis = jedisPool.getResource();
            jedis.flushAll();
            jedis.close();
            jedisPool.close();
        }
    }
}
