package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.domain.Response;
import likelion.sns.domain.dto.comment.write.CommentWriteRequestDto;
import likelion.sns.domain.dto.comment.write.CommentWriteResponseDto;
import likelion.sns.domain.dto.post.write.PostWriteRequestDto;
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

}
