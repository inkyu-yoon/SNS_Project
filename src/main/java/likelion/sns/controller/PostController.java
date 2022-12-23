package likelion.sns.controller;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
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
public class PostController {

    private final PostService postService;

    @GetMapping("")
    public Page<PostListDto> showPostList(Pageable pageable) throws SQLException {
        return postService.getPostList(pageable);
    }

    @GetMapping("/{postId}")
    public PostDetailDto showOne(@PathVariable(name = "postId") Long id) throws SQLException {
        return postService.getPostById(id);
    }

    @PostMapping
    public Response write(@RequestBody PostWriteRequestDto postWriteRequestDto, Authentication authentication) throws SQLException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SNSAppException(ErrorCode.INVALID_PERMISSION);
        }

        String userName = authentication.getName();
        PostWriteResponseDto postWriteResponseDto = postService.writePost(postWriteRequestDto, userName);
        return Response.success(postWriteResponseDto);

    }

    @PutMapping("/{postId}")
    public Response modify(@PathVariable(name = "postId") Long postId, @RequestBody PostModifyRequestDto postModifyRequestDto, Authentication authentication) throws SQLException {

        //인증 실패
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SNSAppException(ErrorCode.INVALID_PERMISSION);
        }

        String requestUserName = authentication.getName();

        postService.modifyPost(postModifyRequestDto, postId, requestUserName);

        return Response.success(new PostModifyResponseDto("포스트 수정 완료", postId));
    }


    @DeleteMapping("/{postId}")
    public Response delete(@PathVariable(name = "postId") Long postId, Authentication authentication) throws SQLException {
        //인증 실패
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SNSAppException(ErrorCode.INVALID_PERMISSION);
        }

        String requestUserName = authentication.getName();

        postService.deletePost(postId, requestUserName);

        return Response.success(new PostDeleteResponseDto("포스트 삭제 완료", postId));
    }
}
