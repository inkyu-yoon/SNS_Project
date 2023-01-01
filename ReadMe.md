# 윤인규 개인 프로젝트 👨🏻‍💻

<br>

## SNS 웹 페이지 구현 프로젝트

회원 가입 후 게시글 작성 · 조회 · 수정 · 삭제 · 댓글 · 좋아요 버튼 등을 할 수 있는 SNS 웹 페이지 구현

<br>

---

<br>

## 개발환경

<br>

- **Java 11**
- **Build** : Gradle 7.5.1
- **Framework** : Springboot 2.7.5
- **Database** : MySQL 8.0
- **CI & CD** : GitLab
- **Server** : AWS EC2
- **Deploy** : Docker
- **IDE** : IntelliJ

<br>

### 라이브러리

```groovy
dependencies {
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
}
```

<br>

---

<br>

## ERD

<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8erd.svg" alt="프로젝트erd" style="zoom:110%;" />

<br>

---

<br>

## 체크리스트

- [x] Swagger 기능 추가 : API 문서 자동화 용이 및 API 테스트 가능
    - swagger
      주소 : [http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/swagger-ui/](http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/swagger-ui/)

- [x] GitLab CI&CD pipeline 구축 : 새 버전 소프트웨어 관리 및 테스트 가능
    - GitLab Project가 업데이트 되었는지 확인하고 업데이트되어 있는 경우, 현재 컨테이너 제거 후 재 실행할 수 있도록 deploy.sh 작성
    - 미리 작성된 Dockerfile을 통해 build
    - crontab 기능을 활용하여 정기적으로 deploy.sh를 실행하도록 설정

- [x] User 회원가입 및 로그인 기능 구현
    - 회원가입 시, 아이디와 비밀번호를 입력받고, 중복된 아이디의 경우 회원가입 에러 발생
    - 로그인 시, jwt 토큰을 발급하고 가입되어 있지 않거나 비밀번호가 일치하지 않으면 에러 발생

- [x] Post 전체 조회 · 상세 조회 · 작성 · 수정 · 삭제 기능 구현
    - post 전체 조회 · 상세 조회는 모든 사용자(로그인되어 있지 않은 사용자 포함) 접근 가능
    - post 작성은 로그인한 회원의 jwt 토큰을 확인한 뒤 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 에러 발생
    - post 수정 · 삭제는 로그인한 회원의 jwt 토큰을 확인한 뒤 가능하고 요청자와 작성자가 같아야 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 · 작성자와 요청자가 일치하지
      않는 경우 에러 발생

- [x] ADMIN 회원의 경우 회원 등급 변경 가능 · 모든 게시글 수정 · 삭제할 수 있는 기능 구현
    - 회원 가입 후, DB로 관리자(ADMIN) 아이디 ROLE (USER -> ADMIN) 변경
    - 모든 게시글 수정 및 삭제 가능 · 회원 등급 변경 가능

- [x] User · Post Controller 기능 동작을 중점으로 UI 구현
    - [http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/?](http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/?)

<br>

---

<br>

## EndPoint

| METHOD | URL                                | Description               | input                                      |
| ------ | ---------------------------------- |---------------------------| ------------------------------------------ |
| POST   | /api/v1/users/join                 | 회원가입                      | {"username": "string","password":"string"} |
| POST   | /api/v1/users/login                | 로그인                       | {"username": "string","password":"string"} |
| POST   | /api/v1/users/{userId}/role/change | 회원 등급 변경(ADMIN 등급만 가능)    | { "role": "string" }                       |
| GET    | /api/v1/posts                      | 게시글 조회(최신 글 20개 페이징 처리)   | -                                          |
| GET    | /api/v1/posts/{postId}             | 특정 게시글 상세 조회              | -                                          |
| POST   | /api/v1/posts                      | 게시글 작성 (jwt 토큰 헤더에 담아 요청) | { "title": "string" , "body": "string"}    |
| PUT    | /api/v1/posts/{postId}             | 게시글 수정 (jwt 토큰 헤더에 담아 요청) | { "title": "string" , "body": "string"}    |
| DELETE | /api/v1/posts/{postId}             | 게시글 삭제 (jwt 토큰 헤더에 담아 요청) | -                                          |

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



---

<br>

## Error Info

| Status Code | Error Message        | When                                               |
|-------------| -------------------- |----------------------------------------------------|
| 409         | DUPLICATED_USER_NAME | 회원 가입 시 중복일 때 발생                                   |
| 404         | USERNAME_NOT_FOUND   | DB에 저장된 회원명이 없는 경우 발생                              |
| 404         | POST_NOT_FOUND       | 상세 조회, 삭제, 수정 요청 시, 요청한 postId에 해당하는 게시글이 없는 경우 발생 |
| 401         | INVALID_PASSWORD     | 로그인 시 패스워드 잘못 입력한 경우 발생                            |
| 401         | EXPIRED_TOKEN        | 만료된 토큰으로 요청할 시 발생                                  |
| 401         | INVALID_TOKEN        | jwt 토큰이 아니거나 유효하지 않은 토큰으로 요청할 시 발생                 |
| 401         | TOKEN_NOT_FOUND      | 토큰 없이, 토큰이 필요한 작업 요청 시 발생                          |
| 401         | USER_NOT_MATCH       | 게시글 수정 · 삭제 요청 시, 요청자와 작성자가 다른 경우 발생               |
| 403         |FORBIDDEN_REQUEST  | ADMIN만 접근할 수 있는 요청을 ADMIN이 아닌 사용자가 요청할 시 발생        |
| 400         |BAD_REQUEST  | 권한을 "ADMIN" 혹은 "USER" 가 아닌 다른 문자열을 담아 요청하는 경우 발생   |
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

<br>


---

<br>

## 특이사항

<br>

### 1. UI 구현

<br>

UI 를 구현할 때, form 에 입력한 정보를 POST · PUT · DELETE 요청을 Controller 에 전달하는 코드를 구현하는게 힘들었었다.

javascript의 `console.log()` 메서드도 몰랐었지만, 이미 만들어둔 RestController 를 활용하기 위해 끊임없이 방법을 찾으려고 해봤고

덕분에 `axios` · `localstorage` · `Session` 등에 대해서 알게 되었다.

웹페이지의 디자인은 꾸미지 못해지만, 로그인 기능 · 로그아웃 기능 · 로그인한 사용자만 게시글 작성 · 수정 · 삭제 등을 구현할 수 있었다.

- [http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/](http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/)

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





