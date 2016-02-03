# Local API

Upon start-up, Decent Messaging listens on TCP port 8881 (by default) for local API connections. Its scope is limited to `localhost` (`127.0.0.1`) for security.

The local API protocol is simple and text based, making it easy to integrate into any language that support TCP socket communications.

## Terminology

It is worth familiarising yourself with [Decent Messaging terminology](terminology.md) before reviewing the available commands.

## Commands
 
The following text commands are available for use with the local API.

* `send` - Prompts for message details and then creates a new DecentMessaging message that will be automatically distributed through the peer to peer network
* `get` - Prompts for a personal message ID and returns the content of that personal message.
* `delete` - Prompts for a personal message ID and deletes the personal message record and file.  
* `list` - Prompts for a UNIX timestamp and then returns a list of all personal message IDs received after that timestamp. The personal message details can then be looked up using the `get` command.
* `me` - Returns the DM address of the currently running DecentMessaging node.

## Protocol

*TODO: Describe the back and forth between the client and the local API server.*

The protocol uses numeric codes prefixed with an asterisk to indicate status. These are presented in the following format

* `*1XX` - Requests for required information, such as the command to run or command parameters.
* `*2XX` - Errors. See the 'Error codes' table below for more details.
* `*3XX` - Informational messages, usually indicating success or providing results.

### Request/Informational codes

*TODO: Provide a reference for all informational codes that may be encountered.*

### Error codes

| Error Number  | Description |
| ------------- | ------------- |
| 200  | Client attempted to execute an invalid command. |
| 201  | DecentMessaging could not calculate the recipient public key. The DM address provided by the client may be invalid. |
| 202  | Client failed to provide required data to construct a new message.  |
| 203  | An internal error in DecentMessaging prevented a new message being created. |
| 204  | An internal error in DecentMessaging prevented verification of new message creation. |
| 205  | The DM address provided by the client is invalid. |
| 211  | Client provided an non-numeric personal message ID while attempting to retrieve a personal message. |
| 212  | Client requested a personal message that does not exist. |
| 221  | Client provided a non-numeric timestamp value when attempting to retrieve a list of personal message IDs. |
| 241  | Client provided an non-numeric personal message ID while attempting to delete a personal message. |
| 242  | Client requested deletion of a personal message that does not exist. |
| 244  | An error occurred deleting the personal message. The database record for this personal message and/or its content within the personal message file may not have been deleted. |
