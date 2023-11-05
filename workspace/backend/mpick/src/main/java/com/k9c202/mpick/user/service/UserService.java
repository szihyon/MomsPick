package com.k9c202.mpick.user.service;

import com.k9c202.mpick.trade.service.S3Service;
import com.k9c202.mpick.user.controller.request.UpdateUserInfoRequest;
import com.k9c202.mpick.user.controller.response.EmailVerificationResponse;
import com.k9c202.mpick.user.controller.response.JoinUserResponse;
import com.k9c202.mpick.user.controller.response.UserInfoResponse;
import com.k9c202.mpick.user.dto.LoginDto;
import com.k9c202.mpick.user.dto.UserDto;
import com.k9c202.mpick.user.entity.User;
import com.k9c202.mpick.user.jwt.JwtFilter;
import com.k9c202.mpick.user.jwt.TokenProvider;
import com.k9c202.mpick.user.repository.UserQueryRepository;
import com.k9c202.mpick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class UserService {

    // object에 종속된 변수
    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisService redisService;
    private final S3Service s3Service;

    // 생성자, 같은 이름으로 정의, 실제 객체를 만들 때 사용
    // UserService userService = new UserService(userRepository)에서 UserService에 대한 정의
//    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.tokenProvider = tokenProvider;
//        this.authenticationManagerBuilder = authenticationManagerBuilder;
//    }
// --> @RequiredArgsConstructor

    // builder 형식으로 생성자 대신 사용
    // 생성자 형식 -> User a = new User(nickname,password,status,....)
    // 아래는 builder 형식

    // 회원가입
    public JoinUserResponse signup(UserDto userDto) {
        
        checkDuplicatedLoginId(userDto.getLoginId());
        checkDuplicatedEmail(userDto.getEmail());
        checkDuplicatedNickname(userDto.getNickname());

        User user = User.builder()
                .loginId(userDto.getLoginId())
                .password(passwordEncoder.encode(userDto.getPassword())) // 패스워드 암호화
                .nickname(userDto.getNickname())
                .email(userDto.getEmail())
                .build();
        // build가 return하는 타입이 User (.build 이후 User)

        // save -> JpaRepository에 정의돼있음
        User savedUser = userRepository.save(user);

        return JoinUserResponse.of(savedUser);
    }

    //로그인 아이디 중복체크
    public void checkDuplicatedLoginId(String loginId) {
        boolean isExistLoginId = userQueryRepository.existLoginId(loginId);
        if (isExistLoginId) {
            throw new IllegalArgumentException("로그인 아이디 중복");
        }
    }

    // 닉네임 중복체크
    public void checkDuplicatedNickname(String nickname) {
        boolean isExistNickname = userQueryRepository.existNickname(nickname);
        if (isExistNickname) {
            throw new IllegalArgumentException("닉네임 중복");
        }
    }

    // 이메일 중복체크
    public void checkDuplicatedEmail(String email) {
        boolean isExistEmail = userQueryRepository.existEmail(email);
        if (isExistEmail) {
            throw new IllegalArgumentException("이메일 중복");
        }
    }

    // 로그인
    public String login(LoginDto loginDto) {
        // 인증에 필요한 정보 authenticationToken에 저장
        UsernamePasswordAuthenticationToken authenticationToken =
                // 입력받은 id, password 정보 사용
                // loadUserByUsername의 반환값과 비교하여 일치여부 체크
                new UsernamePasswordAuthenticationToken(loginDto.getLoginId(), loginDto.getPassword());

        // SecurityContext에 인증 여부(authentication) 저장
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // 인증여부(authentication)를 context에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenProvider.createToken(authentication);
    }

    // 정보 조회
    public UserInfoResponse getUserInfo() {
        // 유저 아이디 정보는 SecurityContextHolder.getContext().getAuthentication().getName()으로 얻을 수 있음
        // JwtFilter에서 setAuthentication
        // 방법2(회원정보수정)/ @AuthenticationPrincipal UserDetails userDetails
        //      userDetails.getUsername()
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        System.out.println("loginId = " + loginId);
        // 로그인 아이디에 해당하는 유저 정보 불러오기
        // findOneByLoginId는 Optional 클래스를 반환하는데, User 클래스를 얻어야 함
        // UserRepository에서 지정해준 클래스(User)로 반환하도록 Optional 함수 중 .orElseThrow 사용
        // Optional 객체 접근 방법 : https://velog.io/@alicesykim95/Java-Optional
        // 함수에 따라 반환값 다름
        // null이 아니면 User 클래스 반환, null이면 UsernameNotFoundException 반환
        User user = userRepository.findOneByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        return UserInfoResponse.of(user);
    }

    // 로그아웃
    // redis에 accesstoken을 저장하여 로그아웃 여부 체크
    public void logout(String accessToken) {
        long accessTokenExpirationMillis = tokenProvider.getTokenValidityInMilliseconds();
        redisService.setValues(accessToken, "logout", Duration.ofMillis(accessTokenExpirationMillis));
    }

    // 회원 탈퇴
    public void withdraw() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();

        User user = userRepository.findOneByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        user.setStatus(2);
    }

    // 회원 정보 수정
    public UserInfoResponse updateUserInfo(String loginId, UpdateUserInfoRequest updateUserInfoRequest, MultipartFile profileImg) throws IOException {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String loginId = authentication.getName();
        User user = userRepository.findOneByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        // updateUserInfoRequest != null 조건을 추가하지 않으면 data없이 프로필이미지만 수정했을 때 null 에러 발생
        if (updateUserInfoRequest != null) {
            if (updateUserInfoRequest.getNickname() != null) {
                user.setNickname(updateUserInfoRequest.getNickname());
            }
            if (updateUserInfoRequest.getEmail() != null) {
                user.setEmail(updateUserInfoRequest.getEmail());
            }
            if (updateUserInfoRequest.getUserIntro() != null) {
                user.setUserIntro(updateUserInfoRequest.getUserIntro());
            }
        }
        if (profileImg != null) {
            String profileUrl = s3Service.upload(profileImg, "profiles/");
            user.setProfileImage(profileUrl);
        }
        return UserInfoResponse.of(user);
    }



 }
