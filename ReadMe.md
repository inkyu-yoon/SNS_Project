



## SNS 웹 페이지 구현 프로젝트

회원 가입 후 게시글 작성 · 조회 · 수정 · 삭제 · 댓글 · 좋아요 버튼 등을 할 수 있는 SNS 웹 페이지 구현

<br>

---

<br>

## 체크리스트

- [x] Swagger  기능 추가 : API 문서 자동화 용이 및 API 테스트 가능
- [x] GitLab CI&CD pipeline 구축 : 새 버전 소프트웨어 관리 및 테스트 가능
    - GitLab Project가 업데이트 되었는지 확인하고 업데이트되어 있는 경우, 현재 컨테이너 제거 후 재 실행할 수 있도록 deploy.sh 작성
    - 미리 작성된 Dockerfile을 통해 build
    - crontab 기능을 활용하여 정기적으로 deploy.sh를 실행하도록 설정

- [x] User 회원가입 및 로그인 기능 구현
    - 회원가입 시, 아이디와 비밀번호를 입력받고, 중복된 아이디의 경우 회원가입 에러 발생
    - 로그인 시, jwt 토큰울 발급하고 가입되어 있지 않거나 비밀번호가 일치하지 않으면 에러 발생

- [x] Post 전체 조회 · 상세 조회 · 작성 · 수정 · 삭제 기능 구현
    - post 전체 조회 · 상세 조회는 모든 사용자(로그인 되어 있지 않은 사용자 포함) 접근 가능
    - post 작성은 로그인한 회원의 jwt 토큰을 확인한 뒤 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 에러 발생
    - post 수정 · 삭제는 로그인한 회원의 jwt 토큰을 확인한 뒤 가능하고 요청자와 작성자가 같아야 가능, 토큰이 유효하지 않은 경우 · 만료된 경우 · 토큰이 없는 경우 · 작성자와 요청자가 일치하지 않는 경우 에러 발생
- [x] ADMIN 회원의 경우 회원 등급 변경 가능 · 모든 게시글 수정 · 삭제 가능한 기능 구현
    - 회원 가입 후, DB로 관리자(ADMIN) 아이디 ROLE (USER -> ADMIN) 변경
    - 모든 게시글 수정 및 삭제 가능 · 회원 등급 변경 가능
- [x] User · Post Controller 기능 동작을 중점으로 UI 구현
    - [http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/?](http://ec2-54-248-132-84.ap-northeast-1.compute.amazonaws.com:8080/?)

<br>

---

<br>

## EndPoint

| METHOD | URL                                | Description                             | input                                      |
| ------ | ---------------------------------- | --------------------------------------- | ------------------------------------------ |
| POST   | /api/v1/users/join                 | 회원가입                                | {"username": "string","password":"string"} |
| POST   | /api/v1/users/login                | 로그인                                  | {"username": "string","password":"string"} |
| POST   | /api/v1/users/{userId}/role/change | 회원 등급 변경(ADMIN 등급만 가능)       | { "role": "string" }                       |
| GET    | /api/v1/posts                      | 게시글 조회(최신글 20개 페이징 처리)    | -                                          |
| GET    | /api/v1/posts/{postId}             | 특정 게시글 상세 조회                   | -                                          |
| POST   | /api/v1/posts                      | 게시글 작성 (jwt 토큰 헤더에 담아 요청) | { "title": "string" , "body": "string"}    |
| PUT    | /api/v1/posts/{postId}             | 게시글 수정 (jwt 토큰 헤더에 담아 요청) | { "title": "string" , "body": "string"}    |
| DELETE | /api/v1/posts/{postId}             | 게시글 삭제 (jwt 토큰 헤더에 담아 요청) | -                                          |



## Endpoint Return Example

### 1. 회원 가입 (POST) : /api/v1/users/join

```
{
    "resultCode": "SUCCESS",
    "result": {
        "userId": 5,
        "userName": "test1"
    }
}
```

<br>

### 2. 회원 로그인 (POST) : /api/v1/users/login

```
{
    "resultCode": "SUCCESS",
    "result": {
        "jwt": "eyJhbGciOiJIU",
    }
}
```

<br>

### 3. 회원 권한 변경 (POST) : /api/v1/users/{userId}/role/change

권한이 `ADMIN` 인 회원만 가능.

```
{
    "resultCode": "SUCCESS",
    "result": {
        "userId": 6,
        "message": "6번 아이디의 권한을 ROLE_USER로 변경하였습니다."
    }
}
```

<br>

### 4. 게시글 조회 (GET) : /api/v1/posts

```
{
    "resultCode": "SUCCESS",
    "result": {
        "content": [
            {
                "id": 10,
                "title": "글이 들어온다아아아",
                "body": "글들어온다아앙",
                "userName": "손흥민",
                "createdAt": "2022/12/22 10:43:25",
                "lastModifiedAt": "2022/12/22 10:43:25"
            }
        ],
        "pageable": "INSTANCE",
        "last": true,
        "totalPages": 1,
        "totalElements": 4,
        "size": 4,
        "number": 0,
        "sort": {
            "empty": true,
            "sorted": false,
            "unsorted": true
        },
        "first": true,
        "numberOfElements": 4,
        "empty": false
    }
}
```

<br>

### 5. 게시글 상세 조회 (GET) :  /api/v1/posts/{postId}

```
{
	"resultCode":"SUCCESS",
	"result":{
		"id" : 1,
		"title" : "title1",
		"body" : "body",
		"userName" : "user1",
		"createdAt" : yyyy/mm/dd hh:mm:ss,
		"lastModifiedAt" : yyyy/mm/dd hh:mm:ss
	}
}
```

<br>

### 6. 게시글 작성 (POST) : /api/v1/posts

```
{
	"resultCode":"SUCCESS",
	"result":{
		"message":"포스트 등록 완료",
		"postId":0
	}
}
```

<br>

### 7. 게시글 수정 (PUT) : /api/v1/posts/{postId}

```
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

```
{
    "resultCode": "SUCCESS",
    "result": {
        "message": "포스트 삭제 완료",
        "postId": 10
    }
}
```

<br>



---

<br>

## Error Info

| Status Code | Error Message        | When                                                         |
| ----------- | -------------------- | ------------------------------------------------------------ |
| 409         | DUPLICATED_USER_NAME | 회원 가입 시 중복일 때 발생                                  |
| 404         | USERNAME_NOT_FOUND   | DB에 저장된 회원명이 없는 경우 발생                          |
| 404         | POST_NOT_FOUND       | 상세 조회, 삭제, 수정 요청 시, 요청한 postId에 해당하는 게시글이 없는 경우 발생 |
| 401         | INVALID_PASSWORD     | 로그인 시 패스워드 잘못 입력한 경우 발생                     |
| 401         | EXPIRED_TOKEN        | 만료된 토큰으로 요청할 시 발생                               |
| 401         | INVALID_TOKEN        | jwt 토큰이 아니거나 유효하지 않은 토큰으로 요청할 시 발생    |
| 401         | TOKEN_NOT_FOUND      | 토큰 없이, 토큰이 필요한 작업 요청 시 발생                   |
| 401         | INVALID_PERMISSION   | ADMIN만 접근할 수 있는 요청을 ADMIN이 아닌 사용자가 요청할 시 발생 |
| 401         | USER_NOT_MATCH       | 게시글 수정 · 삭제 요청 시, 요청자와 작성자가 다른 경우 발생 |
| 500         | DATABASE_ERROR       | DB 연결이 끊어질 경우 발생                                   |

<br>

에러 발생 시, 예시

```
{
  "resultCode":"ERROR",
  "result":{
     "errorCode":"POST_NOT_FOUND",
     "message":"Post not founded"
  }
}
```



## 특이사항

