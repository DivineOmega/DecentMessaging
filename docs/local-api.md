# Local API

Upon start-up, Decent Messaging listens on TCP port 8881 (by default) for local API connections. Its scope is limited to `localhost` (`127.0.0.1`) for security.

The local API protocol is simple and text based, making it easy to integrate into any language that support TCP socket communications.

## Terminology

It is worth familiarising yourself with [Decent Messaging terminology](terminology.md) before reviewing the available commands.

## Commands
 
The following text commands are available for use with the local API.

* `send` - Prompts for message details and then creates a new DecentMessaging message that will be automatically distributed through the peer to peer network
* `get` - Prompts for a personal message ID and returns to content of that personal message. 
* `list` - Prompts for a UNIX timestamp and then return a list of all personal message IDs received after that timestamp. The personal message details can then be looked up using the `get` command.
* `me` - Returns the DM address of the currently running DecentMessaging node.

## Protocol

*TODO: Describe the back and forth between the client and the local API server.*

*TODO: Describe use of HTTP style error code responses.*

### Request/Informational codes

*TODO: Provide a reference for all informational codes that may be encountered.*

### Error codes

*TODO: Provide a reference for all error code that may be encountered.*