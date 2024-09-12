package org.example.expert.domain.user;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAdminServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Nested
    class ChangeUserRoleTest {
        @Test
        public void 사용자가_없어서_에러_발생() {
            // given
            long userId = 1L;
            UserRoleChangeRequest request = spy(UserRoleChangeRequest.class);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userAdminService.changeUserRole(userId, request));

            // then
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        public void 사용자_권한_변경_성공() {
            // given
            long userId = 1L;
            UserRoleChangeRequest request = spy(UserRoleChangeRequest.class);
            ReflectionTestUtils.setField(request, "role", "USER");
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "userRole", UserRole.ADMIN);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            userAdminService.changeUserRole(userId, request);

            //then
            verify(user, times(1)).updateRole(UserRole.of(request.getRole()));
            assertEquals(UserRole.of(request.getRole()), user.getUserRole());
        }
    }
}
