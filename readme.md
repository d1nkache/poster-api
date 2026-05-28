# Poster API: Android Integration Guide

Документ описывает текущий backend API и то, как подключать его в Android-приложении.

## Base URL

Локально:

```text
http://10.0.2.2:8080
```

`10.0.2.2` используется Android Emulator для доступа к `localhost` хост-машины.

Для реального устройства в одной Wi-Fi сети:

```text
http://<LAN_IP_компьютера>:8080
```

В production должен быть HTTPS:

```text
https://api.example.com
```

## Общие правила

- JSON API использует `Content-Type: application/json`.
- Защищенные запросы требуют заголовок:

```http
Authorization: Bearer <accessToken>
```

- `accessToken` - JWT access token.
- `refreshToken` - opaque token, используется только для `/auth/refresh`.
- `currentUserId` на backend берется из `sub` внутри Bearer JWT.
- ID в некоторых DTO сейчас смешанные: профиль отдается как строка `user-1`, контакты/сообщения как `Long`.
- Время отдается строкой ISO-8601.

## Error Format

Ошибки приходят единым JSON:

```json
{
  "code": "CONTACT_NOT_FOUND",
  "message": "Contact not found"
}
```

Клиенту стоит обрабатывать минимум:

| HTTP | Пример code | Что делать |
|---:|---|---|
| 400 | `INVALID_OTP`, `INVALID_CONTACT_ID` | Показать ошибку формы/запроса |
| 401 | `UNAUTHORIZED`, `INVALID_CREDENTIALS`, `INVALID_REFRESH_TOKEN` | Refresh token или logout |
| 404 | `CONTACT_NOT_FOUND`, `CHAT_NOT_FOUND`, `MESSAGE_NOT_FOUND` | Показать empty/error state |
| 409 | `EMAIL_ALREADY_REGISTERED` | Показать ошибку регистрации |
| 415 | `UNSUPPORTED_AVATAR_TYPE` | Попросить выбрать PNG |
| 500 | `INTERNAL_SERVER_ERROR` | Показать общую ошибку |

## Auth Flow

Регистрация:

1. Android вызывает `POST /auth/register`.
2. Backend создает пользователя `isVerified=false`.
3. Backend генерирует OTP и кладет задачу в `otp_outbox`.
4. `runOtpWorker` отправляет код с системной почты.
5. Android показывает экран ввода кода.
6. Android вызывает `POST /auth/verify-otp`.
7. Backend возвращает `accessToken`, `refreshToken`, `profile`.

Вход:

1. Android вызывает `POST /auth/login`.
2. Backend проверяет email/password и `isVerified=true`.
3. Backend возвращает `accessToken`, `refreshToken`, `profile`.

OTP используется только при регистрации. Обычный вход выполняется по email и паролю.

## Auth Endpoints

### POST `/auth/register`

Auth: нет.

Request:

```json
{
  "email": "user@mail.com",
  "password": "password123",
  "displayName": "Ivan"
}
```

Response `201 Created`:

```json
{
  "message": "OTP code sent"
}
```

Ошибки:

```json
{
  "code": "EMAIL_ALREADY_REGISTERED",
  "message": "Email already registered"
}
```

### POST `/auth/verify-otp`

Auth: нет.

Request:

```json
{
  "email": "user@mail.com",
  "code": "123456"
}
```

Response `200 OK`:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "profile": {
    "id": "user-1",
    "name": "Ivan",
    "username": "@user1",
    "email": "user@mail.com",
    "birthday": null,
    "bio": null,
    "avatarUrl": "/media/avatars/profile-1-avatar.png",
    "isOnline": false
  }
}
```

Ошибка:

```json
{
  "code": "INVALID_OTP",
  "message": "Invalid OTP"
}
```

### POST `/auth/login`

Auth: нет.

Request:

```json
{
  "email": "user@mail.com",
  "password": "password123"
}
```

Response такой же, как у `/auth/verify-otp`.

Ошибка:

```json
{
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid credentials or email is not verified"
}
```

### POST `/auth/refresh`

Auth: нет.

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "accessToken": "new-jwt-access-token",
  "refreshToken": "new-refresh-token",
  "profile": {
    "id": "user-1",
    "name": "Ivan",
    "username": "@user1",
    "email": "user@mail.com",
    "birthday": null,
    "bio": null,
    "avatarUrl": "/media/avatars/profile-1-avatar.png",
    "isOnline": false
  }
}
```

Важно: backend ротирует refresh token. Android должен заменить оба токена после успешного refresh.

### POST `/auth/logout`

Auth: Bearer access token.

Request body: пустой.

Response:

```http
204 No Content
```

Backend очищает refresh token hash для текущего пользователя.

## Profile Endpoints

Все profile endpoints требуют Bearer token.

### GET `/profile/me`

Response:

```json
{
  "id": "user-1",
  "name": "Ivan",
  "username": "@user1",
  "email": "user@mail.com",
  "birthday": "2000-01-01",
  "bio": "Hello",
  "avatarUrl": "/media/avatars/profile-1-avatar.png",
  "isOnline": true
}
```

### PATCH `/profile/me`

Request, все поля optional:

```json
{
  "name": "Ivan Ivanov",
  "username": "@ivan",
  "bio": "Android developer",
  "birthday": "2000-01-01"
}
```

Response: `ProfileResponse`.

### GET `/profile/{userId}`

`userId` можно передавать как `1` или `user-1`.

Response: `ProfileResponse`.

### POST `/profile/avatar`

Auth: Bearer access token.

Request:

```text
multipart/form-data
field name: avatar
content type: image/png
```

Response:

```json
{
  "avatarUrl": "/media/avatars/profile-1-avatar.png"
}
```

Ошибки:

```json
{
  "code": "AVATAR_CONTENT_TYPE_REQUIRED",
  "message": "Avatar content type is required"
}
```

```json
{
  "code": "UNSUPPORTED_AVATAR_TYPE",
  "message": "Avatar must be image/png"
}
```

## Settings Endpoints

Все settings endpoints требуют Bearer token.

### GET `/settings`

Response:

```json
{
  "language": "ru",
  "hasMailAccessToken": true,
  "smtpHost": "smtp.example.com",
  "smtpPort": 587,
  "imapHost": "imap.example.com",
  "imapPort": 993
}
```

### PATCH `/settings`

Request:

```json
{
  "language": "en"
}
```

Response: `SettingsResponse`.

### PUT `/settings/mail-access-token`

Request:

```json
{
  "token": "provider-access-token-or-app-password",
  "smtpHost": "smtp.example.com",
  "smtpPort": 587,
  "imapHost": "imap.example.com",
  "imapPort": 993
}
```

`smtpHost`, `smtpPort`, `imapHost`, `imapPort` optional. Если они не переданы, worker попробует определить настройки по email или взять defaults из `application.yaml`.

Response:

```http
204 No Content
```

### DELETE `/settings/mail-access-token`

Response:

```http
204 No Content
```

### GET `/settings/mail-access-token/status`

Response:

```json
{
  "configured": true
}
```

## Contacts Endpoints

Все contacts endpoints требуют Bearer token.

### GET `/contacts`

Query:

```text
q optional
```

Response:

```json
{
  "items": [
    {
      "id": 1,
      "email": "friend@mail.com",
      "displayName": "Friend",
      "avatarUrl": null,
      "createdAt": "2026-05-27T12:00:00Z",
      "updatedAt": "2026-05-27T12:00:00Z"
    }
  ]
}
```

### GET `/contacts/{contactId}`

Response: `ContactResponse`.

### POST `/contacts`

Request:

```json
{
  "email": "friend@mail.com",
  "displayName": "Friend"
}
```

Response `201 Created`: `ContactResponse`.

### PATCH `/contacts/{contactId}`

Request:

```json
{
  "displayName": "Best Friend",
  "avatarUrl": "/media/avatars/friend.png"
}
```

Response: `ContactResponse`.

### DELETE `/contacts/{contactId}`

Response:

```http
204 No Content
```

### POST `/contacts/{contactId}/chat`

Создает или возвращает существующий чат с контактом.

Response:

```json
{
  "id": "1",
  "title": "Friend",
  "initials": "F",
  "lastMessage": null,
  "lastMessageTime": null,
  "unreadCount": 0,
  "isOnline": false
}
```

## Messages Endpoints

Все message endpoints требуют Bearer token.

### GET `/chats/{chatId}/messages`

Query:

```text
limit optional, default 50, range 1..100
offset optional, default 0
```

Response:

```json
{
  "items": [
    {
      "id": 1,
      "chatId": 1,
      "bodyText": "Hello",
      "direction": "OUTGOING",
      "status": "SENT",
      "isRead": false,
      "createdAt": "2026-05-27T12:00:00Z",
      "sentAt": "2026-05-27T12:00:01Z",
      "receivedAt": null
    }
  ]
}
```

### POST `/chats/{chatId}/messages`

Request:

```json
{
  "bodyText": "Hello"
}
```

Response `201 Created`:

```json
{
  "message": {
    "id": 1,
    "chatId": 1,
    "bodyText": "Hello",
    "direction": "OUTGOING",
    "status": "PENDING",
    "isRead": false,
    "createdAt": "2026-05-27T12:00:00Z",
    "sentAt": null,
    "receivedAt": null
  }
}
```

После создания backend кладет письмо в `mail_outbox`, а `runMailWorker` отправляет его через SMTP пользователя.

### GET `/messages/{messageId}`

Response: `MessageResponse`.

### PATCH `/messages/{messageId}/read`

Response:

```http
204 No Content
```

### PATCH `/chats/{chatId}/messages/read`

Пометить сообщения чата прочитанными.

Response:

```http
204 No Content
```

### DELETE `/messages/{messageId}`

Response:

```http
204 No Content
```

## Static Media

Файлы доступны через:

```text
/media/**
```

Например:

```text
http://10.0.2.2:8080/media/avatars/profile-1-avatar.png
```

Если `avatarUrl` приходит относительным путем, Android должен добавить `baseUrl`.

## WebSocket

Текущий endpoint:

```text
ws://10.0.2.2:8080/ws
```

Сейчас backend websocket реализован как echo endpoint:

```text
client: hello
server: YOU SAID: hello
```

Полноценные auth-события чатов пока не реализованы. Для Android MVP лучше использовать REST polling/refresh списка сообщений. Когда WebSocket будет расширяться, нужно добавить Bearer JWT-auth на handshake и события `NEW_MESSAGE`, `MESSAGE_SENT`, `MESSAGE_READ`, `CHAT_UPDATED`.

## Android DTO

Рекомендуемые Kotlin DTO под текущий API:

```kotlin
@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String? = null
)

@Serializable
data class RegisterResponseDto(
    val message: String
)

@Serializable
data class VerifyOtpRequestDto(
    val email: String,
    val code: String
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val profile: ProfileDto
)

@Serializable
data class ProfileDto(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val birthday: String?,
    val bio: String?,
    val avatarUrl: String,
    val isOnline: Boolean
)
```

```kotlin
@Serializable
data class ErrorResponseDto(
    val code: String,
    val message: String
)
```

Для enum:

```kotlin
enum class MessageDirection {
    INCOMING,
    OUTGOING
}

enum class MessageStatus {
    PENDING,
    SENT,
    FAILED,
    READ
}
```

Если Android enum отличается, добавить fallback:

```kotlin
@Serializable
enum class MessageStatusDto {
    PENDING,
    SENT,
    FAILED,
    READ,
    UNKNOWN
}
```

## Retrofit Interfaces

Пример:

```kotlin
interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): RegisterResponseDto

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}
```

```kotlin
interface ProfileApi {
    @GET("profile/me")
    suspend fun getMe(): ProfileDto

    @PATCH("profile/me")
    suspend fun updateMe(@Body body: UpdateProfileRequestDto): ProfileDto

    @Multipart
    @POST("profile/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): UploadProfileAvatarResponseDto
}
```

```kotlin
interface ContactsApi {
    @GET("contacts")
    suspend fun getContacts(@Query("q") query: String? = null): ContactListResponseDto

    @POST("contacts")
    suspend fun createContact(@Body body: CreateContactRequestDto): ContactDto

    @POST("contacts/{contactId}/chat")
    suspend fun getOrCreateChat(@Path("contactId") contactId: Long): ChatDto
}
```

```kotlin
interface MessagesApi {
    @GET("chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: Long,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Long = 0
    ): MessageListResponseDto

    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: Long,
        @Body body: SendMessageRequestDto
    ): SendMessageResponseDto
}
```

## OkHttp Auth Interceptor

Добавлять Bearer token ко всем запросам, кроме `/auth/register`, `/auth/verify-otp`, `/auth/login`, `/auth/refresh`.

```kotlin
class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isPublicAuth = path in setOf(
            "/auth/register",
            "/auth/verify-otp",
            "/auth/login",
            "/auth/refresh"
        )

        val accessToken = tokenStore.accessToken()
        val authenticatedRequest = if (!isPublicAuth && !accessToken.isNullOrBlank()) {
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}
```

## Token Refresh Strategy

Рекомендуемая логика:

1. Если REST вернул `401`, попробовать один раз вызвать `/auth/refresh`.
2. Если refresh успешен, сохранить новые `accessToken` и `refreshToken`.
3. Повторить исходный запрос.
4. Если refresh вернул `401`, очистить сессию и открыть login screen.

Важно: не запускать много refresh-запросов параллельно. Использовать `Mutex`.

## Android Startup Flow

При старте приложения:

1. Прочитать токены из encrypted storage.
2. Если токенов нет - открыть auth screen.
3. Если есть `accessToken`, вызвать `GET /profile/me`.
4. Если `401`, выполнить refresh.
5. После успешной авторизации загрузить:
   - `GET /profile/me`
   - `GET /settings`
   - `GET /contacts`

## Worker Requirements

Для регистрации OTP должен быть запущен:

```bash
.\gradlew.bat runOtpWorker
```

Для отправки пользовательских сообщений и IMAP sync:

```bash
.\gradlew.bat runMailWorker
```

SMTP нашей системной почты задается в `src/main/resources/application.yaml`:

```yaml
mail:
  auth:
    fromEmail: "noreply@example.com"
    username: "noreply@example.com"
    password: "app-password"
    smtpHost: "smtp.example.com"
    smtpPort: 587
```

Пароль лучше передавать через env:

```yaml
password: "$POSTER_AUTH_SMTP_PASSWORD:"
```

## Minimal Integration Checklist

1. Android base URL для emulator: `http://10.0.2.2:8080/`.
2. Реализовать auth screens: register, verify OTP, login.
3. Сохранять оба токена после `verify-otp`, `login`, `refresh`.
4. Добавить OkHttp interceptor для Bearer token.
5. Добавить refresh-on-401.
6. Подключить profile/settings/contacts/messages endpoints.
7. Для avatar upload отправлять только PNG multipart field `avatar`.
8. Не полагаться на WebSocket для MVP, он пока echo-only.
9. На backend запустить API, PostgreSQL, `runOtpWorker`; для сообщений также `runMailWorker`.
