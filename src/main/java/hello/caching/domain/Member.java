package hello.caching.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
