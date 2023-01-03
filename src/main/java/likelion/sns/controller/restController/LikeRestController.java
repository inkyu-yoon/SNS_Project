package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.domain.Response;
import likelion.sns.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.sql.SQLException;

@RestController
@RequestMapping("api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@Api(tags = {"Like API"})
public class LikeRestController {

    private final LikeService likeService;

    /**
     * 좋아요 입력하기 ( 한 계정당 한개만 가능)
     */
    @ApiOperation(value="좋아요 추가", notes="(유효한 jwt Token 필요), 한 계정당 한개만 누를 수 있다.")
    @PostMapping("/{postId}/likes")
    public Response giveLike(@PathVariable(name = "postId") Long postId, @ApiIgnore Authentication authentication) throws SQLException {
        String requestUserName = authentication.getName();
        log.info("좋아요 요청자 userName : {}", requestUserName);

        likeService.AddLike(postId,requestUserName);
        return Response.success("좋아요를 눌렀습니다.");
    }

    /**
     * 좋아요 갯수 구하기
     */
    @ApiOperation(value="좋아요 개수 구하기", notes="해당하는 postId의 좋아요 개수를 출력한다.")
    @GetMapping("/{postId}/likes")
    public Response countLike(@PathVariable(name = "postId") Long postId) throws SQLException {
        Long likesCount = likeService.getLikesCount(postId);

        return Response.success(likesCount);
    }
}
