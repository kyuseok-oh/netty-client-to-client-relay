# Netty Client to Client Relay

[![English](https://img.shields.io/badge/language-English-orange.svg)](README.md) [![Korean](https://img.shields.io/badge/language-Korean-blue.svg)](README_kr.md)

## 1. What is Netty Client to Client Relay?

***Netty Client to Client Relay*** is a utility program that uses an relay server to connect TCP socket communication between a client and a server using outbound traffic, just like the communication between a client and a client.

## 2. How is it different from traditional relay tools?

In the case of the existing relay tool represented by ***socat***, inbound communication from the relay server to the legacy server had to be possible. As a result, there are two major limitations:

1. Inbound communication from the relay server to the legacy server must be possible.
2. The relay server must know the IP address of the legacy server.

If the legacy server cannot communicate inbound due to firewalls or other restrictions on the network, the existing relay tool could not connect the client to the server. Also, even if the legacy server you want to use uses a dynamic IP address instead of a static IP address, the existing relay tool could not be applied.

***Netty Client to Client Relay*** avoids the limitations of existing relay tools by creating a new client from the legacy server and connecting to the relay server when a client connection to the legacy server occurs without changing the legacy server.

Through this, it is possible to reduce costs by relaying a high-performance local server using a dynamic IP address through a low-performance cloud server VM using a static IP address.

In addition, it can be used for remote access via RDP to a PC in a space where inbound traffic is restricted by a firewall.

## 3. Features

***Netty Client to Client Relay*** has the following features:

* The new legacy server connection is turned into a client connection and connected as a relay server.
* When a new client connects to the relay server, new clients are also generated and connected to the legacy server and relay.
* It does not affect the communication of the legacy server and client.
* It can run in any OS environment that can run JVM.
* Multiple clients can connect simultaneously with one relay server and a legacy server.
* Available for all protocols based on TCP.
* It uses less resources on the relay server.

## 4. Restrictions

***Netty Client to Client Relay*** has the following restrictions:

* ***Netty Client to Client Relay*** is not a VPN. Therefore, at present, the communication section is not encrypted. If encryption for the data communication section is required, please apply communication encryption between the legacy server and client.  
(Communication section encryption will be applied in the future.)
* ***Netty Client to Client Relay*** does not currently support logging. Logging will be added in the future.
* ***Netty Client to Client Relay*** does not have a feature to automatically reconnect when the connection between the relay server and the legacy server is lost. Please refer to it when using.

## 5. Download

[Latest release](https://github.com/kyuseok-oh/netty-client-to-client-relay/releases)

***Netty Client to Client Relay*** consists of two files: `client2client.jar` and `server2server.jar`.

* Please download the `client2client.jar` file to the legacy server.
* Please download the `server2server.jar` file to the relay server.

## 6. How to use

***Netty Client to Client Relay*** is built and tested with OpenJDK 1.8. Therefore, OpenJDK 1.8 or higher installation is required on each server for use.

> In order to run the server, first execute `server2server.jar` of the relay server, and then execute `client2client.jar` of the legacy server.

The usage of the relay server's `server2server.jar` file is as follows.

```txt
java -jar [Path of This JAR] [Server2Server Port] [(Optional) API Auth Key]
```

Each parameter is as follows.

* [Path of This JAR] : The path to the `server2server.jar` file.
* [Server2Server Port] : The port to be used by the `server2server`.
* [(Optional) API Auth Key] : It is a key string required for API encryption and authentication with the legacy server, and input an arbitrary string. If no input, a key is randomly generated during the relay server execution process and the key string is output on the screen.

The usage of `client2client.jar` file of the legacy server is as follows.

```txt
java -jar [Path of This JAR] [Inbound Port for Your Server App] [Server2Server Address] [Server2Server Port] [API Auth Key]
```

Each parameter is as follows.

* [Path of This JAR] : Path to `client2client.jar` file.
* [Inbound Port for Your Server App] : The port on which the existing server program is listening.
* [Server2Server Address] : Address of the relay server.
* [Server2Server Port] : Port specified by `server2server` program of the relay server.
* [API Auth Key] : It is a key string required for API encryption and authentication with the relay server. Please enter the same string generated by the relay server or the same string entered when start the relay server.

The following is an example of relaying an legacy server SSH connection to a client via relay server. In this example, when a client connects to port 4000 of the relay server (10.0.0.1), TCP connection is made to port 22 of the legacy server.

* Example of how to use a server2server (when using a custom API Auth key)

```sh
java -jar ./server2server.jar 4000 testApiAuthKey
```

* Example of how to use a client2client (when using a custom API Auth key)

```sh
java -jar ./client2client.jar 22 10.0.0.1 4000 testApiAuthKey
```

The following example is identical to the example above, but uses an auto-generated API Auth key instead of a custom API Auth key.

* Example of how to use a server2server (when using a auto-generated API Auth key)

```sh
java -jar ./server2server.jar 4000
```

Console Output :

```sh
Generated API Auth Key :
uUFwcbknu78fzZ5uXjE_v5J_AQRd3c5F
```

* Example of how to use a client2client (when using a auto-generated API Auth key)

```sh
java -jar ./client2client.jar 22 10.0.0.1 4000 uUFwcbknu78fzZ5uXjE_v5J_AQRd3c5F
```
