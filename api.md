# Backend API для Poster

Документ фиксирует контракт, который сейчас ожидает Android-приложение. Источники в проекте:

- `app/src/main/java/com/example/poster/data/remote/api/*Api.kt`
- `app/src/main/java/com/example/poster/data/remote/api/PosterApiRoutes.kt`
- `app/src/main/java/com/example/poster/data/remote/dto/**/*.kt`
- `app/src/main/java/com/example/poster/data/remote/websocket/PosterSocketEvent.kt`

## Общие правила

- Base URL сейчас заглушка: `https://api.example.com/`.
- Все REST-запросы и ответы, кроме загрузки файлов, в `application/json; charset=utf-8`.
- Все защищенные ручки принимают `Authorization: Bearer <accessToken>`.
- Время отдавать строкой в ISO-8601, например `2026-05-16T12:30:45Z`. Мобильный DTO кладет это в поле `createdAt` / `lastMessageTime` как строку.
- Идентификаторы отдавать строками.
- Для пустого успешного ответа использовать `204 No Content`.
- Для ошибок везде использовать единый JSON:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Email is invalid",
  "details": {
    "field": "email"
  }
}
```

Базовые коды ошибок: `VALIDATION_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `MAIL_TOKEN_INVALID`, `CHAT_SYNC_FAILED`, `ATTACHMENT_TOO_LARGE`, `UNSUPPORTED_FILE_TYPE`, `INTERNAL_ERROR`.

## Enum значения

```text
MessageStatus:
SENDING, SENT, DELIVERED, READ, FAILED

AttachmentType:
IMAGE, VIDEO, AUDIO, DOCUMENT, UNKNOWN

AttachmentUploadStatus на клиенте:
LOCAL_ONLY, UPLOADING, UPLOADED, FAILED
```

В DTO с бэка для сообщений поле `status` должно приходить одним из `MessageStatus`. Для вложений поле `type` должно приходить одним из `AttachmentType`.

## REST ручки

### Auth

| Метод | Путь | Auth | Request | Response | Назначение |
|---|---|---:|---|---|---|
| `POST` | `/auth/sign-up` | нет | `SignUpRequestDto` | `AuthResponseDto` | Регистрация пользователя. Также можно отправлять OTP на email. |
| `POST` | `/auth/sign-in` | нет | `SignInRequestDto` | `AuthResponseDto` | Вход по email и паролю. |
| `POST` | `/auth/verify-otp` | нет | `VerifyOtpRequestDto` | `AuthResponseDto` | Подтверждение OTP. |
| `POST` | `/auth/resend-otp` | нет | `{ "email": "user@mail.com" }` | `204` | Повторная отправка OTP. |
| `POST` | `/auth/refresh` | да, refresh token или cookie | `{ "refreshToken": "..." }` | `AuthResponseDto` | Обновление пары токенов. |
| `POST` | `/auth/logout` | да | пусто | `204` | Инвалидация текущей сессии. |
| `GET` | `/auth/me` | да | нет | `UserDto` | Текущий пользователь. |

DTO:

```json
{
  "SignInRequestDto": {
    "email": "user@mail.com",
    "password": "password"
  },
  "SignUpRequestDto": {
    "name": "Ivan Ivanov",
    "email": "user@mail.com",
    "password": "password"
  },
  "VerifyOtpRequestDto": {
    "email": "user@mail.com",
    "code": "123456"
  },
  "AuthResponseDto": {
    "accessToken": "jwt-access",
    "refreshToken": "jwt-refresh",
    "user": {
      "id": "user-1",
      "name": "Ivan Ivanov",
      "username": "@ivan",
      "email": "user@mail.com"
    }
  }
}
```

Важно: текущие мобильные DTO ожидают `AuthResponseDto` и от `sign-up`, и от `verify-otp`. Если бэк хочет выдавать токены только после OTP, надо либо временно возвращать ограниченную сессию на `sign-up`, либо править мобильный контракт.

### Profile

| Метод | Путь | Auth | Request | Response | Назначение |
|---|---|---:|---|---|---|
| `GET` | `/profile/me` | да | нет | `ProfileDto` | Мой профиль. |
| `PATCH` | `/profile/me` | да | `UpdateProfileRequestDto` | `ProfileDto` | Частичное обновление моего профиля. |
| `GET` | `/profile/{userId}` | да | нет | `ProfileDto` | Профиль другого пользователя. |
| `POST` | `/profile/avatar` | да | `multipart/form-data`, поле `avatar` | `UploadAvatarResponseDto` | Загрузка аватара. |

DTO:

```json
{
  "ProfileDto": {
    "id": "user-1",
    "name": "Ivan Ivanov",
    "username": "@ivan",
    "email": "user@mail.com",
    "birthday": "2000-01-01",
    "bio": "Hey there!",
    "avatarUrl": "https://cdn.example.com/avatars/user-1.jpg",
    "isOnline": true
  },
  "UpdateProfileRequestDto": {
    "name": "Ivan",
    "username": "@ivan",
    "bio": "New bio",
    "birthday": "2000-01-01"
  },
  "UploadAvatarResponseDto": {
    "avatarUrl": "https://cdn.example.com/avatars/user-1.jpg"
  }
}
```

`UpdateProfileRequestDto` частичный: все поля nullable, менять только переданные поля.

### Settings

| Метод | Путь | Auth | Request | Response | Назначение |
|---|---|---:|---|---|---|
| `GET` | `/settings` | да | нет | `SettingsDto` | Получить настройки. |
| `PATCH` | `/settings` | да | `{ "language": "en" }` | `SettingsDto` | Обновить язык. |
| `PUT` | `/settings/mail-access-token` | да | `SaveMailAccessTokenRequestDto` | `204` | Сохранить токен доступа к почте. |
| `DELETE` | `/settings/mail-access-token` | да | нет | `204` | Удалить токен доступа к почте. |
| `GET` | `/settings/mail-access-token/status` | да | нет | `MailAccessTokenStatusDto` | Проверить, настроен ли токен. |

DTO:

```json
{
  "SettingsDto": {
    "language": "en",
    "hasMailAccessToken": true
  },
  "SaveMailAccessTokenRequestDto": {
    "token": "mail-provider-access-token"
  },
  "MailAccessTokenStatusDto": {
    "configured": true
  }
}
```

### Chats

| Метод | Путь | Auth | Request | Response | Назначение |
|---|---|---:|---|---|---|
| `GET` | `/chats` | да | query `q` опционально | `ChatListResponseDto` | Список чатов. Если передан `q`, вернуть отфильтрованный список. |
| `GET` | `/chats/{chatId}` | да | нет | `ChatDto` | Детали чата. |
| `POST` | `/chats` | да | `CreateChatRequestDto` | `ChatDto` | Создать чат с получателем. |
| `DELETE` | `/chats/{chatId}` | да | нет | `204` | Удалить/скрыть чат для текущего пользователя. |
| `POST` | `/chats/{chatId}/read` | да | нет или `{ "lastReadMessageId": "..." }` | `204` | Пометить чат прочитанным. |
| `POST` | `/chats/sync` | да | нет | `204` или `ChatListResponseDto` | Запустить синхронизацию чатов с почтовым источником. |

DTO:

```json
{
  "ChatDto": {
    "id": "chat-1",
    "title": "Product Team",
    "initials": "PT",
    "lastMessage": "Latest message preview",
    "lastMessageTime": "2026-05-16T12:30:45Z",
    "unreadCount": 2,
    "isOnline": true
  },
  "ChatListResponseDto": {
    "chats": []
  },
  "CreateChatRequestDto": {
    "recipient": "user@mail.com"
  }
}
```

### Messages

| Метод | Путь | Auth | Request | Response | Назначение |
|---|---|---:|---|---|---|
| `GET` | `/chats/{chatId}/messages` | да | query `limit`, `before` опционально | `MessageListResponseDto` | История сообщений чата. |
| `POST` | `/chats/{chatId}/messages` | да | `SendMessageRequestDto` | `MessageDto` | Отправить сообщение. |
| `DELETE` | `/messages/{messageId}` | да | нет | `204` | Удалить сообщение. |
| `POST` | `/messages/{messageId}/read` | да | нет | `204` | Пометить сообщение прочитанным. |
| `POST` | `/messages/{messageId}/retry` | да | нет | `MessageDto` | Повторить отправку failed-сообщения. |

DTO:

```json
{
  "MessageDto": {
    "id": "msg-1",
    "chatId": "chat-1",
    "senderId": "user-1",
    "text": "Hello",
    "createdAt": "2026-05-16T12:30:45Z",
    "isMine": true,
    "status": "SENT",
    "attachments": []
  },
  "SendMessageRequestDto": {
    "text": "Hello",
    "attachmentIds": ["att-1"]
  },
  "MessageListResponseDto": {
    "messages": []
  },
  "AttachmentDto": {
    "id": "att-1",
    "fileName": "photo.jpg",
    "mimeType": "image/jpeg",
    "sizeBytes": 248000,
    "url": "https://cdn.example.com/attachments/att-1/photo.jpg",
    "type": "IMAGE"
  }
}
```

`text` в `MessageDto` nullable, но в доменную модель приложение сейчас мапит `null` в пустую строку. Сообщение валидно, если есть непустой `text` или хотя бы один `attachmentId`.

### Attachments

`AttachmentApi` есть в коде, но маршруты для него пока не вынесены в `PosterApiRoutes`. Для бэка и будущей интеграции нужны такие ручки:

| Метод | Путь | Auth | Request | Response | Назначение |
|---|---|---:|---|---|---|
| `POST` | `/chats/{chatId}/attachments` | да | `multipart/form-data`, поле `file` | `AttachmentDto` | Загрузить вложение перед отправкой сообщения. |
| `GET` | `/attachments/{attachmentId}` | да | нет | `AttachmentDto` | Получить метаданные вложения. |
| `DELETE` | `/attachments/{attachmentId}` | да | нет | `204` | Удалить вложение, если сообщение еще не отправлено или пользователь имеет права. |

Ограничения для MVP:

- Максимальный размер согласовать на бэке, например 25 MB.
- `mimeType` определять на бэке, не доверять только клиенту.
- `url` должен быть доступен приложению для скачивания/просмотра по `Authorization` или быть короткоживущей signed URL.

## Workflows

### Регистрация и вход

1. Пользователь открывает auth-экран.
2. `POST /auth/sign-up` с `name`, `email`, `password`.
3. Бэк создает пользователя, отправляет OTP и возвращает `AuthResponseDto` по текущему мобильному контракту.
4. Пользователь вводит код.
5. `POST /auth/verify-otp` с `email`, `code`.
6. Бэк подтверждает email и возвращает актуальный `AuthResponseDto`.
7. Приложение сохраняет `accessToken`, `refreshToken`, открывает список чатов.

Для входа:

1. `POST /auth/sign-in`.
2. Приложение сохраняет токены.
3. Дальше все защищенные REST и WebSocket используют `accessToken`.

### Старт приложения после авторизации

1. Проверить локальную сессию.
2. Если access token протух, вызвать `POST /auth/refresh`.
3. Получить профиль: `GET /profile/me`.
4. Проверить почтовый токен: `GET /settings/mail-access-token/status`.
5. Получить чаты: `GET /chats`.
6. Открыть WebSocket `/ws`.

### Настройка почтового токена и синхронизация

1. Пользователь вводит токен доступа к почте.
2. `PUT /settings/mail-access-token`.
3. `GET /settings/mail-access-token/status`.
4. `POST /chats/sync`.
5. Бэк через WebSocket отправляет `SYNC_STARTED`, затем `CHAT_UPDATED` по измененным чатам, затем `SYNC_FINISHED`.
6. Клиент обновляет список чатов через события или дополнительно вызывает `GET /chats`.

### Открытие чата

1. Пользователь выбирает чат.
2. `GET /chats/{chatId}/messages`.
3. Клиент подписан на WebSocket и принимает `NEW_MESSAGE`, `MESSAGE_SENT`, `MESSAGE_READ`, `CHAT_UPDATED`.
4. При входе в чат можно вызвать `POST /chats/{chatId}/read`.

### Отправка сообщения без вложений

1. Клиент валидирует: `text.trim()` не пустой.
2. `POST /chats/{chatId}/messages` с `SendMessageRequestDto`.
3. Бэк создает сообщение со статусом `SENT` или `DELIVERED`.
4. Ответом возвращается `MessageDto`.
5. Через WebSocket:
   - отправителю можно отправить `MESSAGE_SENT`;
   - другим участникам отправить `NEW_MESSAGE`;
   - всем участникам отправить `CHAT_UPDATED`.

### Отправка сообщения с вложениями

1. Клиент выбирает локальный файл.
2. `POST /chats/{chatId}/attachments` multipart.
3. Бэк возвращает `AttachmentDto`.
4. `POST /chats/{chatId}/messages` с `attachmentIds`.
5. Дальше workflow такой же, как для обычного сообщения.

### Прочтение сообщений

1. При открытии чата или просмотре сообщения клиент вызывает `POST /messages/{messageId}/read` или `POST /chats/{chatId}/read`.
2. Бэк обновляет read state.
3. Через WebSocket отправляет `MESSAGE_READ` и `CHAT_UPDATED`.

## WebSocket

### Подключение

Путь из кода: `/ws`.

Рекомендуемый URL:

```text
wss://api.example.com/ws
```

Auth:

- предпочтительно: `Authorization: Bearer <accessToken>` при handshake;
- если клиентская библиотека не дает передать headers: `wss://api.example.com/ws?token=<accessToken>`.

После подключения сервер должен привязать socket к текущему пользователю и отправлять события только по чатам, где пользователь участник.

### Envelope

Сейчас в Android есть DTO:

```kotlin
data class PosterSocketEventDto(
    val type: PosterSocketEventType,
    val payload: String,
)
```

То есть для полной совместимости `payload` должен быть строкой с JSON внутри:

```json
{
  "type": "NEW_MESSAGE",
  "payload": "{\"message\":{\"id\":\"msg-1\",\"chatId\":\"chat-1\",\"senderId\":\"user-2\",\"text\":\"Hi\",\"createdAt\":\"2026-05-16T12:30:45Z\",\"isMine\":false,\"status\":\"DELIVERED\",\"attachments\":[]}}"
}
```

Более удобный будущий вариант для клиента: заменить `payload: String` на `payload: JsonElement` и отправлять объект:

```json
{
  "type": "NEW_MESSAGE",
  "payload": {
    "message": {
      "id": "msg-1",
      "chatId": "chat-1",
      "senderId": "user-2",
      "text": "Hi",
      "createdAt": "2026-05-16T12:30:45Z",
      "isMine": false,
      "status": "DELIVERED",
      "attachments": []
    }
  }
}
```

Для MVP можно оставить строковый `payload`, но формат внутреннего JSON должен совпадать с payload-описаниями ниже.

### Обязательные события сервера

Эти события уже есть в `PosterSocketEventType` и должны быть реализованы на бэке:

| Event | Payload | Когда отправлять | Что делает клиент |
|---|---|---|---|
| `NEW_MESSAGE` | `{ "message": MessageDto, "chat": ChatDto? }` | В чат пришло новое сообщение от другого пользователя/почтовой синхронизации. | Добавить сообщение в открытый чат, обновить превью чата и unread. |
| `MESSAGE_SENT` | `{ "message": MessageDto }` | Сообщение текущего пользователя принято/отправлено сервером. | Заменить optimistic/failed-сообщение на серверное, обновить статус. |
| `MESSAGE_READ` | `{ "chatId": "chat-1", "messageId": "msg-1", "readerUserId": "user-2", "readAt": "2026-05-16T12:31:00Z" }` | Пользователь прочитал сообщение или чат. | Обновить статус сообщения на `READ`, сбросить unread где нужно. |
| `CHAT_UPDATED` | `{ "chat": ChatDto }` | Изменился title, lastMessage, unreadCount, online-state или чат появился после sync. | Обновить элемент списка чатов. |
| `SYNC_STARTED` | `{ "syncId": "sync-1", "startedAt": "2026-05-16T12:30:00Z" }` | Началась синхронизация чатов/почты. | Показать состояние загрузки, не блокируя экран. |
| `SYNC_FINISHED` | `{ "syncId": "sync-1", "updatedChats": [ChatDto], "finishedAt": "2026-05-16T12:31:00Z" }` | Синхронизация завершилась успешно. | Обновить список чатов или вызвать `GET /chats`. |
| `ERROR` | `{ "code": "CHAT_SYNC_FAILED", "message": "Cannot sync mail account", "details": {} }` | Ошибка, относящаяся к socket workflow. | Показать ошибку/лог, при необходимости запросить REST fallback. |

Примеры payload:

```json
{
  "type": "CHAT_UPDATED",
  "payload": "{\"chat\":{\"id\":\"chat-1\",\"title\":\"Product Team\",\"initials\":\"PT\",\"lastMessage\":\"Hi\",\"lastMessageTime\":\"2026-05-16T12:30:45Z\",\"unreadCount\":1,\"isOnline\":true}}"
}
```

```json
{
  "type": "ERROR",
  "payload": "{\"code\":\"UNAUTHORIZED\",\"message\":\"Socket token expired\"}"
}
```

### Рекомендуемые client -> server команды

В текущем Android enum этих команд нет, но бэку полезно заложить их формат. Если клиент будет отправлять сообщения в socket, использовать такой envelope:

| Command | Payload | Назначение |
|---|---|---|
| `SUBSCRIBE_CHAT` | `{ "chatId": "chat-1" }` | Подписаться на точечные события чата. |
| `UNSUBSCRIBE_CHAT` | `{ "chatId": "chat-1" }` | Отписаться от событий чата при выходе с экрана. |
| `MARK_MESSAGE_READ` | `{ "messageId": "msg-1" }` | Альтернатива REST `POST /messages/{messageId}/read`. |
| `MARK_CHAT_READ` | `{ "chatId": "chat-1", "lastReadMessageId": "msg-1" }` | Альтернатива REST `POST /chats/{chatId}/read`. |
| `TYPING_STARTED` | `{ "chatId": "chat-1" }` | Начал печатать. |
| `TYPING_STOPPED` | `{ "chatId": "chat-1" }` | Закончил печатать. |
| `PING` | `{ "sentAt": "2026-05-16T12:30:45Z" }` | Keepalive. Сервер отвечает `PONG`. |

Если эти команды будут добавлены в приложение, надо расширить мобильный enum и обработчики.

### Дополнительные возможные события

Эти события пока не добавлены в `PosterSocketEventType`, но пригодятся для нормального real-time UX:

| Event | Payload | Для чего |
|---|---|---|
| `MESSAGE_DELIVERED` | `{ "chatId": "chat-1", "messageId": "msg-1", "deliveredAt": "..." }` | Перевод статуса `SENT` -> `DELIVERED`. |
| `MESSAGE_DELETED` | `{ "chatId": "chat-1", "messageId": "msg-1" }` | Удаление сообщения у участников. |
| `MESSAGE_FAILED` | `{ "chatId": "chat-1", "messageId": "msg-1", "reason": "..." }` | Ошибка доставки. |
| `ATTACHMENT_UPLOADED` | `{ "attachment": AttachmentDto }` | Асинхронная обработка большого файла. |
| `USER_STATUS_CHANGED` | `{ "userId": "user-2", "isOnline": true, "lastSeenAt": "..." }` | Online/offline индикаторы. |
| `TYPING_STARTED` | `{ "chatId": "chat-1", "userId": "user-2" }` | Индикатор печати. |
| `TYPING_STOPPED` | `{ "chatId": "chat-1", "userId": "user-2" }` | Скрыть индикатор печати. |
| `PONG` | `{ "sentAt": "...", "serverAt": "..." }` | Ответ на keepalive. |

## Минимальный список для бэка в MVP

1. Реализовать auth: `/auth/sign-up`, `/auth/sign-in`, `/auth/verify-otp`, `/auth/resend-otp`, `/auth/refresh`, `/auth/logout`, `/auth/me`.
2. Реализовать profile: `/profile/me`, `/profile/{userId}`, `/profile/avatar`.
3. Реализовать settings и хранение mail access token.
4. Реализовать chats: список, создание, удаление, read, sync.
5. Реализовать messages: история, отправка, read, delete, retry.
6. Реализовать attachments: upload/get/delete и отдачу `AttachmentDto.url`.
7. Реализовать WebSocket `/ws` с событиями из текущего enum: `NEW_MESSAGE`, `MESSAGE_SENT`, `MESSAGE_READ`, `CHAT_UPDATED`, `SYNC_STARTED`, `SYNC_FINISHED`, `ERROR`.
8. При истечении access token REST должен отдавать `401`, WebSocket должен отправлять `ERROR` с `code = "UNAUTHORIZED"` и закрывать соединение.
