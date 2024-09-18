package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Nested
    class SaveManagerTest {
        @Test
        public void 할일을_조회할_수_없어서_에러_발생() {
            // given
            AuthUser authUser = new AuthUser(1L, "user@gmail.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;
            ManagerSaveRequest request = new ManagerSaveRequest(managerUserId);

            given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> managerService.saveManager(authUser, todoId, request));

            verify(todoRepository, times(1)).findById(todoId);
            assertEquals("Todo not found", exception.getMessage());
        }


        @Test
        void todo의_user가_null인_경우_예외가_발생한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "user@gmail.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;

            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo, "user", null);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.saveManager(authUser, todoId, managerSaveRequest)
            );

            assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
        }

        @Test
        public void 인증_사용자와_일정_작성자가_달라서_에러_발생() {
            // given
            AuthUser authUser = new AuthUser(1L, "user@gmail.com", UserRole.USER);
            long todoId = 1L;
            long managerId = 2L;

            Todo todo = new Todo();
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", 2L);
            ReflectionTestUtils.setField(todo, "user", user);

            ManagerSaveRequest request = new ManagerSaveRequest(managerId);


            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> managerService.saveManager(authUser, todoId, request));

            assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.",  exception.getMessage());
            verify(todoRepository, times(1)).findById(todoId);
        }

        @Test
        public void 담당자를_조회할_수_없어서_에러_발생() {
            // given
            AuthUser authUser = new AuthUser(1L, "user@gmail.com", UserRole.USER);
            long todoId = 1L;
            long managerId = 2L;

            Todo todo = new Todo();
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(todo, "user", user);

            ManagerSaveRequest request = new ManagerSaveRequest(managerId);


            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> managerService.saveManager(authUser, todoId, request));

            assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.",  exception.getMessage());
            verify(todoRepository, times(1)).findById(todoId);
            verify(userRepository, times(1)).findById(managerId);
        }

        @Test
        public void 일정작성자와_등록하려는_담당자가_같아서_에러_발생() {
            // given
            AuthUser authUser = new AuthUser(1L, "user@gmail.com", UserRole.USER);
            long todoId = 1L;
            long managerId = 1L;

            Todo todo = new Todo();
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(todo, "user", user);

            ManagerSaveRequest request = new ManagerSaveRequest(managerId);
            User managerUser = spy(User.class);
            ReflectionTestUtils.setField(managerUser, "id", managerId);

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(userRepository.findById(anyLong())).willReturn(Optional.of(managerUser));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> managerService.saveManager(authUser, todoId, request));

            assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.",  exception.getMessage());
            verify(todoRepository, times(1)).findById(todoId);
            verify(userRepository, times(1)).findById(managerId);
        }

        @Test // 테스트코드 샘플
        void todo가_정상적으로_등록된다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

            long todoId = 1L;
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

            long managerUserId = 2L;
            User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
            ReflectionTestUtils.setField(managerUser, "id", managerUserId);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
            given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

            // then
            assertNotNull(response);
            assertEquals(managerUser.getId(), response.getUser().getId());
            assertEquals(managerUser.getEmail(), response.getUser().getEmail());
        }
    }

    @Nested
    class GetManagersTest {
        @Test
        public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
            // given
            long todoId = 1L;
            given(todoRepository.findById(todoId)).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> managerService.getManagers(todoId));
            assertEquals("Todo not found", exception.getMessage());
        }

        @Test // 테스트코드 샘플
        public void manager_목록_조회에_성공한다() {
            // given
            long todoId = 1L;
            User user = new User("user1@example.com", "password", UserRole.USER);
            Todo todo = new Todo("Title", "Contents", "Sunny", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            Manager mockManager = new Manager(todo.getUser(), todo);
            List<Manager> managerList = List.of(mockManager);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

            // when
            List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

            // then
            assertEquals(1, managerResponses.size());
            assertEquals(mockManager.getId(), managerResponses.get(0).getId());
            assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
        }
    }

    @Nested
    class DeleteManagerTest {
        @Test
        public void 사용자를_찾지_못해서_에러_발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 2L;

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(userId, todoId, managerId));

            assertEquals("User not found", exception.getMessage());
        }

        @Test
        public void 할일을_찾지_못해서_에러_발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 2L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", userId);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(userId, todoId, managerId));

            assertEquals("Todo not found", exception.getMessage());
            verify(userRepository, times(1)).findById(userId);
        }

        @Test
        public void 할일작성자가_null이라서_에러_발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 2L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = spy(Todo.class);
            ReflectionTestUtils.setField(todo, "user", null);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(userId, todoId, managerId));

            assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
            verify(userRepository, times(1)).findById(userId);
            verify(todoRepository, times(1)).findById(todoId);
            assertTrue(todo.getUser() == null);
        }

        @Test
        public void 인증사용자와_일정작성자가_달라서_에러_발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 2L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = spy(Todo.class);
            User todoUser = spy(User.class);
            ReflectionTestUtils.setField(todoUser, "id", 2L);
            ReflectionTestUtils.setField(todo, "user", todoUser);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(userId, todoId, managerId));

            assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
            verify(userRepository, times(1)).findById(userId);
            verify(todoRepository, times(1)).findById(todoId);
            assertFalse(todo.getUser() == null);
            assertFalse(ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId()));
        }

        @Test
        public void 담당자를_찾지_못해서_에러_발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 2L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = spy(Todo.class);
            User todoUser = spy(User.class);
            ReflectionTestUtils.setField(todoUser, "id", 1L);
            ReflectionTestUtils.setField(todo, "user", todoUser);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(managerRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(userId, todoId, managerId));

            assertEquals("Manager not found", exception.getMessage());
            verify(userRepository, times(1)).findById(userId);
            verify(todoRepository, times(1)).findById(todoId);
            assertFalse(todo.getUser() == null);
            assertTrue(ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId()));
        }

        @Test
        public void 할일에_등록된_담당자가_아니라서_에러_발생() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 2L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = spy(Todo.class);
            User todoUser = spy(User.class);
            ReflectionTestUtils.setField(todoUser, "id", 1L);
            ReflectionTestUtils.setField(todo, "user", todoUser);
            Manager manager = spy(Manager.class);
            Todo managerTodo = spy(Todo.class);
            ReflectionTestUtils.setField(managerTodo, "id", 2L);
            ReflectionTestUtils.setField(manager, "todo", managerTodo);
            

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(userId, todoId, managerId));

            assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
            verify(userRepository, times(1)).findById(userId);
            verify(todoRepository, times(1)).findById(todoId);
            verify(managerRepository, times(1)).findById(managerId);
            assertFalse(ObjectUtils.nullSafeEquals(todo.getId(), manager.getTodo().getId()));
        }

        @Test
        public void 담당자_삭제_성공() {
            // given
            long userId = 1L;
            long todoId = 1L;
            long managerId = 2L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = spy(Todo.class);
            User todoUser = spy(User.class);
            ReflectionTestUtils.setField(todoUser, "id", 1L);
            ReflectionTestUtils.setField(todo, "id", todoId);
            ReflectionTestUtils.setField(todo, "user", todoUser);
            Manager manager = spy(Manager.class);
            Todo managerTodo = spy(Todo.class);
            ReflectionTestUtils.setField(managerTodo, "id", 1L);
            ReflectionTestUtils.setField(manager, "todo", managerTodo);


            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));
            doNothing().when(managerRepository).delete(manager);

            // when
            managerService.deleteManager(userId, todoId, managerId);

            // then
            verify(userRepository, times(1)).findById(userId);
            verify(todoRepository, times(1)).findById(todoId);
            verify(managerRepository, times(1)).findById(managerId);
            verify(managerRepository, times(1)).delete(manager);
        }
    }



}
