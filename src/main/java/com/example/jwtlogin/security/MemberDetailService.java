package com.example.jwtlogin.security;

import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.member.domain.MemberRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public MemberDetailService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member memeber = memberRepository.findByEmail(email);
        if (memeber == null) {
            throw new UsernameNotFoundException(null);
        }
        return MemberDetails.builder()
                .email(email)
                .memberSeq(memeber.getMemberSeq())
                .build();
    }

}
