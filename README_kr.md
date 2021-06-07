# Netty Client to Client Relay

## 1. Netty Client to Client Relay는 무엇인가요?

***Netty Client to Client Relay*** 는 중개 서버를 사용하여 클라이언트와 서버간의 TCP 소켓 통신을 클라이언트와 클라이언트의 통신처럼 Outbound 트래픽을 사용하여 중개하여 연결하여주는 유틸리티 프로그램입니다.

## 2. 기존 릴레이 툴과 다른 점은 무엇인가요?

***socat*** 으로 대표되는 기존 릴레이 툴의 경우 중개하여주는 서버에서 중개받는 서버로의 inbound 통신이 가능해야만 하였습니다. 이에 따라 크게 다음과 같은 두 가지 제약사항이 생기게 되었습니다.

1. 중개 서버에서 중개받고자 하는 서버로의 inbound 통신이 가능해야 합니다.
2. 중개 서버는 중개받고자 하는 서버의 IP 주소를 알고 있어야만 합니다.

만약 중개받고자 하는 서버가 방화벽이나 네트워크상의 기타 제약 사항으로 인해 inbound 통신이 불가능할 경우, 기존 릴레이 툴로는 클라이언트를 서버로 접속 시킬 수 없었습니다. 또한 사용하고자 하는 서버가 DHCP 등으로 인해 고정 IP 주소가 아닌 유동 IP 주소를 사용하는 경우에도 마찬가지로 기존 릴레이 툴의 적용이 불가능하였습니다.

***Netty Client to Client Relay*** 는 기존 서버의 변경 없이 서버 접속을 클라이언트화시켜 중개 서버로 접속하게 함으로써 기존 릴레이 툴이 가지고 있던 제약 사항을 피할 수 있습니다.  

이를 통해 유동 IP 주소를 사용하는 고성능의 로컬 서버를 저사양의 클라우드 서버 VM을 사용하여 중개함으로써 비용을 절감하거나, inbound 트래픽이 방화벽으로 제한되어있는 공간에서의 PC에 RDP 등으로 원격 접속하는 등의 활용이 가능합니다.

## 3. 기능

***Netty Client to Client Relay*** 는 다음과 같은 기능을 가지고 있습니다:

* 기존 서버를 클라이언트화 하여 중개 서버로 접속시킵니다.
* 중개 서버에 새로운 클라이언트 접속 시 기존 서버에서도 새로운 클라이언트가 접속되어 중개됩니다.
* 기존 서버의 통신 기능에 영향을 미치지 않습니다.
* JVM을 구동 가능한 모든 OS 환경에서 실행 가능합니다.
* 하나의 중개 받는 서버로 여러 클라이언트가 동시 접속이 가능합니다.
* TCP 통신 기반의 모든 프로토콜에서 사용 가능합니다.
* 중개 서버의 리소스를 적게 사용합니다.

## 4. 제한 사항

***Netty Client to Client Relay*** 는 다음과 같은 제한 사항을 가지고 있습니다:

* ***Netty Client to Client Relay*** 는 VPN이 아닙니다. 따라서 현재로써는 통신 구간에 대한 암호화가 되지 않습니다. 만일 데이터 통신 구간에 대한 암호화가 필요한 경우 기존 사용중인 서버와 클라이언트 사이에 통신 암호화를 적용시켜 주시기 바랍니다.  
(통신 구간 암호화의 경우 향후 적용 예정입니다.)
* ***Netty Client to Client Relay*** 는 현재 로그 기능을 지원하지 않습니다. 추후 로그 기능 추가 예정입니다.
* ***Netty Client to Client Relay*** 는 릴레이 서버와 기존 서버의 연결이 끊길 시 자동으로 재접속하는 기능이 없습니다. 사용 시 참조 부탁드립니다.

## 5. 다운로드

[Latest release](https://github.com/kyuseok-oh/netty-client-to-client-relay/releases)

***Netty Client to Client Relay*** 는 `client2client.jar`, `server2server.jar` 의 두 개의 파일로 구성되어 있습니다.

* `client2client.jar` 파일은 기존 서버 위치에 다운로드 받아주시기 바랍니다.
* `server2server.jar` 파일은 중개 서버 위치에 다운로드 받아주시기 바랍니다.

## 6. 사용법

***Netty Client to Client Relay*** 는 OpenJDK 1.8에서 빌드 및 테스트 되었습니다. 따라서 사용을 위해 각각의 서버에 JRE 1.8 이상 설치가 필요합니다.

> 서버 실행 시 순서는 먼저 중개 서버의 `server2server.jar` 를 실행시킨 뒤 기존 서버의 `client2client.jar` 파일을 실행시켜야 합니다.

중개 서버의 `server2server.jar` 파일의 사용법은 다음과 같습니다.

```txt
java -jar [Path of This JAR] [Server2Server Port] [(Optional) API Auth Key]
```

각 파라미터는 다음과 같습니다.

* [Path of This JAR] : `server2server.jar` 파일의 경로
* [Server2Server Port] : `server2server` 프로그램에서 사용할 포트
* [(Optional) API Auth Key] : 기존 서버와의 API 암호화 및 인증에 필요한 키 문자열이며, 임의의 값을 입력한다. 미입력시 중개서버 실행 과정에서 랜덤으로 키 생성 후 화면에 키값을 출력한다.

기존 서버의 `client2client.jar` 파일의 사용법은 다음과 같습니다.

```txt
java -jar [Path of This JAR] [Inbound Port for Your Server App] [Server2Server Address] [Server2Server Port] [API Auth Key]
```

각 파라미터는 다음과 같습니다.

* [Path of This JAR] : `client2client.jar` 파일의 경로
* [Inbound Port for Your Server App] : 기존 서버 프로그램이 수신 대기중인 포트
* [Server2Server Address] : 중개서버의 주소
* [Server2Server Port] : 중개서버의 `server2server` 프로그램에서 지정한 포트
* [API Auth Key] : 중개서버와의 API 암호화 및 인증에 필요한 키 문자열이며, 중개서버에서 생성된 값 또는 중개서버 실행 시 입력한 임의의 값을 동일하게 입력한다.

다음은 중개 서버를 통해 기존 서버 SSH 접속을 외부 클라이언트로 릴레이하는 예제입니다. 해당 예제에서는 중개 서버(10.0.0.1)의 4000번 포트로 클라이언트가 접속할 시 기존 서버의 22번 포트로 TCP 접속이 이루어지도록 구성하였습니다.

* 중개 서버 사용법 예제 (사용자 키 사용 시)

```sh
java -jar ./server2server.jar 4000 testApiAuthKey
```

* 기존 서버 사용법 예제 (사용자 키 사용 시)

```sh
java -jar ./client2client.jar 22 10.0.0.1 4000 testApiAuthKey
```

다음은 위 예제와 동일하지만 사용자 키 대신 자동 생성되는 키를 사용하는 예제입니다.

* 중개 서버 사용법 예제 (키 생성 사용 시)

```sh
java -jar ./server2server.jar 4000
```

출력 :

```sh
Generated API Auth Key :
uUFwcbknu78fzZ5uXjE_v5J_AQRd3c5F
```

* 기존 서버 사용법 예제 (생성된 키 사용 시)

```sh
java -jar ./client2client.jar 22 10.0.0.1 4000 uUFwcbknu78fzZ5uXjE_v5J_AQRd3c5F
```
