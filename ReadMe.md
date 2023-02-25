# 윤인규 개인 프로젝트 👨🏻‍💻

<br>

## SNS 웹 페이지 구현 프로젝트

회원 가입 후 게시글 작성 · 조회 · 수정 · 삭제 · 댓글 · 좋아요 버튼 등을 할 수 있는 SNS 웹 페이지 구현

배포 주소 : [http://ec2-13-231-48-116.ap-northeast-1.compute.amazonaws.com:8080/](http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/)

스웨거 주소 : [http://ec2-13-231-48-116.ap-northeast-1.compute.amazonaws.com:8080/swagger-ui/](http://ec2-13-231-48-116.ap-northeast-1.compute.amazonaws.com:8080/swagger-ui/)


<br>

## 목차

<br>

1. [개발환경](#개발환경)
2. [ERD](#erd)
3. [체크리스트](#체크리스트)
4. [UI 개발 상황](#ui-개발-상황)
5. [테스트 코드](#테스트-코드)
6. [회고](#프로젝트-회고)
7. [EndPoint](#endpoint)
8. [Endpoint Return Example](#endpoint-return-example)
9. [Error Info](#error-info)


<br>

## 개발환경

<br>

- **Java 11**
- **Build** : Gradle 7.5.1
- **Framework** : Springboot 2.7.5
- **Database** : MySQL 8.0
- **CI & CD** : Git Actions
- **Server** : AWS EC2
- **Deploy** : Docker
- **IDE** : IntelliJ

<br>

### 라이브러리

```groovy
dependencies {

  implementation group: 'org.bgee.log4jdbc-log4j2', name: 'log4jdbc-log4j2-jdbc4.1', version: '1.16'

  implementation 'org.springframework.boot:spring-boot-starter-web'
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
  annotationProcessor 'org.projectlombok:lombok'
  compileOnly 'org.projectlombok:lombok'

  // DB
  implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
  runtimeOnly 'com.mysql:mysql-connector-j'


  // template 엔진
  implementation 'org.springframework.boot:spring-boot-starter-mustache'

  //Swagger
  implementation 'io.springfox:springfox-swagger-ui:3.0.0'
  implementation 'io.springfox:springfox-boot-starter:3.0.0'

  //테스트에 사용 (객체 JSON 화)
  implementation 'com.google.code.gson:gson:2.10'

  //security 관련 라이브러리
  implementation 'org.springframework.security:spring-security-test'
  implementation group: 'io.jsonwebtoken', name: 'jjwt', version: '0.9.1'
  implementation group: 'org.springframework.boot', name: 'spring-boot-starter-security', version: '2.7.5'

  // request dto 필드 유효성 검사
  implementation 'org.springframework.boot:spring-boot-starter-validation'

  //static method 테스트 코드에 사용
  testImplementation group: 'org.mockito', name: 'mockito-inline', version: '4.8.1'

  //Querydsl
  implementation "com.querydsl:querydsl-jpa:${queryDslVersion}"
  implementation "com.querydsl:querydsl-apt:${queryDslVersion}"

}
```


<br>

## ERD

<p align="center">
<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230109223804607.png" alt="image-20230109223804607" style="zoom:80%;" />
</p>


<br>

## 체크리스트

- [x] Swagger 기능 추가 : API 문서 자동화 용이 및 API 테스트 가능
  - swagger
    주소 : [http://ec2-13-231-48-116.ap-northeast-1.compute.amazonaws.com:8080/swagger-ui/](http://ec2-13-231-48-116.ap-northeast-1.compute.amazonaws.com:8080/swagger-ui/)

- [x] Git Actions CI&CD pipeline 구축 : 새 버전 소프트웨어 관리 및 테스트 가능
  - Main 원격 저장소에 push 될 때마다, 현재 컨테이너 제거 후 재 실행할 수 있도록 cicd.yml 작성
  - 미리 작성된 Dockerfile을 통해 build

- [x] Git submodule 적용 : 보안에 민감한 환경변수 스크립트화 관리 및 권한 외 접근 제한

- [x] User 회원가입 및 로그인 기능 구현
  - 회원가입 시, 아이디와 비밀번호를 입력받고, 중복된 아이디의 경우 회원가입 에러 발생
  - 로그인 시, jwt 토큰을 발급하고 가입되어 있지 않거나 비밀번호가 일치하지 않으면 에러 발생

- [x] Post 페이징(20개씩) 조회 · 상세 조회 · 작성 · 수정 · 삭제 기능 구현
  - post 페이징(20개씩) 조회 · 상세 조회는 모든 사용자(로그인되어 있지 않은 사용자 포함) 접근 가능하고 조회시 `deleted_at`이 null인 데이터만 조회
  - post 작성은 로그인한 회원의 jwt 토큰을 확인한 뒤 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 에러 발생
  - post 수정 · 삭제는 로그인한 회원의 jwt 토큰을 확인한 뒤 가능하고 요청자와 작성자가 같아야 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 · 작성자와 요청자가 일치하지
    않는 경우 에러 발생
  - Post 삭제는 delete 쿼리로 삭제하는 것이 아닌, `deleted_at`을 현재 시간으로 입력하는 방법을 적용
    - post 삭제 시, 연관있는 `alarm soft delete`, `@Query` 와 `@Modifying` 어노테이션으로 여러개의 `alarm` delete 시 최적화 (쿼리문 1개로 삭제)

- [x] ADMIN 회원의 경우 회원 등급 변경 가능 · 모든 게시글 수정 · 삭제할 수 있는 기능 구현
  - 회원 가입 후, DB로 관리자(ADMIN) 아이디 ROLE (USER -> ADMIN) 변경
  - 모든 게시글 수정 및 삭제 가능 · 회원 등급 변경 가능

- [x] Comment 페이징(10개씩) 조회 · 작성 · 수정 · 삭제 기능 구현
  - comment 페이징(10개씩) 조회는 모든 사용자(로그인되어 있지 않은 사용자 포함) 접근 가능
  - comment 작성은 로그인한 회원의 jwt 토큰을 확인한 뒤 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 에러 발생
  - comment 수정 · 삭제는 로그인한 회원의 jwt 토큰을 확인한 뒤 가능하고 요청자와 작성자가 같아야 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 · 작성자와 요청자가 일치하지
    않는 경우 에러 발생
    - comment 삭제 시, soft delete 처리 되며, 삭제된 댓글의 대댓글도 모두 soft delete 처리 된다.
  - comment 작성 시, 알림 데이터가 생성되고, 만약 게시글 주인과 comment를 작성한 사람이 같으면, 알림을 따로 저장하지 않음
  - comment 작성 후, comment의 기본키 id를 바탕으로 replyComment를 작성할 수 있다. (대댓글)

- [x] Like 입력 및 개수 조회 기능 구현
  - Like 개수 조회는 모든 사용자(로그인되어 있지 않은 사용자 포함) 접근 가능
  - Like 입력은 로그인한 회원의 jwt 토큰을 확인한 뒤 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 에러 발생
  - Like 입력은 한 계정당 하나만 가능 (중복 입력 불가능)
  - Like 입력 시, 알림 데이터가 생성되고, 만약 게시글 주인과 Like를 입력한 사람이 같으면, 알림을 따로 저장하지 않음

- [x] 마이 피드(요청자가 작성한 게시글 모아보기) 기능 구현
  - 마이피드 조회는 로그인한 회원의 jwt 토큰을 확인한 뒤 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 에러 발생
  - 10개씩 페이징되며, 최신에 작성한 게시글 순으로 조회

- [x] Alarm 조회(요청자에게 온 알림 모아보기), 삭제 기능 구현
  - 알림 조회 및 삭제를 요청한 회원의 jwt 토큰을 확인한 뒤 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 에러 발생
  - 최신에 생성된 알림 순으로 조회 가능
  - 특정 알림 삭제 가능 (soft delete)

- [x] Rest Controller 에서 `request dto` @Valid 유효성 검사 후 예외 처리
  - null 이나 공백만으로 이루어진 값 허용하지 않음

- [x] ADMIN 회원의 경우 회원 등급 변경 가능 · 모든 게시글 및 댓글 수정 · 삭제할 수 있는 기능 구현
  - 회원 가입 후, DB로 관리자(ADMIN) 아이디 ROLE (USER -> ADMIN) 변경
  - 모든 게시글 및 댓글 수정 및 삭제 가능 · 회원 등급 변경 가능


<br>

## UI 개발 상황

주소 : [http://ec2-13-231-48-116.ap-northeast-1.compute.amazonaws.com:8080/](http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/)

- [x] 홈 화면
  - [x] 홈 화면 이동, 게시판 이동, 회원가입 화면, 로그인 화면 으로 이동할 수 있는 상단 바 구현
    - [x] 로그인 후, 상단 바에 회원가입, 로그인 버튼은 사라지고 회원명과 함께 마이페이지 버튼, 알림(🔔) 버튼, 로그아웃 버튼이 생성
    - [x] 알림(🔔) 버튼 클릭 시, 어떤 회원이 어떤 일자에 나의 어떤 게시글에 댓글 or 좋아요를 눌렀는지 표시
      - [x] 알림 내용 클릭시, 알림이 발생한 포스트로 이동.
      - [x] 알림 확인버튼(✔) 클릭시 삭제 구현
    - [x] 마이페이지 버튼 클릭 시, 마이페이지 화면으로 이동.


- [x] 회원가입
  - [x] 회원가입 시, 회원가입 명 또는 비밀번호 글자 사이에 공백이 포함되어 있는 경우 회원가입 불가
    - [x] 회원가입 시, 회원명 15글자 이상 문자로 불가
  - [x] 비밀번호 2차 확인 진행, 일치하는 경우에만 회원가입 가능
  - [x] 비밀번호 마스킹 처리
  - [x] 회원 가입 성공 후 홈 화면으로 이동

- [x] 로그인
  - [x] 가입된 회원이 아닐 시, 로그인 불가
  - [x] 비밀번호 일치하지 않을 시, 로그인 불가
  - [x] 비밀번호 마스킹 처리

- [x] 게시판 화면
  - [x] 게시글 id, 제목, 작성자, 작성날짜, 게시글 댓글 수 · 좋아요 수 표시
  - [x] 게시글 작성 버튼 클릭시, 게시글 작성 화면으로 이동
  - [x] 게시글 제목 클릭시, 게시글 상세 페이지로 이동

- [x] 게시글 상세 페이지
  - [x] 게시글 수정 버튼 클릭 시, 게시글 수정화면으로 이동
    - [x] 게시글 수정 시, 작성일자는 수정일자로 바뀌고 (수정됨) 표시
  - [x] 게시글 삭제 버튼 클릭 시, 게시글 작성자와 삭제 버튼 클릭자가 일치할 시 삭제됨
  - [x] 게시글 목록 버튼 클릭 시, 게시판 화면으로 이동
  - [x] 좋아요 버튼 클릭 및, 좋아요 갯수 표시 구현
    - [x] 로그인 사용자만 좋아요 입력 가능하고, 2번 누르는 것은 불가
  - [x] 댓글 기능 구현
    - [x] 댓글은 로그인한 사용자만 등록 가능
      - [x] 로그인한 사용자 `댓글의 댓글`(대댓글) 작성 · 수정 · 삭제 가능
      - [x] 댓글의 댓글 수 표시
    - [x] 댓글 수정 및 삭제는 댓글 작성자와 요청자가 일치하는 경우에만 가능
      - [x] 댓글 수정 시, 댓글 작성일자는 수정일자로 바뀌고 (수정됨) 표시
    - [x] 댓글 등록 및 수정시에 내용이 비어있는 경우 등록 및 수정 불가
  - [x] 게시글 제목 혹은 회원명으로 게시글을 검색할 수 있는 기능

- [x] 게시글 작성 페이지
  - [x] 게시글 제목 및 내용이 비어있는 경우 등록 불가

- [x] 게시글 수정 페이지
  - [x] 게시글 수정 시, 제목 및 내용 비어있는 경우 수정 불가
  - [x] 수정 요청자와 게시글 작성자가 다르면 수정 불가

- [x] 마이 페이지
  - [x] 로그인 시, 마이페이지 버튼이 상단 바에 생기고, 클릭하면 본인이 작성한 게시글을 확인할 수 있다.

<br>

### 구현한 UI 예시 화면

<br>

#### 1. 회원가입 및 로그인

<p align="center">
<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/joinAndLogin.gif" alt="joinAndLogin" style="zoom: 40%;" />
</p>

<br>

#### 2. 게시글 작성 · 댓글 작성 · 좋아요 클릭

<p align="center">
<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/%EA%B2%8C%EC%8B%9C%EA%B8%80%EB%8C%93%EA%B8%80%EC%9E%91%EC%84%B1.gif" alt="게시글댓글작성" style="zoom:40%;" />
</p>

<br>

#### 3. 알림 확인 및 게시글 수정 · 대댓글 작성 + 수정

<p align="center">
<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/%EC%95%8C%EB%A6%BC%ED%99%95%EC%9D%B8%EB%8C%93%EA%B8%80%EC%88%98%EC%A0%95.gif" alt="알림확인댓글수정" style="zoom:40%;" />
</p>

<br>

#### 4. 마이페이지 확인 및 게시글 검색 (게시글 제목 · 회원명 검색)

<p align="center">
<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/%EB%A7%88%EC%9D%B4%ED%8E%98%EC%9D%B4%EC%A7%80%EA%B2%80%EC%83%89.gif" alt="마이페이지검색" style="zoom:40%;" />
</p>


<br>

## 테스트 코드

<br>

총 129개 케이스 테스트 코드를 작성했고, RestController와 Service 테스트 코드 커버리지 100% 달성

테스트 코드 리포트 : [https://inkyu-yoon-sns-test-code.netlify.app/](https://inkyu-yoon-sns-test-code.netlify.app/)

<br>

<p align="center">
<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230226013603547.png" alt="image-20230226013603547" style="zoom:50%;" />
</p>

<br>

## 프로젝트 회고

<br>

### 1. UI 구현

<br>

UI 를 구현할 때, form 에 입력한 정보를 POST · PUT · DELETE 요청을 Controller 에 전달하는 코드를 구현하는게 힘들었었다.

javascript의 `console.log()` 메서드도 몰랐었지만, 이미 만들어둔 RestController 를 활용하기 위해 끊임없이 방법을 찾으려고 해봤고

덕분에 `axios` · `localstorage` · `Session` 등에 대해서 알게 되었고, css style 옵션값을 설정하여 원하는 크기, 색상들을 설정하여 UI를 구현할 수 있었다.

백엔드로서 구현한 기능을 UI로 직접 사용해보니, 더 큰 뿌듯함을 느낄 수 있었고,

프론트 쪽도 경험을 해보니, 프론트에서 활용하기 쉬운 `response Dto` 필드값을 설정하고 효율적인 Controller 로직과 Service layer 메서드를 구현하는 것이 중요하구나 라는 생각이 들었다.

- [배포 URL](http://ec2-13-231-48-116.ap-northeast-1.compute.amazonaws.com:8080/)

<br>

### 2. Security Chain 관련 Exception Handling

<br>

Service · Controller 에서 발생하는 Exception Handling은 `@RestControllerAdvice` 로 편하게 Handling할 수 있었는데,

Security Chain 에서 발생하는 Exception 은 Handling하기 어려웠다.

`@RestControllerAdvice` 는 Security Chain 에서 발생하는 Exception은 처리해주지 못하기 때문에, 원하는 에러 응답을 반환하지 못했다.

`AccessDeniedHandler`, `AuthenticationEntryPoint`, `ExceptionHandlerFilter` 를 구현한 뒤,

`SecurityConfig` 에 적절하게 설정을 하니, 원하는 대로 동작하게 만들 수 있었다.

이 문제를 해결하는 과정에서 Spring Security 공식문서를 공부해봐야겠다는 생각이 들었다.

- [Security Filter Exception Handling 정리](https://inkyu-yoon.github.io/docs/Language/SpringBoot/FilterExceptionHandle)
- [Security antMatchers Exception Handling 정리](https://inkyu-yoon.github.io/docs/Language/SpringBoot/SecurityChainException)

<br>

### 3. Controller Test, `@WithMockUser` 과 `@WithAnonymousUser`사용에 대한 고찰

<br>

Controller Test 를 구현하면서 많은 의문점이 생겼었다.

처음에 성공 테스트를 구현할 때에는, `@WithMockUser` 어노테이션을 메서드 레벨에 적용했었고, 인증이 된 상태로 테스트를 진행하도록 도와주었다.

하지만, 나는 특정 HTTP 메서드에 유효한 토큰을 담아서 요청한 경우에만 성공하도록 구현했었고,

`@WithMockUser` 를 사용하면 토큰을 **헤더에 담아서 요청하지 않았거나 이상한 토큰을 담아도** 테스트 결과는 언제나 성공이었다.

<br>

또한, 실패 테스트를 진행할 때에도, `@WithAnonymousUser` 를 사용하면,

내가 여러가지 상황을 염두해서 security chain 에서 정의한 예외처리를 확인하기 힘들었다.

물론, `willThrow()` 나 `doThrow()` 와 같은 방식으로 에러 상황을 설정할 수도 있었지만,

나는 TDD방식이 아닌 비즈니스 로직을 먼저 구현한 후, 리팩토링 시 리스크를 줄이기위한 목적으로 테스트 코드를 작성했기 때문에

테스트 코드에 내가 정의한 Security Chain을 적용하는게 맞다고 생각이 들었다.

StackOverFlow, 스프링 시큐리티 공식 문서 등에서 적용할 방법을 열심히 찾았고 내가 의도한 대로 테스트 코드 상황을 가정할 수 있었지만,

사실, 아직도 어떤 테스트 코드가 옳은 테스트 코드인지는 헷갈린다.😥

- [Security Chain Filter 포함해서 Controller Test 하기](https://inkyu-yoon.github.io/docs/Language/SpringBoot/SecurityChainTest)

<br>

### 4. Controller 에서 request Dto 필드 값 `null`로 입력되는 것 방지

<br>

POST · PUT 요청 시, JSON 형식의 데이터를 request 할때, request dto 객체의 필드 데이터가 null인 경우를 처리를 해주지 않았더니 UI를 구성할 때

null 인 데이터 때문에, 페이지 랜더링을 잘 못하는 현상이 일어났다.

물론, mustache 문법으로 처리할 수 도 있었지만,

애초에 회원명, 비밀번호, 게시글 제목, 게시글 내용 등등의 데이터가 null로 입력되어 저장되는 것은 이상하다고 생각했다.

따라서, null 체크를 하는 로직을 추가하였다.

controller 에서 request 객체를 받으면, service에서 null인지 체크하는 로직을 구현할 수도 있었지만

`validation` 라이브러리를 이용하여 Controller 단에서 바로 처리하게끔 구현하였다.

- [Controller 에서 요청 객체 변수 유효성 검사하기](https://inkyu-yoon.github.io/docs/Language/SpringBoot/validation)

<br>

### 5. Jacoco 라이브러리를 통한 Test 코드 커버리지 확인

<br>

오석이 덕분에 코드 커버리지를 확인할 수 있는 `Jacoco` 라는 라이브러리를 최근에 알게 되었다.

내가 어떤 부분에서 테스트 코드를 미흡하게 작성했는지 알 수 있었고,

부족한 부분을 채워서 Rest Controller 코드 라인 커버리지를 모두 90% 이상 달성했다.

Controller 에서 `@Valid` 로 바인딩 에러시 예외처리를 해놓은 부분 때문에, 100%는 달성하지 못했다.

이 부분은, 테스트 할 수 있는 방법을 찾아서 추가해야할 것 같다.

Service 테스트 코드를 많이 구현하지 못해서, 코드 커버리지 퍼센트가 낮은데, 이 부분을 개선해야겠다.

-> 😎 2월 26일 Rest Controller & Service 테스트 코드 커버리지 100% 달성했다.

숫자가 중요한 것은 아니지만, 테스트 코드를 짜는 연습을 할 수 있었고, Mockito를 능숙하게 사용할 수 있게 되었다.

- [Jacoco 적용으로 테스트 코드 개선하기](https://inkyu-yoon.github.io/docs/Language/SpringBoot/Jacoco)

<br>

### 6. Git Submodule & Git Actions Ci/CD 적용

<br>

코드를 Main에 push 할 때마다, 새로 업데이트 된 코드 기능이 자동으로 배포되도록

Git Actions 를 이용해 CI/CD 파이프라인을 구축했다.

또한, Git Submodule을 적용해 보안에 민감한 환경변수를

스크립트로서 관리하여 유지보수성을 향상시켰으며, private한 공간에 저장해두어 보안도 향상시켰다.

- [Git Submodule을 이용해서 안전하고 유지보수하기 쉽게 환경변수 관리하기](https://inkyu-yoon.github.io/docs/Learned/Git/GitSubmodule)
- [Docker Compose와 Git Actions로 CI/CD 구현하기](https://inkyu-yoon.github.io/docs/Learned/Docker/GitActionsCICD)

<br>

### 7. Querydsl을 사용하여 쿼리 최적화 하기

<br>

핑계일 수 있겠지만, 일단 결과물을 완성시켜야 한다는 생각이 앞서서 쿼리문을 잔뜩 남발했다.

‘쿼리문을 여러개 날려도 원하는 결과만 반환되면 되니까?!’ 라고 생각했었다. 🤔

기존 엉망진창으로 데이터를 합쳐서 반환시켰던 코드를 Querydsl을 사용해서 join 후 가져오는 방식으로 변경하였다.

이번 개선을 통해, 공부했던 Querydsl를 내가 직접 만든 프로젝트에 적용하니 뿌듯했고, 앞으로는 처음부터 적극 활용하여 좋은 성능으로 데이터를 가져와야겠다.

- [QueryDsl를 이용한 쿼리 수와 실행시간 개선 여정기](https://inkyu-yoon.github.io/docs/Language/JPA/UseQuerydsl)

<br>

## EndPoint

| METHOD | URL                                         | Description                                    | input                                      |
|--------|---------------------------------------------|------------------------------------------------|--------------------------------------------|
| POST   | /api/v1/users/join                          | 회원가입                                           | {"username": "string","password":"string"} |
| POST   | /api/v1/users/login                         | 로그인                                            | {"username": "string","password":"string"} |
| POST   | /api/v1/users/{userId}/role/change          | 회원 등급 변경(ADMIN 등급만 가능)                         | { "role": "string" }                       |
| GET    | /api/v1/posts                               | 게시글 조회(최신 글 20개 페이징 처리)                        | -                                          |
| GET    | /api/v1/posts/{postId}                      | 특정 게시글 상세 조회                                   | -                                          |
| POST   | /api/v1/posts                               | 게시글 작성 (jwt 토큰 헤더에 담아 요청)                      | { "title": "string" , "body": "string"}    |
| PUT    | /api/v1/posts/{postId}                      | 게시글 수정 (jwt 토큰 헤더에 담아 요청)                      | { "title": "string" , "body": "string"}    |
| DELETE | /api/v1/posts/{postId}                      | 게시글 삭제 (jwt 토큰 헤더에 담아 요청)                      | -                                          |
| GET    | /api/v1/posts/{postId}/comments             | postId에 해당하는 게시글에 존재하는 댓글 조회(최신 댓글 10개 페이징 처리) | -                                          |
| POST   | /api/v1/posts/{postId}/comments             | 댓글 작성 (jwt 토큰 헤더에 담아 요청)                       | { "comment": "string"}                     |
| POST   | /api/v1/posts/{postId}/comments/{commentId} | 댓글의 댓글 작성 (jwt 토큰 헤더에 담아 요청)                   | { "replyComment": "string"}                |
| PUT    | /api/v1/posts/{postId}/comments/{commentId} | 댓글 수정 (jwt 토큰 헤더에 담아 요청)                       | { "comment": "string"}                     |
| DELETE | /api/v1/posts/{postId}/comments/{commentId} | 댓글 삭제 (jwt 토큰 헤더에 담아 요청)                       | -                                          |
| GET    | /api/v1/posts/{postsId}/likes               | 좋아요 개수 조회                                      | -                                          |
| POST   | /api/v1/posts/{postsId}/likes               | 좋아요 입력 (jwt 토큰 헤더에 담아 요청)                      | -                                          |
| GET    | /api/v1/posts/my                            | 요청자가 작성한 게시글 조회 (최신 글 10개 페이징 처리)              | -                                          |
| GET    | /api/v1/alarms                              | 요청자에게 온 알림 조회 (최신 알림 20개 페이징 처리)               | -                                          |
| DELETE | /api/v1/alarms/{alarmId}                    | alarmId에 해당하는 알림 삭제                            | -                                          |

<br>

## Endpoint Return Example

### 1. 회원 가입 (POST) : /api/v1/users/join

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "userId": 1,
    "userName": "userName"
  }
}
```

<br>

### 2. 회원 로그인 (POST) : /api/v1/users/login

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "jwt": "eyJhbGciOiJIU"
  }
}
```

<br>

### 3. 회원 권한 변경 (POST) : /api/v1/users/{userId}/role/change

권한이 `ADMIN` 인 회원만 가능.

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "userId": 1,
    "message": "1번 아이디의 권한을 ROLE_USER로 변경하였습니다."
  }
}
```

<br>

### 4. 게시글 조회 (GET) : /api/v1/posts

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "content": [
      {
        "id": 2,
        "title": "hello-title",
        "body": "hello-body",
        "userName": "userName",
        "createdAt": "yyyy/mm/dd hh:mm:ss",
        "lastModifiedAt": "yyyy/mm/dd hh:mm:ss"
      }
    ],
    "pageable": {
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "offset": 0,
      "pageNumber": 0,
      "pageSize": 20,
      "paged": true,
      "unpaged": false
    },
    "last": false,
    "totalPages": 1,
    "totalElements": 1,
    "size": 20,
    "number": 0,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
  }
}
```

<br>

### 5. 게시글 상세 조회 (GET) :  /api/v1/posts/{postId}

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "id": 1,
    "title": "title1",
    "body": "body",
    "userName": "user1",
    "createdAt": "yyyy/mm/dd hh:mm:ss",
    "lastModifiedAt": "yyyy/mm/dd hh:mm:ss"
  }
}
```

<br>

### 6. 게시글 작성 (POST) : /api/v1/posts

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "message": "포스트 등록 완료",
    "postId": 0
  }
}
```

<br>

### 7. 게시글 수정 (PUT) : /api/v1/posts/{postId}

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "message": "포스트 수정 완료",
    "postId": 0
  }
}
```

<br>

### 8. 게시글 삭제 (DELETE) : /api/v1/posts/{postId}

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "message": "포스트 삭제 완료",
    "postId": 0
  }
}
```

<br>

### 9. 댓글 조회 (GET) : /api/v1/posts/{postId}/comments

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "content": [
      {
        "id": 2,
        "comment": "comment2",
        "userName": "userName1",
        "postId": 1,
        "createdAt": "yyyy/mm/dd hh:mm:ss"
      },
      {
        "id": 1,
        "comment": "comment2",
        "userName": "userName1",
        "postId": 1,
        "createdAt": "yyyy/mm/dd hh:mm:ss"
      }
    ],
    "pageable": {
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "offset": 0,
      "pageSize": 10,
      "pageNumber": 0,
      "unpaged": false,
      "paged": true
    },
    "last": true,
    "totalElements": 2,
    "totalPages": 1,
    "size": 10,
    "number": 0,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "first": true,
    "numberOfElements": 2,
    "empty": false
  }
}
```

<br>

### 10. 댓글 작성 (POST) : /api/v1/posts/{postId}/comments

```json
{
  "resultCode": "SUCCESS",
  "result":{
    "id": 1,
    "comment": "comment",
    "userName": "userName",
    "postId": 1,
    "createdAt": "yyyy/mm/dd hh:mm:ss"
  }
}
```

<br>

### 11. 댓글의 댓글 작성 (POST) : /api/v1/posts/{postId}/comments/{commentId}

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "id": 1,
    "comment": "comment",
    "userName": "userName",
    "postId": 1,
    "createdAt": "yyyy/mm/dd hh:mm:ss",
    "parentId": 1
  }
}
```


<br>

### 12. 댓글 수정 (PUT) : /api/v1/posts/{postId}/comments/{commentId}

```json
{
  "resultCode": "SUCCESS",
  "result":{
    "id": 1,
    "comment": "comment",
    "userName": "userName",
    "postId": 1,
    "createdAt": "yyyy/mm/dd hh:mm:ss",
    "modifiedAt": "yyyy/mm/dd hh:mm:ss"
  }
}
```

<br>

### 13. 댓글 삭제 (DELETE) : /api/v1/posts/{postId}/comments/{commentId}

```json
{
  "resultCode": "SUCCESS",
  "result":{
    "message": "댓글 삭제 완료",
    "id": 1
  }
}
```

### 14. 좋아요 추가 (POST) : /api/v1//posts/{postId}/likes

```json
{
  "resultCode":"SUCCESS",
  "result": "좋아요를 눌렀습니다."
}
```

<br>

### 15. 좋아요 개수 (GET) : /api/v1/posts/{postsId}/likes

```json
{
  "resultCode":"SUCCESS",
  "result": 0
}
```

<br>

### 16. 마이 피드 (요청자가 작성한 게시글 조회) (GET) : /api/v1/posts/my

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "content": [
      {
        "id": 2,
        "title": "title",
        "body": "body",
        "userName": "userName",
        "createdAt": "yyyy/mm/dd hh:mm:ss",
        "lastModifiedAt": "yyyy/mm/dd hh:mm:ss"
      },
      {
        "id": 1,
        "title": "title",
        "body": "body",
        "userName": "userName",
        "createdAt": "yyyy/mm/dd hh:mm:ss",
        "lastModifiedAt": "yyyy/mm/dd hh:mm:ss"
      }
    ],
    "pageable": {
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "offset": 0,
      "pageSize": 20,
      "pageNumber": 0,
      "paged": true,
      "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 2,
    "size": 20,
    "number": 0,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "first": true,
    "numberOfElements": 2,
    "empty": false
  }
}
```

<br>

### 17. 알림 조회 (GET) : /api/v1/alarms

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "content": [
      {
        "id": 2,
        "alarmType": "NEW_LIKE_ON_POST",
        "fromUserId": 1,
        "targetId": 1,
        "text": "new like!",
        "createdAt": "yyyy/mm/dd hh:mm:ss"
      },
      {
        "id": 1,
        "alarmType": "NEW_COMMENT_ON_POST",
        "fromUserId": 1,
        "targetId": 1,
        "text": "new comment!",
        "createdAt": "yyyy/mm/dd hh:mm:ss"
      }
    ],
    "pageable": {
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "offset": 0,
      "pageNumber": 0,
      "pageSize": 20,
      "unpaged": false,
      "paged": true
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 2,
    "size": 20,
    "number": 0,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "first": true,
    "numberOfElements": 2,
    "empty": false
  }
}
```

<br>

### 18. 알림 삭제 (DELETE) : /api/v1/alarms/{alarmId}

```json
{
  "resultCode": "SUCCESS",
  "result": {
    "message": "알림 삭제 완료",
    "id": 1
  }
}
```

<br>

## Error Info

| Status Code | Error Message        | When                                               |
|-------------|----------------------|----------------------------------------------------|
| 409         | DUPLICATED_USER_NAME | 회원 가입 시 중복일 때 발생                                   |
| 404         | USERNAME_NOT_FOUND   | DB에 저장된 회원명이 없는 경우 발생                              |
| 404         | POST_NOT_FOUND       | 상세 조회, 삭제, 수정 요청 시, 요청한 postId에 해당하는 게시글이 없는 경우 발생 |
| 404         | COMMENT_NOT_FOUND    | 댓글 삭제, 수정 요청 시, 요청한 commentId 해당하는 게시글이 없는 경우 발생   |
| 404         | ALARM_NOT_FOUND      | 알림 삭제 요청 시, 요청한 alarmId 해당하는 알림이 없는 경우 발생          |
| 401         | INVALID_PASSWORD     | 로그인 시 패스워드 잘못 입력한 경우 발생                            |
| 401         | EXPIRED_TOKEN        | 만료된 토큰으로 요청할 시 발생                                  |
| 401         | INVALID_TOKEN        | jwt 토큰이 아니거나 유효하지 않은 토큰으로 요청할 시 발생                 |
| 401         | TOKEN_NOT_FOUND      | 토큰 없이, 토큰이 필요한 작업 요청 시 발생                          |
| 401         | USER_NOT_MATCH       | 게시글 수정 · 삭제 요청 시, 요청자와 작성자가 다른 경우 발생               |
| 403         | FORBIDDEN_REQUEST    | ADMIN만 접근할 수 있는 요청을 ADMIN이 아닌 사용자가 요청할 시 발생        |
| 403         | FORBIDDEN_ADD_LIKE   | 이미 좋아요를 입력하고, 2번째 입력하는 경우                          |
| 400         | BAD_REQUEST          | 권한을 "ADMIN" 혹은 "USER" 가 아닌 다른 문자열을 담아 요청하는 경우 발생   |
| 400         | BLANK_NOT_ALLOWED    | 공백 또는 null 유효성 검사 시, 에러가 발생할 경우                    |
| 500         | DATABASE_ERROR       | DB 연결이 끊어질 경우 발생                                   |

<br>

에러 발생 시, 예시

```json
{
  "resultCode": "ERROR",
  "result": {
    "errorCode": "POST_NOT_FOUND",
    "message": "Post not founded"
  }
}
```
