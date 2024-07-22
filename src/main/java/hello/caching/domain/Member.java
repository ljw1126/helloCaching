package hello.caching.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private Long idx;
    private String email;
    private String name;

    public Member(String email, String name) {
        this(null, email, name);
    }

    public Member(Long idx, String email, String name) {
        this.idx = idx;
        this.email = email;
        this.name = name;
    }
}
