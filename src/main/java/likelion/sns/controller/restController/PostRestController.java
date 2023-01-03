package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.domain.Response;
import likelion.sns.domain.dto.post.delete.PostDeleteResponseDto;
import likelion.sns.domain.dto.post.modify.PostModifyRequestDto;
import likelion.sns.domain.dto.post.modify.PostModifyResponseDto;
import likelion.sns.domain.dto.post.read.PostListDto;
import likelion.sns.domain.dto.post.write.PostWriteRequestDto;
import likelion.sns.domain.dto.post.write.PostWriteResponseDto;
import likelion.sns.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.sql.SQLException;

@RestController
@RequestMapping("api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@Api(tags = {"Post API"})
public class PostRestController {

    private final PostService postService;

    /**
     * 포스트 리스트 조회 (최신 글 순서로 정렬)
     **/
    @ApiOperation(value="Post 리스트 조회", notes="작성된 게시글을 최신순으로 20개씩 페이징 해서 가져온다.")
    @GetMapping("")
    public Response showPostList(@ApiIgnore Pageable pageable) throws SQLException {
        return Response.success(postService.getPostList(pageable));
    }

    /**
     * 포스트 단건 상세 조회
     **/
    @ApiOperation(value="Post 단건 조회", notes="path variable로 입력한 postId의 상세 정보를 가져온다.")
    @GetMapping("/{postId}")
    public Response showOne(@PathVariable(name = "postId") Long id) throws SQLException {
        return Response.success(postService.getPostById(id));
    }

    /**
     * 포스트 작성
     **/
    @ApiOperation(value="Post 추가", notes="(유효한 jwt Token 필요) title, body 데이터를 저장")
    @PostMapping
    public Response write(@RequestBody PostWriteRequestDto requestDto, @ApiIgnore Authentication authentication) throws SQLException {

        log.info("{}", requestDto);


        String requestUserName = authentication.getName();
        log.info("작성 요청자 userName : {}", requestUserName);

        PostWriteResponseDto responseDto = postService.writePost(requestDto, requestUserName);
        log.info("{}", responseDto);

        return Response.success(responseDto);
    }

    /**
     * 포스트 수정 (제목, 내용)
     **/
    @ApiOperation(value="Post 수정", notes="(유효한 jwt Token 필요) path variable로 입력한 postId의 Post를 title, body 로 수정")
    @PutMapping("/{postId}")
    public Response modify(@PathVariable(name = "postId") Long postId, @RequestBody PostModifyRequestDto requestDto, @ApiIgnore Authentication authentication) throws SQLException {

        log.info("{}", requestDto);

        String requestUserName = authentication.getName();
        log.info("수정 요청자 userName : {}", requestUserName);


        postService.modifyPost(requestDto, postId, requestUserName);

        PostModifyResponseDto responseDto = new PostModifyResponseDto(postId);
        log.info("{}", responseDto);

        return Response.success(responseDto);
    }

    /**
     * 포스트 삭제
     **/
    @ApiOperation(value="Post 삭제", notes="(유효한 jwt Token 필요) path variable로 입력한 postId의 Post 삭제")
    @DeleteMapping("/{postId}")
    public Response delete(@PathVariable(name = "postId") Long postId, @ApiIgnore Authentication authentication) throws SQLException {

        String requestUserName = authentication.getName();
        log.info("삭제 요청자 userName : {}", requestUserName);

        postService.deletePost(postId, requestUserName);
        PostDeleteResponseDto responseDto = new PostDeleteResponseDto(postId);
        log.info("{}", responseDto);

        return Response.success(responseDto);
    }

    /**
     * 마이 피드 (작성한 게시글 모아보기)
     **/
    @ApiOperation(value="마이 피드 기능 (작성한 게시글 모아보기)", notes="(유효한 jwt Token 필요) 토큰 정보로 작성한 게시글 조회")
    @GetMapping("/my")
    public Response myFeed(@ApiIgnore Pageable pageable,@ApiIgnore Authentication authentication) throws SQLException {

        String requestUserName = authentication.getName();
        log.info("삭제 요청자 userName : {}", requestUserName);

        Page<PostListDto> myPosts = postService.getMyPosts(requestUserName, pageable);

        return Response.success(myPosts);
    }
}
