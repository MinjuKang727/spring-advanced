package org.example.expert.domain.auth;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder;

    @Spy
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    class SignupTest {
        @Test
        public void 이미_존재하는_이메일이어서_에러_발생() {
            // given
            SignupRequest request = new SignupRequest("user@gmail.com", "PASSWORD1234", "USER");

            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signup(request));

            // then
            verify(userRepository, times(1)).existsByEmail(anyString());
            assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        }

        @Test
        public void 회원가입_성공() {
            // given
            SignupRequest request = new SignupRequest("user@gmail.com", "PASSWORD1234", "USER");
            User savedUser = spy(User.class);
            ReflectionTestUtils.setField(savedUser, "id", 1L);
            ReflectionTestUtils.setField(savedUser, "email", request.getEmail());
            ReflectionTestUtils.setField(jwtUtil, "secretKey", "7Iqk7YyM66W07YOA7L2U65Sp7YG065+9U3ByaW5n6rCV7J2Y7Yqc7YSw7LWc7JuQ67mI7J6F64uI64ukLg==");

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            ReflectionTestUtils.invokeMethod(jwtUtil, "init");

            // when
            SignupResponse response = authService.signup(request);


            // then
            verify(passwordEncoder, times(1)).encode(anyString());
            verify(userRepository, times(1)).save(any(User.class));
            verify(jwtUtil, times(1)).createToken(anyLong(), anyString(), any(UserRole.class));
            assertNotNull(response);
            assertEquals("Bearer eyJhbGciOiJIUzI1NiJ9", response.getBearerToken().substring(0, 27));
        }
    }

    @Nested
    class SigninTest {

        @Test
        public void 가입되지_않은_유저여서_에러_발생() {
            // given
            SigninRequest request = new SigninRequest("user@gmail.com", "PASSWORD1234");

            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    authService.signin(request)
            );

            assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
            verify(userRepository, times(1)).findByEmail(anyString());
        }

        @Test
        public void 잘못된_비밀번호를_입력해서_에러_발생() {
            // given
            SigninRequest request = new SigninRequest("user@gmail.com", "PASSWORD2468");
            User user = spy(User.class);
            String encodedPassword = passwordEncoder.encode("PASSWORD1234");
            ReflectionTestUtils.setField(user, "password", encodedPassword);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

            // when & then
            AuthException exception = assertThrows(AuthException.class, () ->
                    authService.signin(request)
            );

            assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
            verify(userRepository, times(1)).findByEmail(anyString());
            assertFalse(passwordEncoder.matches(request.getPassword(), user.getPassword()));
        }

        @Test
        public void 로그인_성공() {
            // given
            SigninRequest request = new SigninRequest("user@gmail.com", "PASSWORD1234");
            User user = new User("user@gmail.com", null, UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);
            String encodedPassword = passwordEncoder.encode("PASSWORD1234");
            ReflectionTestUtils.setField(user, "password", encodedPassword);
            ReflectionTestUtils.setField(jwtUtil, "secretKey", "7Iqk7YyM66W07YOA7L2U65Sp7YG065+9U3ByaW5n6rCV7J2Y7Yqc7YSw7LWc7JuQ67mI7J6F64uI64ukLg==");

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            ReflectionTestUtils.invokeMethod(jwtUtil, "init");

            // when
            SigninResponse response = authService.signin(request);

            // then
            verify(userRepository, times(1)).findByEmail(anyString());
            assertTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()));
            verify(jwtUtil, times(1)).createToken(anyLong(), anyString(), any(UserRole.class));
            assertNotNull(response);
        }
    }
}
