package hello.caching.practice;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.resps.GeoRadiusResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// https://redis.io/docs/latest/develop/data-types/geospatial/
public class GeospatialTest {
    private static JedisPool jedisPool;

    @BeforeAll
    static void beforeAll() {
        jedisPool = new JedisPool("localhost", 6379);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.geoadd("bikes:rentable", -122.27652, 37.805186, "station:1");
            jedis.geoadd("bikes:rentable", -122.2674626, 37.8062344, "station:2");
            jedis.geoadd("bikes:rentable", -122.2469854, 37.8104049, "station:3");
        }
    }

    @Test
    void geodist() {
        try (Jedis jedis = jedisPool.getResource()) {
            Double geodist = jedis.geodist("bikes:rentable", "station:1", "station:2"); // default meter
            assertThat(geodist).isEqualTo(804.7392);
        }
    }

    @Test
    void geosearch() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<GeoRadiusResponse> geosearch = jedis.geosearch("bikes:rentable",
                    new GeoCoordinate(-122.2612767, 37.7936847),
                    5,
                    GeoUnit.KM
            );

            assertThat(geosearch).hasSize(3)
                    .extracting(GeoRadiusResponse::getMemberByString)
                    .containsExactly("station:1", "station:2", "station:3");
        }
    }

    @Test
    void geosearchWithDist() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<GeoRadiusResponse> geosearch = jedis.geosearch("bikes:rentable",
                    new GeoSearchParam()
                            .fromLonLat(new GeoCoordinate(-122.2612767, 37.7936847))
                            .byRadius(5, GeoUnit.KM)
                            .withDist()
            );

            assertThat(geosearch).hasSize(3)
                    .extracting(GeoRadiusResponse::getMemberByString, GeoRadiusResponse::getDistance)
                    .containsExactly(
                            Tuple.tuple("station:1", 1.8523),
                            Tuple.tuple("station:2", 1.4979),
                            Tuple.tuple("station:3", 2.2441)
                    );
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
