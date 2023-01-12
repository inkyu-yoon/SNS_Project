package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.Exception.ExceptionManager;
import likelion.sns.domain.Response;
import likelion.sns.domain.dto.comment.delete.CommentDeleteResponseDto;
import likelion.sns.domain.dto.comment.modify.CommentModifyRequestDto;
import likelion.sns.domain.dto.comment.modify.CommentModifyResponseDto;
import likelion.sns.domain.dto.comment.write.CommentWriteRequestDto;
import likelion.sns.domain.dto.comment.write.CommentWriteResponseDto;
import likelion.sns.domain.dto.comment.write.ReplyCommentWriteRequestDto;
import likelion.sns.domain.dto.comment.write.ReplyCommentWriteResponseDto;
import likelion.sns.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
        log.info("💬댓글을 조회하려는 게시글 id : {}", postId);

        //댓글 조회
        return Response.success(commentService.getCommentList(postId, pageable));
    }

    /**
     * 해당 postId에 댓글 작성
     */
    @PostMapping("/{postId}/comments")
    @ApiOperation(value = "Comment 작성", notes = "Path variable에 해당하는 포스트에, 입력한 comment 내용을 저장")
    public ResponseEntity createComment(@PathVariable(name = "postId") Long postId, @Validated @RequestBody CommentWriteRequestDto requestDto, BindingResult br, @ApiIgnore Authentication authentication) throws SQLException {
        log.info("💬댓글을 작성하려는 게시글 id : {} || requestDto : {}", postId, requestDto);

        // request dto 바인딩 조건 처리
        // requestDto의 값이 null이나 공백인 값으로 요청할 시, BLANK_NOT_ALLOWED 에러 메세지 출력
        if (br.hasErrors()) {
            return ExceptionManager.ifNullAndBlank();
        }
        String requestUserName = authentication.getName();

        log.info("💬댓글 작성 요청자 userName : {} ", requestUserName);

        // 댓글 작성
        CommentWriteResponseDto responseDto = commentService.writeComment(requestDto, requestUserName, postId);

        return ResponseEntity.ok(Response.success(responseDto));

    }

    /**
     * 해당 postId의 commentId 에 해당하는 댓글에 댓글의 댓글 작성
     */
    @PostMapping("/{postId}/comments/{commentId}")
    @ApiOperation(value = "Reply Comment(댓글의 댓글) 작성", notes = "Path variable에 해당하는 포스트의 코멘트에, 입력한 replyComment 내용을 저장")
    public ResponseEntity createReplyComment(@PathVariable(name = "postId") Long postId, @PathVariable(name = "commentId") Long parentCommentId, @Validated @RequestBody ReplyCommentWriteRequestDto requestDto, BindingResult br, @ApiIgnore Authentication authentication) throws SQLException {
        log.info("💬💬대댓글을 작성하려는 게시글 id : {} 댓글 id : {}", postId, parentCommentId);
        log.info("💬💬대댓글 작성 requestDto : {}", requestDto);
        // request dto 바인딩 조건 처리
        if (br.hasErrors()) {
            return ExceptionManager.ifNullAndBlank();
        }

        String requestUserName = authentication.getName();
        log.info("💬💬대댓글 작성 요청자 userName : {}", requestUserName);

        //대댓글 작성
        ReplyCommentWriteResponseDto responseDto = commentService.writeReplyComment(requestDto, requestUserName, postId, parentCommentId);

        return ResponseEntity.ok(Response.success(responseDto));

    }

    /**
     * 댓글 수정 (특정 postId에 해당하는 게시글의 특정 commentId의 내용)
     **/
    @ApiOperation(value = "Comment 수정", notes = "(유효한 jwt Token 필요) path variable로 입력한 postId의 Post의 commentId의 내용을 수정")
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity modify(@PathVariable(name = "postId") Long postId, @PathVariable(name = "commentId") Long commentId, @Validated @RequestBody CommentModifyRequestDto requestDto, BindingResult br, @ApiIgnore Authentication authentication) throws SQLException {
        log.info("💬댓글을 수정하려는 게시글 id : {} 댓글 id : {}", postId, commentId);
        log.info("💬댓글 수정 requestDto : {}", requestDto);

        // request dto 바인딩 조건 처리
        if (br.hasErrors()) {
            return ExceptionManager.ifNullAndBlank();
        }

        String requestUserName = authentication.getName();

        log.info("💬댓글 수정 요청자 userName : {}", requestUserName);

        // 수정 적용
        CommentModifyResponseDto responseDto = commentService.modifyComment(requestDto, postId, commentId, requestUserName);

        return ResponseEntity.ok(Response.success(responseDto));

    }

    /**
     * 댓글 삭제
     **/
    @ApiOperation(value = "Comment 삭제", notes = "(유효한 jwt Token 필요) path variable로 입력한 postId의 Post의 commentId의 Comment를 삭제")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public Response delete(@PathVariable(name = "postId") Long postId, @PathVariable(name = "commentId") Long commentId, @ApiIgnore Authentication authentication) throws SQLException {
        log.info("💬댓글을 삭제하려는 게시글 id : {} 댓글 id : {}", postId, commentId);

        String requestUserName = authentication.getName();
        log.info("💬댓글 삭제 요청자 userName : {}", requestUserName);

        // 댓글 삭제
        commentService.deleteComment(postId, commentId, requestUserName);

        return Response.success(new CommentDeleteResponseDto(commentId));
    }


}
