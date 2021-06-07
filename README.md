# Netty Client to Client Relay

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

## 4. 제한 사항

***Netty Client to Client Relay*** 는 다음과 같은 제한 사항을 가지고 있습니다:

* ***Netty Client to Client Relay*** 는 VPN이 아닙니다. 따라서 현재로써는 통신 구간에 대한 암호화가 되지 않습니다. 만일 데이터 통신 구간에 대한 암호화가 필요한 경우 기존 사용중인 서버와 클라이언트 사이에 통신 암호화를 적용시켜 주시기 바랍니다.  
(통신 구간 암호화의 경우 향후 적용 예정입니다.)
* ***Netty Client to Client Relay*** 는 현재 로그 기능을 지원하지 않습니다. 추후 로그 기능 추가 예정입니다.
* ***Netty Client to Client Relay*** 는 릴레이 서버와 기존 서버의 연결이 끊길 시 자동으로 재접속하는 기능이 없습니다. 사용 시 참조 부탁드립니다.
* ***Netty Client to Client Relay*** 는 현재 인증 기능이 없이 단순 릴레이 서버로써만 작동하며, 향후 인증 기능 추가 예정입니다. 따라서 현재 버전은 통신 데이터 내용이 유출되어도 지장이 없는 경우에 사용하시기 바랍니다.

## 5. 다운로드

***Netty Client to Client Relay*** 는 `client2client.jar`, `server2server.jar` 의 두 개의 파일로 구성되어 있습니다.

* `client2client.jar` 파일은 기존 서버 위치에 다운로드 받아주시기 바랍니다.
* `server2server.jar` 파일은 중개 서버 위치에 다운로드 받아주시기 바랍니다.

## 6. 사용법

***Netty Client to Client Relay*** 는 JRE 1.8에서 빌드되었습니다. 따라서 사용을 위해 각각의 서버에 JRE 1.8 이상 설치가 필요합니다.

기존 서버의 `client2client.jar` 파일의 사용법은 다음과 같습니다.

```txt
java -jar [Path of This JAR] [Inbound Port for Your Server App] [Server2Server Address] [Server2Server Port]
```

각 파라미터는 다음과 같습니다.

* [Path of This JAR] : `client2client.jar` 파일의 경로
* [Inbound Port for Your Server App] : 기존 서버 프로그램이 수신 대기중인 포트
* [Server2Server Address] : 중개서버의 주소
* [Server2Server Port] : 중개서버의 `server2server` 프로그램에서 지정한 포트

중개 서버의 `server2server.jar` 파일의 사용법은 다음과 같습니다.

```txt
java -jar [Path of This JAR] [Server2Server Port]
```

각 파라미터는 다음과 같습니다.

* [Path of This JAR] : `server2server.jar` 파일의 경로
* [Server2Server Port] : `server2server` 프로그램에서 사용할 포트

다음은 기존 서버 SSH 접속을 중개 서버로 릴레이하는 예제입니다. 해당 예제에서는 중개 서버(10.0.0.1)의 4000번 포트로 클라이언트가 접속할 시 기존 서버의 22번 포트로 TCP 접속이 이루어지도록 구성하였습니다.

* 기존 서버 사용법 예제

```sh
java -jar ./client2client.jar 22 10.0.0.1 4000
```

* 중개 서버 사용법 예제

```sh
java -jar ./server2server.jar 4000
```
