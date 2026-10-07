package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.application.gateway.MemberGateway;
import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.LibraryException;
import io.github.alef2509.library.domain.Member;

import java.util.Locale;

public class RegisterMemberInteractor implements RegisterMember {

    private final MemberGateway members;
    private final UnitOfWork unitOfWork;

    public RegisterMemberInteractor(MemberGateway members, UnitOfWork unitOfWork) {
        this.members = members;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public Response execute(Request request) {
        Member.Membership membership;
        try {
            membership = Member.Membership.valueOf(request.membership().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new LibraryException("invalid-membership", "Membership must be STANDARD or PREMIUM");
        }
        Member member = new Member(MemberId.next(), request.name(), membership);
        return unitOfWork.execute(() -> {
            members.save(member);
            return new Response(member.id().value());
        });
    }
}
