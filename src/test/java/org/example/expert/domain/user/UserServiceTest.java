package org.example.expert.domain.user;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder;

    @Spy
    @InjectMocks
    private UserService userService;

    @Nested
    class ChangePasswordTest {
        @ParameterizedTest
        @ValueSource(strings = {"1A", "12A", "123A", "1234A", "12345A", "123456A"})
        public void 새_비밀번호길이가_8보다_짧아서_에러_발생(String newPassword) {
            // given
            long userId = 1L;
            UserChangePasswordRequest request = spy(UserChangePasswordRequest.class);
            given(request.getNewPassword()).willReturn(newPassword);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertTrue(request.getNewPassword().length() < 8);
            assertFalse(!request.getNewPassword().matches(".*\\d.*"));
            assertFalse(!request.getNewPassword().matches(".*[A-Z].*"));
            assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
        }

        @Test
        public void 새_비밀번호에_숫자가_안들어가서_에러_발생() {
            // given
            long userId = 1L;
            String newPassword = "ABCDEFGH";
            UserChangePasswordRequest request = spy(UserChangePasswordRequest.class);
            given(request.getNewPassword()).willReturn(newPassword);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertFalse(request.getNewPassword().length() < 8);
            assertTrue(!request.getNewPassword().matches(".*\\d.*"));
            assertFalse(!request.getNewPassword().matches(".*[A-Z].*"));
            assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
        }

        @Test
        public void 새_비밀번호에_영어가_안들어가서_에러_발생() {
            // given
            long userId = 1L;
            String newPassword = "0123456789";
            UserChangePasswordRequest request = spy(UserChangePasswordRequest.class);
            given(request.getNewPassword()).willReturn(newPassword);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertFalse(request.getNewPassword().length() < 8);
            assertFalse(!request.getNewPassword().matches(".*\\d.*"));
            assertTrue(!request.getNewPassword().matches(".*[A-Z].*"));
            assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
        }

        @Test
        public void 새_비밀번호길이에_숫자와_영어_대문자를_포함하지_않아서_에러_발생() {
            // given
            long userId = 1L;
            String newPassword = "abcdefgh";
            UserChangePasswordRequest request = spy(UserChangePasswordRequest.class);
            given(request.getNewPassword()).willReturn(newPassword);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertFalse(request.getNewPassword().length() < 8);
            assertTrue(!request.getNewPassword().matches(".*\\d.*"));
            assertTrue(!request.getNewPassword().matches(".*[A-Z].*"));
            assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"1", "12", "123", "1234", "12345", "123456", "1234567"})
        public void 새_비밀번호길이가_8보다_짧고_영어_대문자를_포함하지_않아서_에러_발생(String newPassword) {
            // given
            long userId = 1L;
            UserChangePasswordRequest request = spy(UserChangePasswordRequest.class);
            given(request.getNewPassword()).willReturn(newPassword);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertTrue(request.getNewPassword().length() < 8);
            assertFalse(!request.getNewPassword().matches(".*\\d.*"));
            assertTrue(!request.getNewPassword().matches(".*[A-Z].*"));
            assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"A", "AB", "ABC", "ABCD", "ABCDE", "ABCDEF", "ABCDEFG"})
        public void 새_비밀번호길이가_8보다_짧고_숫자를_포함하지_않아서_에러_발생(String newPassword) {
            // given
            long userId = 1L;
            UserChangePasswordRequest request = spy(UserChangePasswordRequest.class);
            given(request.getNewPassword()).willReturn(newPassword);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertTrue(request.getNewPassword().length() < 8);
            assertTrue(!request.getNewPassword().matches(".*\\d.*"));
            assertFalse(!request.getNewPassword().matches(".*[A-Z].*"));
            assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
        }

        @Test
        public void 사용자가_없어서_에러_발생() {
            // given
            long userId = 1L;
            UserChangePasswordRequest request = spy(UserChangePasswordRequest.class);
            ReflectionTestUtils.setField(request, "newPassword", "ABCDE12345");

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        public void 새_비밀번호가_기존_비밀번호와_같아서_에러_발생() {
            // given
            long userId = 1L;
            String oldPassword = passwordEncoder.encode("oldPASSWORD123");
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "password", oldPassword);
            UserChangePasswordRequest request = spy(UserChangePasswordRequest.class);
            ReflectionTestUtils.setField(request, "newPassword", "oldPASSWORD123");

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
        }

        @Test
        public void 잘못된_비밀번호를_입력해서_에러_발생() {
            // given
            long userId = 1L;
            String oldPassword = passwordEncoder.encode("oldPASSWORD123");
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "password", oldPassword);
            UserChangePasswordRequest request = new UserChangePasswordRequest("wrongPASSWORD123", "newPASSWORD123");

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));

            // then
            assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
        }

        @Test
        public void 비밀번호_변경_성공() {
            // given
            long userId = 1L;
            String oldPassword = passwordEncoder.encode("oldPASSWORD123");
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "password", oldPassword);
            UserChangePasswordRequest request = new UserChangePasswordRequest("oldPASSWORD123", "newPASSWORD123");

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            userService.changePassword(userId, request);

            // then
            assertTrue(passwordEncoder.matches(request.getNewPassword(), user.getPassword()));
        }
    }


    @Nested
    class GetUserTest {
        @Test
        public void 사용자가_없어서_에러_발생() {
            // given
            long userId = 1L;

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.getUser(userId));

            // then
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        public void 사용자_조회_성공() {
            // given
            long userId = 1L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", userId);
            ReflectionTestUtils.setField(user, "email", "user@gmail.com");

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            UserResponse response = userService.getUser(userId);
            assertEquals(user.getId(), response.getId());
            assertEquals(user.getEmail(), response.getEmail());
        }
    }
}
