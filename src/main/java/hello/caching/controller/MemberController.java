package hello.caching.controller;

import hello.caching.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MemberController {

    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/member/nocache/{name}")
    public ResponseEntity<Member> getNoCacheMember(@PathVariable("name") String name) {
        long start = System.currentTimeMillis();
        Member member = memberRepository.findByNameNoCache(name);
        long end = System.currentTimeMillis();

        log.info("{}의 NoCache 수행시간 : {} ", name, (end - start));

        return ResponseEntity.ok(member);
    }

    @GetMapping("/member/cache/{name}")
    public ResponseEntity<Member> getCacheMember(@PathVariable("name") String name) {
        long start = System.currentTimeMillis();
        Member member = memberRepository.findByNameCache(name);
        long end = System.currentTimeMillis();

        log.info("{}의 Cache 수행시간 : {} ", name, (end - start));

        return ResponseEntity.ok(member);
    }

    @GetMapping("/member/refresh/{name}")
    public ResponseEntity<String> refresh(@PathVariable("name") String name) {
        memberRepository.refresh(name);
        return ResponseEntity.ok("Cache Clear");
    }
}
