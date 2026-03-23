package com.example.ordersystem.member.service;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.LoginDto;
import com.example.ordersystem.member.dto.MemberSaveReqDto;
import com.example.ordersystem.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long save(MemberSaveReqDto memberSaveReqDto){
        Optional<Member> optionalMember = memberRepository.findByEmail(memberSaveReqDto.getEmail());
        if(optionalMember.isPresent()){
            throw new IllegalArgumentException("기존에 존재하는 회원입니다.");
        }

        String password = passwordEncoder.encode(memberSaveReqDto.getPassword());
        Member member = memberRepository.save(memberSaveReqDto.toEntity(password));
        return member.getId();
    }

    public Member login(LoginDto dto){
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("email 또는 비밀번호가 일치하지 않습니다."));

        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("email 또는 비밀번호가 일치하지 않습니다.");
        }

        return member;
    }
}
