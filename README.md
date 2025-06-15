# GridgeChallenge - Server

## ⦿ 주요 기능

1. 관리자 기능

- 회원 목록 조회(페이징 및 필터링)
- 회원 상태 관리(활성화 및 비활성화)
- 회원 삭제(소프트 삭제 및 하드 삭제)

2. 감사 로그

- 모든 데이터 변경 이력 추적
- 사용자별 변경 기록 조회
- 시스템 전체 감사 로그 조회
- 특정 리비전 상세 정보 조회

3. 회원 관리

- 회원 가입 및 로그인
- 소셜 로그인
- 회원 정보 수정 및 탈퇴
- 개인정보 관리

## ⦿ 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                         │
├─────────────────────────────────────────────────────────────┤
│  Web Browser / Mobile App / Postman / Swagger UI            │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
├─────────────────────────────────────────────────────────────┤
│  Spring Boot Application (Port: 9000)                       │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐  │
│  │   User API  │  Admin API  │  Auth API   │ Audit API   │  │
│  │ Controller  │ Controller  │ Controller  │ Controller  │  │
│  └─────────────┴─────────────┴─────────────┴─────────────┘  │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐  │
│  │ User Service│Admin Service│OAuth Service│Audit Service│  │
│  └─────────────┴─────────────┴─────────────┴─────────────┘  │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐  │
│  │    JPA      │ Hibernate   │    JWT      │  Security   │  │
│  │ Repository  │   Envers    │  Service    │   Filter    │  │
│  └─────────────┴─────────────┴─────────────┴─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
├─────────────────────────────────────────────────────────────┤
│  MySQL Database (Port: 3306)                                │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐  │
│  │    Users    │   Admins    │   Audit     │   OAuth     │  │
│  │    Table    │   Table     │   Tables    │   Tables    │  │
│  └─────────────┴─────────────┴─────────────┴─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                   External Services                         │
├─────────────────────────────────────────────────────────────┤
│  Google OAuth 2.0 / Naver OAuth / Kakao OAuth               │
└─────────────────────────────────────────────────────────────
```

## ⦿ 데이터베이스 스키마

![Image](https://github.com/user-attachments/assets/561af161-eea6-420c-ab36-e4309f916d51)

> **※ `logs` 테이블에 대하여**: 위 ERD의 `logs` 테이블은 Hibernate Envers의 데이터 변경 이력과는 별개로, '사용자 로그인/로그아웃' 등 개발자가
> 직접 정의한 특정 행위(Event)를 기록하기 위한 애플리케이션 로그 테이블입니다.

### 로깅 상세 스키마

해당 프로젝트는 변경 이력을 추적하기 위해 **Hibernate Envers**를 사용합니다.
`@Audited` 어노테이션이 붙은 엔티티에 대해 아래와 같은 감사 테이블들이 **자동으로 생성**됩니다.

![Image](https://github.com/user-attachments/assets/11c15a69-627a-4c73-b1ed-08b87e45a805)

- **REVINFO**: 모든 변경의 시점(Timestamp)과 고유 리비전 번호(REV)를 기록하는 마스터 테이블입니다.
- **USER_AUD**: `users` 테이블의 데이터 변경 내역이 기록됩니다. 모든 감사 테이블은 `REVINFO`의 리비전 번호를 참조하여 변경 시점을 추적합니다.

## ⦿ 프로젝트 구조

```
demo/
├── common/
│   ├── config/ # 설정 클래스
│   │   ├── RestTemplateConfig.java // HTTP get,post 요청을 날릴때 일정한 형식에 맞춰주는 template
│   │   ├── SwaggerConfig.java  // Swagger 관련 설정 
│   │   └── WebConfig.java // Web 관련 설정(CORS 설정 포함)
│   ├── entity/
│   │   └── BaseEntity.java // create, update, state 등 Entity에 공통적으로 정의되는 변수를 정의한 BaseEntity
│   ├── exceptions/
│   │   ├── BaseException.java // Controller, Service에서 Response 용으로 공통적으로 사용 될 익셉션 클래스
│   │   └── ExceptionAdvice.java // ExceptionHandler를 활용하여 정의해놓은 예외처리를 통합 관리하는 클래스
│   ├── oauth/
│   │   ├── GoogleOauth.java // Google OAuth 처리 클래스
│   │   ├── OAuthService.java // OAuth 공통 처리 서비스 클래스
│   │   └── SocialOauth.java // OAuth 공통 메서드 정의 인터페이스
│   ├── response/
│   │   ├── BaseResponse.java // Controller 에서 Response 용으로 공통적으로 사용되는 구조를 위한 모델 클래스
│   │   └── BaseResponseStatus.java // ExceptionHandler를 활용하여 정의해놓은 예외처리를 통합 관리하는 클래스
│   ├── secret/
│   │   └── Secret.java // jwt 암호키 보관 클래스
│   └── Constant.java // 상수 보관 클래스
├── src/
│   ├── admin/
│   │   ├── model/
│   │   │   ├── GetUserState.java
│   │   │   └── UpdateUserReq.java 
│   │   ├── AdminController.java // 관리자용 컨트롤러
│   │   ├── AdminDataManager.java 
│   │   ├── AdminService.java
│   │   └── UserSpecifications.java
│   ├── audit/
│   │   ├── model/
│   │   │   ├── UserAuditReq.java
│   │   │   └── UserAuditRes.java
│   │   ├── AuditController.java
│   │   └── AuditService.java
│   ├── test/
│   │   ├── entity/
│   │   │   ├── Comment.java
│   │   │   └── Memo.java
│   │   ├── model/
│   │   │   ├── GetMemoDto.java
│   │   │   ├── MemoDto.java
│   │   │   └── PostCommentDto.java
│   │   ├── CommentRepository.java
│   │   ├── MemoRepository.java
│   │   ├── TestController.java
│   │   ├── TestDao.java
│   │   └── TestService.java
│   ├── user/
│       ├── entity/
│       │   └── User.java
│       ├── model/
│       │   ├── GetSocialOAuthRes.java
│       │   ├── GetUserRes.java
│       │   ├── GoogleOAuthToken.java
│       │   ├── GoogleUser.java
│       │   ├── PatchUserReq.java
│       │   ├── PostLoginReq.java
│       │   ├── PostLoginRes.java
│       │   ├── PostUserReq.java
│       │   ├── PostUserRes.java
│       │   ├── UserConsentReq.java
│       │   └── UserPrivacyConsent.java
│       ├── UserController.java
│       ├── UserRepository.java
│       └── UserService.java
├── utils/
│   ├── JwtService.java
│   ├── SHA256.java
│   └── ValidationRegex.java
└── DemoApplication.java

```

### JSON 응답 예시

#### ✅ 성공 시

`result` 필드에 실제 데이터가 포함되어 반환됩니다.

```json
{
  "isSuccess": true,
  "code": 1000,
  "message": "요청에 성공하였습니다.",
  "result": {
    "userId": 1,
    "name": "홍길동",
    "email": "gildong@example.com"
  }
}
```

### ❌실패 시

result 필드는 null이며, JSON 응답에 포함되지 않습니다. 에러에 대한 정보는 isSuccess, code, message를 통해 전달됩니다.

```
JSON
{
    "isSuccess": false,
    "code": 2015,
    "message": "중복된 이메일입니다."
}
```

### 주요 설정

- DB : MySQL 8.0 이상의 버전으로 연결될 수 있도록 설정
- Swagger: API 문서화를 위한 springdoc 설정
- Hibernate Envers: 감사 로그 기능을 위한 audit 설정

## ⦿ 시작하기

1. **프로젝트 클론 (Clone)**

   터미널에서 아래 명령어를 실행하여 프로젝트를 복제합니다.

   ```bash
   git clone https://github.com/gobad820/2506_Server.git.git
   ```

2. **데이터베이스 설정 (Database Setup)**

   ```sql
   CREATE DATABASE demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **애플리케이션 설정 (Application Configuration)**

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/demo?serverTimezone=Asia/Seoul
       username: {DB_사용자명}
       password: {DB_비밀번호}
   ```

4. **애플리케이션 실행 (Run Application)**

   프로젝트 루트 디렉토리에서 아래의 Gradle 명령어를 실행하여 애플리케이션을 시작합니다.

   ```bash
   ./gradlew build
   nohup java -jar build/libs/demo-0.0.1-SNAPSHOT.jar > application.log 2>&1 &
   ```

5. **접속**

- **기본 접속 URL**: `http://localhost:9000`
- **API 문서 (Swagger UI)**: `http://localhost:9000/swagger-ui.html`

