package likelion.sns.controller;

import likelion.sns.domain.dto.alarm.AlarmListDetailsDto;
import likelion.sns.domain.dto.comment.read.CommentListDto;
import likelion.sns.domain.dto.post.read.PostDetailDto;
import likelion.sns.domain.dto.post.read.PostListDto;
import likelion.sns.service.AlarmService;
import likelion.sns.service.CommentService;
import likelion.sns.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
@ApiIgnore
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final AlarmService alarmService;

    /**
     * 게시글 리스트 (페이지)
     **/
    @GetMapping("")
    public String searchList(@RequestParam(required = false) String keyword, @RequestParam(required = false) String condition, Model model, Pageable pageable, HttpServletRequest request) throws SQLException {
        log.info("🔍검색 조건 : {} || 검색 키워드 : {}", condition, keyword);
        Page<PostListDto> posts = null;


        // 검색 키워드가 있으면, 키워드로 page 구성, 검색 키워드가 없으면 전체 페이지
        if (condition != null) {
            if (condition.equals("제목 검색")) {
                posts = postService.getPostsByTitle(keyword, pageable);
                model.addAttribute("titleCon", "제목 검색");
            } else if (condition.equals("회원명 검색")) {
                posts = postService.getPostsByUserName(keyword, pageable);
                model.addAttribute("userNameCon", "회원명 검색");
            }
        } else {
            posts = postService.getPostList(pageable);
        }


        // 로그인 시, 화면에 로그인 회원명, 알림 전달
        showLoginUserNameAndAlarm(request, model, pageable);

        model.addAttribute("keyword", keyword);
        model.addAttribute("condition", condition);
        model.addAttribute("posts", posts);
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());
        return "posts/list";
    }

    /**
     * 게시글 상세 페이지
     **/
    @GetMapping("/{postId}")
    public String showDetail(@PathVariable(name = "postId") Long postId, Model model, Pageable pageable, HttpServletRequest request) throws SQLException {
        PostDetailDto post = postService.getPostById(postId);

        Page<CommentListDto> comments = commentService.getCommentListAsc(postId, pageable);

        //댓글에 대댓글 리스트 주입 + 개수 전달
        for (CommentListDto comment : comments) {
            List<CommentListDto> commentListDtos = commentService.getReplyCommentListAsc(postId, comment.getId(), pageable).toList();
            comment.setReplys(commentListDtos);
            comment.setReplysSize(commentListDtos.size());
        }

        // 로그인 시, 화면에 로그인 회원명, 알림 표시
        showLoginUserNameAndAlarm(request, model, pageable);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        return "posts/details";
    }

    /**
     * 게시글 작성
     **/
    @GetMapping("/write")
    public String writePost(Model model, HttpServletRequest request, Pageable pageable) {

        // 로그인 시, 화면에 로그인 회원명, 알림 표시
        showLoginUserNameAndAlarm(request, model, pageable);

        return "posts/write";
    }

    /**
     * 게시글 수정
     **/
    @GetMapping("/modify/{postId}")
    public String modifyPost(@PathVariable(name = "postId") Long postId, Model model, HttpServletRequest request, Pageable pageable) throws SQLException {
        PostDetailDto post = postService.getPostById(postId);

        // 로그인 시, 화면에 로그인 회원명, 알림 표시
        showLoginUserNameAndAlarm(request, model, pageable);

        model.addAttribute("post", post);
        return "posts/modify";
    }

    /**
     * 마이페이지
     **/
    @GetMapping("/my")
    public String modifyPost(Model model, HttpServletRequest request, Pageable pageable) throws SQLException {


        HttpSession session = request.getSession(true);

        // 로그인 시, 화면에 로그인 회원명, 알림 표시
        showLoginUserNameAndAlarm(request, model, pageable);

        // 특정 유저가 작성한 게시글만 list 에 담아서 전달
        if (session.getAttribute("userName") != null) {
            Object loginUserName = session.getAttribute("userName");
            Page<PostListDto> myPosts = postService.getMyPosts(loginUserName.toString(), pageable);
            model.addAttribute("myPosts", myPosts);
        }

        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());

        return "posts/myPage";
    }

    /**
     * 로그인 되어 있을 시, 상단 바에(nav bar)에 사용자 명과, 알람 목록 전달
     * 로그인 여부는 세션에 회원명이 저장되어 있는지로 확인
     * 서비스는 토큰 존재 여부, 만료 여부 등 유효성으로 체크
     */
    public void showLoginUserNameAndAlarm(HttpServletRequest request, Model model, Pageable pageable) {
        HttpSession session = request.getSession(true);

        if (session.getAttribute("userName") != null) {
            Object loginUserName = session.getAttribute("userName");
            model.addAttribute("loginUserName", loginUserName);


            List<AlarmListDetailsDto> alarms = alarmService.getAlarms((String) loginUserName);
            model.addAttribute("alarms", alarms);
        }
    }
}
