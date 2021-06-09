# 스마트홈

wisoft IoT 플랫폼 토이 프로젝트
* 에어컨 제어
* 연구실 전등 제어

## 설계

![스크린샷 2021-06-09 오후 1 30 56](https://user-images.githubusercontent.com/29699217/121293914-70e69800-c927-11eb-9a2a-275904ca79a9.png)

## 리소스 구조

![스크린샷 2021-06-09 오후 1 35 28](https://user-images.githubusercontent.com/29699217/121293991-9673a180-c927-11eb-9572-94bbf20c5f97.png)

## 제어 시나리오

### 등록 절차

- HomeGateway는 전원 제공 시에 oneM2M Service Platform에 등록.
- Smartphone에 oneM2M 앱 설치시 oneM2M Service Platform에 등록.
- 에어컨을 제어하기 위한 Actuator는 전원 제공 시에 HomeGateway에 등록.
    - 서보모터 1개
- 전구를 제어하기 위한 Actuator는 전원 제공 시에 HomeGateway에 등록.
    - 서보모터 4개
- 연구실 내부 온'습도를 측정하기 위한 Sensor는 켜질 시에 HomeGateway에 등록.
    - 온습도 센서 1개

### 초기 리소스 생성

- 각각의 서보모터는 자신들의 상태를 저장하기 위한 컨테이너 리소스를 생성하기 위해 HomeGateway에 등록 요청 시도.
- 온 습도 센서는 센싱 값을 저장하기 위한 컨테이너 리소스를 생성하기 위해 HomeGateway에 등록 요청 시도.
- 온 습도 센서에서 새로운 contentInstance가 업데이트 될 때 마다 게이트웨이의 온 습도 컨테이너 아래에 contentInsatnce 리소스가 생성. (HomeGateway에 요청)
- 각각의 서보모터는 IN-AE의 명령 제어 명령을 수용하기 위해 컨테이너 아래에 구독(Subscription) 리소스를 생성. (HomeGateway에 요청)

### 검색 프로시저

현재 조명 및 에어컨의 상태 및 온'습도 상태를 확인하기 위해 content Instance 리소스의 발견 및 검색 단계가 필요.
- IN-AE는 컨테이너 리소스 또는 content-Instance 리소스의 발견을 위한 검색 요청. (IN-CSE → MN-CSE)
- HomeGateway는 해당 요청 container, content-Instance 리소스의 URI를 스마트폰 애플리케이션 IN-AE로 응답.

### 조명 및 에어컨 제어 프로시저

- 사용자가 자신의 스마트폰에서 에어컨을 제어하기 위해 상태를 업데이트 할 때 IN-AE는 IN-CSE에 contentInstance 생성을 하기 위한 요청 보냄.
- IN-CSE는 IN-AE로 부터 수신 받은 메시지를 해석하고, MN-CSE에 저장된 대상 컨테이너 아래에 새로운 content-Instance를 생성.
- content-Instance가 성공적으로 만들어졌다면, MN-CSE는 ADN-AE에게 상태 제어를 위한 명령을 보냄.
