package io.github.alef2509.library.application.gateway;

import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.Member;

import java.util.Optional;

public interface MemberGateway {

    void save(Member member);

    Optional<Member> findById(MemberId id);
}
