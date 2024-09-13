package org.example.expert.domain.todo;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Nested
    class SaveTodoTest {
        @Test
        public void 일정_저장_성공() {
            // given
            AuthUser authUser = new AuthUser(1L, "user@gmail.com", UserRole.USER);
            TodoSaveRequest request = new TodoSaveRequest("title", "contents");
            User user = User.fromAuthUser(authUser);
            String weather = "sunny";
            given(weatherClient.getTodayWeather()).willReturn(weather);

            Todo savedTodo = new Todo(
                    request.getTitle(),
                    request.getContents(),
                    weather,
                    user
            );

            given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

            // when
            TodoSaveResponse response = todoService.saveTodo(authUser, request);

            // then
            verify(weatherClient, times(1)).getTodayWeather();
            verify(todoRepository, times(1)).save(any(Todo.class));
            assertNotNull(response);
            assertEquals(savedTodo.getId(), response.getId());
            assertEquals(savedTodo.getTitle(), response.getTitle());
            assertEquals(savedTodo.getContents(), response.getContents());
            assertEquals(savedTodo.getWeather(), response.getWeather());
            assertEquals(user.getId(), response.getUser().getId());
            assertEquals(user.getEmail(), response.getUser().getEmail());
        }
    }

    @Nested
    class GetTodosTest {
        @Test
        public void 일정_전체_조회_성공() {
            // given
            int page = 2;
            int size = 10;
            User user = User.fromAuthUser(new AuthUser(1L, "user@gmail.com", UserRole.USER));
            Pageable pageable = PageRequest.of(page - 1, size);
            Todo todo = new Todo("title", "contents", "sunny", user);
            ReflectionTestUtils.setField(todo, "id", 1L);
            List<Todo> todoList = List.of(todo);
            Page<Todo> todos = new PageImpl<>(todoList, pageable, todoList.size());

            given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todos);

            // when
            Page<TodoResponse> todoResponses = todoService.getTodos(page, size);

            // then
            verify(todoRepository, times(1)).findAllByOrderByModifiedAtDesc(pageable);
            assertNotNull(todoResponses);
            assertEquals(todoList.size(), todoResponses.getNumberOfElements());
            assertEquals(size, todoResponses.getSize());
        }
    }

    @Nested
    class GetTodoTest {

        @Test
        public void 할일이_없어서_에러_발생() {
            // given
            long todoId = 1L;
            given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> todoService.getTodo(todoId));

            // then
            assertEquals("Todo not found", exception.getMessage());
        }

        @Test
        public void 일정_조회_성공() {
            // given
            long todoId = 1L;
            long userId = 2L;

            User user = new User("user@gmail.com", "PASSWORD1234", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            Todo todo = new Todo("title", "contents", "sunny", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

            // when
            TodoResponse todoResponse = todoService.getTodo(todoId);

            // then
            verify(todoRepository, times(1)).findByIdWithUser(anyLong());
            assertNotNull(todoResponse);
            assertEquals(todo.getId(), todoResponse.getId());
            assertEquals(todo.getTitle(), todoResponse.getTitle());
            assertEquals(todo.getContents(),todoResponse.getContents());
            assertEquals(todo.getWeather(), todoResponse.getWeather());
            assertEquals(user.getId(), todoResponse.getUser().getId());
            assertEquals(user.getEmail(), todoResponse.getUser().getEmail());
            assertEquals(todo.getCreatedAt(), todoResponse.getCreatedAt());
            assertEquals(todo.getModifiedAt(), todoResponse.getModifiedAt());
        }
    }
}
