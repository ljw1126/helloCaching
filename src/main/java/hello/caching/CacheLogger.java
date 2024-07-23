package hello.caching;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

@Slf4j
public class CacheLogger implements CacheEventListener<Object, Object> {

    @Override
    public void onEvent(CacheEvent<?, ?> event) {
        log.info("key : {}, eventType : {}, old value : {}, new value : {}",
                event.getKey(),
                event.getType(),
                event.getOldValue(),
                event.getNewValue());
    }
}
