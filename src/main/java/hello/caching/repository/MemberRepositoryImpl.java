package hello.caching.repository;

import hello.caching.controller.MemberRepository;
import hello.caching.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class MemberRepositoryImpl implements MemberRepository {

    @Override
    public Member findByNameNoCache(String name) {
        slowQuery(2000);
        return new Member(1L, name + "@gmail.com", name);
    }

    @Override
    @Cacheable(value = "findMemberCache", key = "#name", unless = "#result == null")
    public Member findByNameCache(String name) {
        slowQuery(2000);
        return new Member(1L, name + "@gmail.com", name);
    }

    @Override
    @CacheEvict(value = "findMemberCache", key = "#name")
    public void refresh(String name) {
        log.info("{}의 Cache Clear!", name);
    }

    private void slowQuery(long seconds) {
        try {
            Thread.sleep(seconds);
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
