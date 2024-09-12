package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    @Spy
    private CommentService commentService;

    @Nested
    class SaveCommentTest {
        @Test
        public void 댓글_등록_중_할일을_찾지_못해_에러가_발생한다() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                commentService.saveComment(authUser, todoId, request);
            });

            // then
            assertEquals("Todo not found", exception.getMessage());
        }

        @Test
        public void 담당자가_없을_때_인증사용자가_할일_담당자가_아니라서_에러_발생() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
            Todo todo = spy(Todo.class);

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentService.saveComment(authUser, todoId, request));


            // then
            assertEquals("Not manager of this todo", exception.getMessage());
        }

        @Test
        public void 담당자가_있을_때_인증사용자가_할일_담당자가_아니라서_에러_발생() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "user@gmail.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "contents", "sunny", new User());
            Manager manager = spy(Manager.class);
            ReflectionTestUtils.setField(manager, "id", 2L);
            ReflectionTestUtils.setField(todo, "managers", List.of(
                    manager
            ));

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentService.saveComment(authUser, todoId, request));


            // then
            assertEquals("Not manager of this todo", exception.getMessage());
        }

        @Test
        public void 댓글_등록_성공() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "user@gmail.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "contents", "sunny", new User());
            Manager manager = spy(Manager.class);
            ReflectionTestUtils.setField(manager, "id", 1L);
            ReflectionTestUtils.setField(todo, "managers", List.of(
                    manager
            ));
            Comment savedComment = new Comment(request.getContents(), user, todo);
            ReflectionTestUtils.setField(savedComment, "id", 1L);

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

            // when
            CommentSaveResponse response = commentService.saveComment(authUser, todoId, request);

            // then
            verify(commentRepository, times(1)).save(any(Comment.class));
            assertNotNull(response);

        }
    }

    @Nested
    class GetCommentsTest {
        @Test
        public void 댓글_0개일_때_댓글_전체_조회_성공() {
            // given
            long todoId = 1L;
            List<Comment> commentList = new ArrayList<>();

            given(commentRepository.findByTodoIdWithUser(anyLong())).willReturn(commentList);

            // when
            List<CommentResponse> commentResponseList = commentService.getComments(todoId);

            // then
            verify(commentRepository, times(1)).findByTodoIdWithUser(anyLong());
            assertNotNull(commentResponseList);
            assertEquals(0, commentResponseList.size());
        }

        @Test
        public void 댓글_1개이상일_때_댓글_전체_조회_성공() {
            // given
            long todoId = 1L;
            User user = spy(User.class);
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(user, "email", "user@gmail.com");
            Comment comment = new Comment("contents", user, new Todo());
            List<Comment> commentList = List.of(comment);

            given(commentRepository.findByTodoIdWithUser(anyLong())).willReturn(commentList);

            // when
            List<CommentResponse> commentResponseList = commentService.getComments(todoId);

            // then
            verify(commentRepository, times(1)).findByTodoIdWithUser(anyLong());
            assertNotNull(commentResponseList);
            assertEquals(1, commentResponseList.size());
        }
    }

    @Nested
    class DeleteComments {
        @Test
        public void 할일이_없어서_에러_발생() {
            // given
            long todoId = 1L;

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentService.deleteComments(todoId));

            // then
            assertEquals("Todo not found", exception.getMessage());
        }

        @Test
        public void 댓글_삭제_성공() {
            // given
            long todoId = 1L;

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(new Todo()));
            doNothing().when(commentRepository).deleteAll(anyList());
            doNothing().when(commentService).showThrow();

            // when
            commentService.deleteComments(todoId);

            // then
            verify(commentRepository, times(1)).deleteAll(anyList());
        }
    }

    @Nested
    @DisplayName(value = "특강 때, 작성한 코드 테스트")
    class ExtraMethodTest {
        @Test
        public void 무조건_예외_던지기_성공() {
            // given

            // when
            RuntimeException exception = assertThrows(RuntimeException.class, () -> commentService.showThrow());

            // then
            assertEquals("강제 오류 발생", exception.getMessage());
        }

        @Test
        @DisplayName(value = "진짜 객체와 spy 객체 차이 테스트")
        public void simple_spy() {
            Comment commentReal = new Comment();
            Comment commentSpy = spy(Comment.class);
            given(commentSpy.getId()).willReturn(1L);

            assertNull(commentReal.getId());
            assertEquals(1, commentSpy.getId());
        }
    }
}
