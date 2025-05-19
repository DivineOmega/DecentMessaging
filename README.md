# Decent Messaging
Decent Messaging - a decentralised encrypted messaging system platform

## Quick Start

1. Ensure you have a recent version of Java installed.
2. Download the latest version of `DecentMessaging.jar` from the [releases page](https://github.com/DivineOmega/DecentMessaging/releases).
3. Double-click the downloaded file, or run it via the command line using `java -jar DecentMessaging.jar`.

## Command Line Arguments

By default, Decent Messaging will start using default ports, with a GUI to monitor and manage your Decent Messaging node.
You can customise this behaviour by specifying various command line arguments.

| Argument        | Description |
| --------------- | ----------------- |
| `--hidden`        | Do not show any GUI. Useful for integrating Decent Messaging into your own application. |
| `--portable`      | Store Decent Messaging files and database in the same directory as the binary, rather than in the user's home directory. Useful if you want your application to have its own instance of Decent Messaging, instead of potentially sharing its data and DM Address with other applications. |
| `--peer-server-port 9991` | Change the port Decent Messaging listens on for connections from other nodes. |
| `--local-server-port 7771` | Change the port Decent Messaging listens on for local API connections. |

## Integrating with Decent Messaging

If you wish to build an application or service on top of the Decent Messaging network, your application will need to:

1. Start the Decent Messaging system.
2. Communicate with the network via the [Decent Messaging local API](docs/local-api.md).
