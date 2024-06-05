package com.example.jwtlogin.member;

import com.example.jwtlogin.common.dto.enums.RoleEnums;
import com.example.jwtlogin.member.domain.Member;
import com.example.jwtlogin.member.domain.MemberRepository;
import com.example.jwtlogin.member.dto.request.MemberSaveRequestDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestPropertySource(locations = "classpath:application.yml")
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void saveMemberTest() {

        // given
        MemberSaveRequestDto saveRequestDto = MemberSaveRequestDto.builder()
                .email("test@gmail.com")
                .password("test!!")
                .name("해니")
                .role(RoleEnums.ROLE_MEMBER)
                .build();

        // when
        Member member = memberRepository.save(saveRequestDto.toEntity());

        // then
        Assertions.assertThat(member).isNotNull();
    }

}
