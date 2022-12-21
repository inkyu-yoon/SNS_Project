package likelion.sns.controller;

import likelion.sns.domain.dto.write.PostWriteRequestDto;
import likelion.sns.domain.dto.read.PostDetailDto;
import likelion.sns.domain.dto.read.PostListDto;
import likelion.sns.repository.PostRepository;
import likelion.sns.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("")
    public Page<PostListDto> showPostList(Pageable pageable) {
        return postService.getPostList(pageable);
    }

    @GetMapping("/{postId}")
    public PostDetailDto showOne(@PathVariable(name = "postId") Long id) {
        return postService.getPostById(id);
    }

}
