# Local API

Upon start-up, Decent Messaging starts a local web server on TCP port 7771 (by default) for communication with application running on the same machine. 
Its scope is limited to `localhost` (`127.0.0.1`) for security.

The local API protocol uses simple GET and POSTs to specific URLs, and returns JSON responses.

## Terminology

It is worth familiarising yourself with [Decent Messaging terminology](terminology.md) before reviewing the available commands.

## API Endpoints
 
The base URL for the API is `http://localhost:7771/api/v1/`.

| Method  | Endpoint            | Parameters                 | Description   |
| ------- | ------------------- | -------------------------- | ------------- |
| GET     | `personal-messages`   | (none)                   | Retrieves a list of all personal message IDs |
| GET     | `personal-messages`   | since=X                  | Retrieves a list of all personal message IDs received since the UNIX timestamp X |
| GET     | `personal-messages`   | id=X                     | Retrieves the personal message with an ID of X |
| POST    | `personal-messages`   | delete=X                 | Deletes the personal message with an ID of X |
