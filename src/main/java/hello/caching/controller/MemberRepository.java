package hello.caching.controller;

import hello.caching.domain.Member;

public interface MemberRepository {

    Member findByNameNoCache(String name);
    Member findByNameCache(String name);
    void refresh(String name);
}
