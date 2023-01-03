package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.domain.Response;
import likelion.sns.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
