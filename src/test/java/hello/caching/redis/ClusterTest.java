package hello.caching.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class ClusterTest {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    void get() {
        String key = "testKey";
        String value = "Cluster Testing..";

        stringRedisTemplate.opsForValue().set(key, value);

        String result = stringRedisTemplate.opsForValue().get(key);

        assertThat(result).isEqualTo(value);
    }

    // cluster.conf에 read priority 조절하여 클러스터 노드 모니터링 한다
    // priority 가중치를 slave가 높게하니 master로 read 요청이 가지 않는다
    @Test
    void loop() {
        String key = "testKey";
        String value = "Cluster Testing..";

        for (int i = 0; i < 10; i++) {
            String result = stringRedisTemplate.opsForValue().get(key);
            assertThat(result).isEqualTo(value);
        }
    }

    @Test
    void multiGet() {
        stringRedisTemplate.opsForValue().set("name", "matrim cauthon"); // slot 5798
        stringRedisTemplate.opsForValue().set("nickname", "prince of the ravens"); // slot 14594

        assertThat(stringRedisTemplate.opsForValue().multiGet(Arrays.asList("name", "nickname")))
                .contains("matrim cauthon", "prince of the ravens");
    }

    @DisplayName("{user}, 고정태그를 사용하여 저장하면 multi key 호출할 수 있다")
    @Test
    void fixedSlotOperation() {
        stringRedisTemplate.opsForValue().set("{user}.name", "perrin aybara");
        stringRedisTemplate.opsForValue().set("{user}.nickname", "wolfbrother");

        List<String> result = stringRedisTemplate.opsForValue().multiGet(Arrays.asList("{user}.name", "{user}.nickname"));
        assertThat(result)
                .contains("perrin aybara", "wolfbrother");
    }

    @DisplayName("predixy에서는 keys를 처리하지 못한다")
    @Test
    void error() {
        assertThatThrownBy(() -> stringRedisTemplate.keys("*"))
                .isInstanceOf(RedisSystemException.class)
                .hasMessage("Error in execution");
    }
}
