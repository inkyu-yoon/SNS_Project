package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.domain.Response;
import likelion.sns.domain.dto.comment.delete.CommentDeleteResponseDto;
import likelion.sns.domain.dto.comment.modify.CommentModifyRequestDto;
import likelion.sns.domain.dto.comment.modify.CommentModifyResponseDto;
import likelion.sns.domain.dto.comment.write.CommentWriteRequestDto;
import likelion.sns.domain.dto.comment.write.CommentWriteResponseDto;
import likelion.sns.domain.dto.post.delete.PostDeleteResponseDto;
import likelion.sns.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.sql.SQLException;

@RestController
@RequestMapping("api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@Api(tags = {"Comment API"})
public class CommentRestController {

    private final CommentService commentService;

    /**
     * 해당 postId의 댓글 리스트 조회 (최신순으로 정렬)
     */
    @GetMapping("/{postId}/comments")
    @ApiOperation(value = "Comment 리스트 조회", notes = "작성된 댓글을 최신순으로 10개씩 페이징 해서 가져온다.")
    public Response showCommentList(@PathVariable(name = "postId") Long postId, @ApiIgnore @PageableDefault Pageable pageable) throws SQLException {
        log.info("postId : {}", postId);
        return Response.success(commentService.getCommentList(postId, pageable));
    }

    /**
     * 해당 postId에 댓글 작성
     */
    @PostMapping("/{postId}/comments")
    @ApiOperation(value = "Comment 작성", notes = "Path variable에 해당하는 포스트에, 입력한 comment 내용을 저장")
    public Response write(@PathVariable(name = "postId") Long postId, @RequestBody CommentWriteRequestDto requestDto, @ApiIgnore Authentication authentication) throws SQLException {
        log.info("{}", requestDto);

        String requestUserName = authentication.getName();
        log.info("작성 요청자 userName : {}", requestUserName);

        CommentWriteResponseDto responseDto = commentService.writeComment(requestDto, requestUserName, postId);
        log.info("{}", responseDto);

        return Response.success(responseDto);
    }

    /**
     * 댓글 수정 (특정 postId에 해당하는 게시글의 특정 commentId의 내용)
     **/
    @ApiOperation(value = "Comment 수정", notes = "(유효한 jwt Token 필요) path variable로 입력한 postId의 Post의 commentId의 내용을 수정")
    @PutMapping("/{postId}/comments/{commentId}")
    public Response modify(@PathVariable(name = "postId") Long postId, @PathVariable(name = "commentId") Long commentId, @RequestBody CommentModifyRequestDto requestDto, @ApiIgnore Authentication authentication) throws SQLException {

        log.info("{}", requestDto);

        String requestUserName = authentication.getName();
        log.info("댓글 수정 요청자 userName : {}", requestUserName);

        // 수정 적용
        commentService.modifyComment(requestDto, postId, commentId, requestUserName);

        // 수정 날짜가, Transaction이 종료되어야 적용되므로, 수정을 끝낸 후, 가져오는 메서드를 한번더 사용.
        CommentModifyResponseDto responseDto = commentService.getOneComment(postId, commentId, requestUserName);

        log.info("{}", responseDto);

        return Response.success(responseDto);
    }

    /**
     * 댓글 삭제
     **/
    @ApiOperation(value="Comment 삭제", notes="(유효한 jwt Token 필요) path variable로 입력한 postId의 Post의 commentId의 Comment를 삭제")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public Response delete(@PathVariable(name = "postId") Long postId, @PathVariable(name = "commentId") Long commentId, @ApiIgnore Authentication authentication) throws SQLException {

        String requestUserName = authentication.getName();
        log.info("삭제 요청자 userName : {}", requestUserName);

        // 댓글 삭제
        commentService.deleteComment(postId, commentId, requestUserName);

        CommentDeleteResponseDto responseDto = new CommentDeleteResponseDto(commentId);
        log.info("{}", responseDto);

        return Response.success(responseDto);
    }
}
