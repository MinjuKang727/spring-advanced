package org.example.expert.domain.auth;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    }
}
