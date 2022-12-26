package likelion.sns.controller.restController;

import likelion.sns.domain.Response;
import likelion.sns.domain.dto.delete.PostDeleteResponseDto;
import likelion.sns.domain.dto.modify.PostModifyRequestDto;
import likelion.sns.domain.dto.modify.PostModifyResponseDto;
import likelion.sns.domain.dto.write.PostWriteRequestDto;
import likelion.sns.domain.dto.read.PostDetailDto;
import likelion.sns.domain.dto.read.PostListDto;
import likelion.sns.domain.dto.write.PostWriteResponseDto;
import likelion.sns.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("api/v1/posts")
@RequiredArgsConstructor
@Slf4j
public class PostRestController {

    private final PostService postService;

    /**
     포스트 리스트 조회 (최신 글 순서로 정렬)
     **/
    @GetMapping("")
    public Response showPostList(Pageable pageable) throws SQLException {
        return Response.success(postService.getPostList(pageable));
    }

    /**
     포스트 단건 상세 조회
     **/
    @GetMapping("/{postId}")
    public Response showOne(@PathVariable(name = "postId") Long id) throws SQLException {
        return Response.success(postService.getPostById(id));
    }

    /**
     포스트 작성
     **/
    @PostMapping
    public Response write(@RequestBody PostWriteRequestDto requestDto, Authentication authentication) throws SQLException {

        log.info("{}", requestDto);


        String requestUserName = authentication.getName();
        log.info("작성 요청자 userName : {}", requestUserName);

        PostWriteResponseDto responseDto = postService.writePost(requestDto, requestUserName);
        log.info("{}", responseDto);

        return Response.success(responseDto);
    }

    /**
     포스트 수정 (제목, 내용)
     **/
    @PutMapping("/{postId}")
    public Response modify(@PathVariable(name = "postId") Long postId, @RequestBody PostModifyRequestDto requestDto, Authentication authentication) throws SQLException {

        log.info("{}", requestDto);

        String requestUserName = authentication.getName();
        log.info("수정 요청자 userName : {}", requestUserName);


        postService.modifyPost(requestDto, postId, requestUserName);

        PostModifyResponseDto responseDto = new PostModifyResponseDto(postId);
        log.info("{}", responseDto);

        return Response.success(responseDto);
    }

    /**
     포스트 삭제
     **/
    @DeleteMapping("/{postId}")
    public Response delete(@PathVariable(name = "postId") Long postId, Authentication authentication) throws SQLException {

        String requestUserName = authentication.getName();
        log.info("삭제 요청자 userName : {}", requestUserName);

        postService.deletePost(postId, requestUserName);
        PostDeleteResponseDto responseDto = new PostDeleteResponseDto(postId);
        log.info("{}", responseDto);

        return Response.success(responseDto);
    }
}
