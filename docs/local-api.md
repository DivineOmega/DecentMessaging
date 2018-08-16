# Local API

Upon start-up, Decent Messaging starts a local web server on TCP port 7771 (by default) for communication with application running on the same machine. 
Its scope is limited to `localhost` (`127.0.0.1`) for security.

The local API protocol uses simple GET and POSTs to specific URLs, and returns JSON responses.

## Terminology

It is worth familiarising yourself with [Decent Messaging terminology](terminology.md) before reviewing the available commands.

## API Endpoints
 
The base URL for the API is `http://localhost:7771/api/v1/`.

### Personal messages

Endpoint: `/api/v1/personal-messages`

| Method   | Parameters               | Description   |
| -------- | ------------------------ | ------------- |
| GET      | -                        | Retrieves a list of all personal message IDs |
| GET      | since=X                  | Retrieves a list of all personal message IDs received since the UNIX timestamp X |
| GET      | id=X                     | Retrieves the personal message with an ID of X |
| POST     | delete=X                 | Deletes the personal message with an ID of X |

### Messages

Endpoint: `/api/v1/messages`

| Method   | Parameters                            | Description   |
| -------- | ------------------------------------- | ------------- |
| POST     | recipientAddress=X, subject=Y, body=Z | Queues a new message to be sent to the DM Address defined by X, with the subject Y and body content Z |

### Nodes

Endpoint: `/api/v1/nodes`

| Method   | Parameters     | Description   |
| -------- | -------------- | ------------- |
| POST     | host=X, port=Y | Adds a new node with the host X and port number Y, which will be connected to if necessary |
