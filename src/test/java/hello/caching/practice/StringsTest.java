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
import redis.clients.jedis.Pipeline;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StringsTest {

    private static JedisPool jedisPool;

    @BeforeAll
    static void beforeAll() {
        jedisPool = new JedisPool("localhost", 6379);

        try (Jedis jedis = jedisPool.getResource()) {
            for (int i = 1; i <= 10; i++) {
                String key = String.format("users:%d:email", i);
                String value = String.format("test%d@gmail.com", i);
                jedis.set(key, value);
            }
        }
    }

    @Test
    void get() {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.get("users:1:email");
            assertThat(result).isEqualTo("test1@gmail.com");
        }
    }

    @Test
    void mget() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.mget("users:1:email", "users:2:email", "users:3:email");
            assertThat(result).hasSize(3)
                    .containsExactly("test1@gmail.com", "test2@gmail.com", "test3@gmail.com");
        }
    }

    @Order(1)
    @Test
    void incr() {
        try (Jedis jedis = jedisPool.getResource()) {
            String before = jedis.get("counter");
            long expected = (before == null ? 0 : Long.parseLong(before)) + 1;

            long counter = jedis.incr("counter");

            assertThat(counter).isGreaterThanOrEqualTo(expected);
        }
    }

   @Order(2)
   @Test
   void incrBy() {
       try (Jedis jedis = jedisPool.getResource()) {
           long before = Long.parseLong(jedis.get("counter"));
           long counter = jedis.incrBy("counter", 10);
           assertThat(counter).isGreaterThanOrEqualTo(before + 10);
       }
   }

   @Order(3)
   @Test
   void decr() {
       try (Jedis jedis = jedisPool.getResource()) {
           long before = Long.parseLong(jedis.get("counter"));
           long counter = jedis.decr("counter");
           assertThat(counter).isEqualTo(before - 1);
       }
   }

    @Order(4)
    @Test
    void decrBy() {
        try (Jedis jedis = jedisPool.getResource()) {
            long before = Long.parseLong(jedis.get("counter"));
            long counter = jedis.decrBy("counter", 10);
            assertThat(counter).isEqualTo(before - 10);
        }
    }

    // https://redis.io/docs/latest/develop/use/pipelining/
    @DisplayName("pipelining 으로 여러 set 요청을 한번에 보낼 수 있다")
    @Test
    void pipeline() {
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipelined = jedis.pipelined();
            for (int i = 11; i <= 20; i++) {
                String key = String.format("users:%d:email", i);
                String value = String.format("test%d@gmail.com", i);
                pipelined.set(key, value);
            }

            List<Object> objects = pipelined.syncAndReturnAll();
            assertThat(objects).hasSize(10)
                    .containsOnly("OK");
        }
    }


    /**
     * new String[0]
     * - 배열의 크기가 0이어도 컬렉션 크기에 맞는 새로운 배열을 생성하여 반환
     * - 자바 6 이전에는 new String[collection.size()]로 생성했지만, 이후 성능 최적화가 이루어져 new String[0] 사용해도 불리한점 x
     */
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
