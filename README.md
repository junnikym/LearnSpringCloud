# TIL; Spring Cloud

## Index
1. [Hystrix](#1.-Circuit-breaker---Hystrix) - <sup>2022-01-26</sup>
2. [Feign](#2.-Declarative-Http-Client---Feign) - <sup>2022-01-17</sup>

<br/>

---
<p align="right"> <sup>2022-01-26</sup><br/> </p>

## 1. Circuit breaker - Hystrix
Circuit breaker; 전류를 차단하는 장치이다. ```Hystrix```는 Micro Service의 Circuit Breaker 역할. <br/>
문제가 있는 Micro Service 로의 트래픽을 차단; 전체 서비스의 문제를 미리 방지

### 문제점 

분산 시스템 / 클라우드 환경에선 실패는 일반표준이다 (Failure as a First Class Citizen); <br/>
- Monolithic 환경에서는 의존성 호출을 Method를 통해 진행; 따라서 100% 신뢰
- 반면 Micro Service 환경에서 한 서비스의 가동률(uptime)이 최대 99.99% 라고 가정 시 1억개의 요청이 들어 오면 1만개의 요청이 실패하하는 셈;
  (30개의 서비스로 구성 시 -> 99.99^30 = 99.7% uptime 이므로 0.3% 인 30만개가 된다.)

<sup>** ref : https://velog.io/@chlee4858/인강-Spring-Cloud를-활용한-MSA-기초-4강-Circuit-Breaker-Hystrix </sup>

Latency Tolerance and Fault Tolerance for Distributed Systems <br/>
<sub>분산 시스템에서 지연감내 및 실패감내</sub> 

기본 설정의 임베디드 톰캣 4개로 구성된 경우
```
timeout     : infinity
maxThreads  : 200
                    ┏━━━━━━━━┓
               ┏━━━▶┃ Tomcat ┣━━━━┓
┏━━━━━━━━┓     ┃    ┗━━━━━━━━┛    ┃     ┏━━━━━━━━┓
┃ Tomcat ┣━━━━━┫                  ┣━━━━▶┃ Tomcat ┃
┗━━━━━━━━┛     ┃    ┏━━━━━━━━┓    ┃     ┗━━━━━━━━┛
               ┗━━━▶┃ Tomcat ┣━━━━┛         ▲ 마지막 톰캣이 다운 시 
                    ┗━━━━━━━━┛
```
마지막 서비스가 다운 시, Tomcat의 최대 Thread 인 200개가 꽉찰때까지 무한대기 / 200개가 꽉찬 후 Down

### 해결법
1. Container의 Thread를 직접 사용하지 못하게 함.
2. Queue로 대기열 사용 X -> 빠르게 Failure
3. 실패로부터 서비스 보호를 위해 Fallback 제공
<br/>...

<sup>** ref : https://sabarada.tistory.com/52 </sup>

<b> < Circuit Breaker Pattern > </b>

```
┏━━━━━━━━━━━┓   ┏━━━━━━━━━━━━━━━━┓   ┏━━━━━━━━━━━┓
┃ Service A ┃   ┃ CircuitBreaker ┃   ┃ Service B ┃
┃           ┃   ┃                ┃   ┃           ┃
┃  ( CALL )━╋━━━╋━━━━(Bypass)━━━━╋━━▶┃ ( GOOD )  ┃ 정상 시
┃           ┃◀━━╋━━━━━━━━━━━━━━━━╋━━━╋━━━━┛      ┃
┃           ┃   ┃                ┃   ┃           ┃
┃  ( CALL )━╋━━━╋━━━(Fallback)   ┃   ┃ ( DOWN )  ┃ 서버 다운/지연 시
┃           ┃◀━━╋━━━━━━━┛        ┃   ┃           ┃
┗━━━━━━━━━━━┛   ┗━━━━━━━━━━━━━━━━┛   ┗━━━━━━━━━━━┛
```

두 서비스 A, B 가 존재 시, A가 B로의 모든 호출은 Circuit Breaker 를 통과한다.

- B가 정상일 경우 : Traffic 을 문제업이 Bypass;
- B에 문제를 감지 : Circuit Breaker 를 강제적으로 차단; 장애가 전파됨을 막음 / Fallback Messaging 을 통해 임시 메시지를 리턴 할 수 있도록한다. 예를 들어 A가 상품조회, B가 상품추천 일 때 B에 장애가 나면 운영자가 미리 설정해둔 추천 상품을 제공하는 등 시스템의 장애를 최소화 할 수 있다.

** Hystrix는 일정횟수 이상 비정상적인 응답이 발생하면 Close 상태에서 Open 상태로 Trip(이동); - Circuit Open : 차단 상태 <br/>
** Back Pressure 란? : 수돗물이 흐를때, 물을 받아낼 것이 없다면 수돗꼭지를 잠그는 것; / MSA 에선 이런 수도꼭지를 잠그고 ```전체적인 시스탬장에로 전파되는것을 막는 것``` <br/>
** Fail Fast : 조기 차단 <br/>

<sup>** ref : https://bcho.tistory.com/1247 </sup>

Hystrix의 순서도는 아래와 같다
<p align="center"> <img src="https://techblog.woowahan.com/wp-content/uploads/img/2017-08-20/hystrix-command-flow-chart.png" width="100%" alter="result" /> </p>

### Dependency

< Gradle >
``` gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-hystrix'
    
    ...
    
//  위와 같이 추가 시 Dependency 가 추가되지 않아 버전을 명시했더니 추가가 됨     
//  implementation 'org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.1.0.RELEASE'

}
```

### 한번 적용해보기

```
┏━ User ━━━━━━━━━━━━┓ [1]                             ┏━ Product ━━━━━━━━━┓
┃            ┏━━━━━━╋━( api/../product/{id} call )━X━▶┃                   ┃
┃ someFunc(Long id) ┃    ┏━━━━━━━━━━━━━━━━━┓          ┃    product api    ┃
┃                   ┃◀━━━┃ Circuit Breaker ┃          ┃                   ┃
┗━━━━━━━━━━━━━━━━━━━┛ [2]┗━━━━━━━━━━━━━━━━━┛          ┗━━━━━━━━━━━━━━━━━━━┛
```

다음과 같이 User 서비스에서 Product 서비스를 호출 시 문제가 발생하면 Circuit Breaker 가 Fallback 을 반환하도록 만들어보자<br/>

<br/>
<b>< User Service ></b>
<p align="right"> ( User ) Controller </p> 

``` java
@SpringBootApplication
@EnableCircuitBreaker       // 추가
public class UserApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public static void main(...) {
        SpringApplication.run(...);
    }
}
```
Root Package에 '@EnableCircuitBreaker' Annotation을 추가하여 Circuit Breaker 사용여부를 선언 <br/>
서비스에서 RestTemplate을 사용할 것이니 RestTemplate 빈을 생성한다.

<p align="right"> ( User ) Controller </p>

``` java
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

	private final ProductRemoteService productRemoteService;

	@GetMapping("/own/product/{id}")
	public String getProductInfo(@PathVariable("id") Long id) {
		return productRemoteService.getProductInfo(id);
	}
}
```

<p align="right"> ( User ) Product Remote Service </p>

``` java
@RequiredArgsConstructor
@Service
public class ProductRemoteService {

	private static final String url = "http://localhost:8082/api/v1/product/";

	private final RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "getProductInfoFallback")
	public String getProductInfo(Long productId) {
		return this.restTemplate.getForObject(url+productId, String.class);
	}

	public String getProductInfoFallback(Long productId) {
		return "The product is sold out";
	}
}
```
@HystrixCommand 어노테이션을 추가할 경우 getProductInfo 메소드 호출을 Interept 하여 대신 실행; 이때 성공/실패 여부를 판단 후 그에 해당하는 조치를 취함.<br/>
이때 fallbackMethod Option을 통해 Fallback 메소드를 지정해 줄 수 있으며 문제 발생시 Fallback 메소드를 실행하여 차선의 결과를 제공할 수 있다. <br/>

<br/>
<b>< Product Service ></b>
<p align="right"> ( Product ) Controller </p> 

``` java
@RestController
@RequestMapping(value = "/api/v1/product", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

	@GetMapping("/{id}")
	public String getProductInfo(@PathVariable("id") Long id) {
		throw new RuntimeException("I/O Exception");
	}
}
```
일부로 Runtime Exception 을 발생시켜 실습을 진행하였다.

### Result

<table>
  <tr>
    <th width="50%" align="center"> <code>Product API</code> 를 호출했을 경우 </th>
    <th width="50%" align="center"> Product API 를 호출하는 <code>User API</code> 를 호출했을 경우 </th>
  </tr>
  <tr>
    <td align="center"> <img src="./screen/hystrix/result_product.png" width="100%" alter="result" /> </td>
    <td align="center"> <img src="./screen/hystrix/result_user.png" width="100%" alter="result" /> </td>
  </tr>
</table>
<br/>

### Fallback 원인을 알아보자

<p align="right">  ( User ) Product Remote Service </p>

``` java
...

    public String getProductInfoFallback(Long productId, Throwable t) {
        System.out.println("[Log] Product Remote Service In User | getProductInfo Fallback Run : " + t);
		return "The product is sold out";
	}
	
...
```
다음과 같이 Fallback 함수에 Throwable 타입의 인자를 사용하면 요청의 에러를 조회 할 수 있다.

### Hystrix의 Timeout 설정하기

@HystrixCommand 어노테이션이 붙은 메소드는 Timeout 시간 내 처리가 되지 않으면 Exception 이 발생한다. 기본 설정 시간은 1000ms (1s)

<p align="right">  ( User ) Product Remote Service </p>

``` java
    @GetMapping("/{id}")
	public String getProductInfo(@PathVariable("id") Long id) {
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Call Product ID : " + id);
		return "product id is " + String.valueOf(id);
	}
```
Product 서비스의 Controller 를 다음과 같이 수정 후 User 서비스를 호출하면 Fallback 이 호출되는것을 확인할 수 있다. <br/>
( Hystrix 의 기본 Timeout 값은 1000ms / Product 서비스는 2000ms 후 값을 리턴한다. ) <br/>

<table>
  <tr>
    <th width="50%" align="center"> User API 를 호출했을 시 Client 가 받는 값 </th>
    <th width="50%" align="center"> Product API 의 로그 </th>
  </tr>
  <tr>
    <td align="center"> <img src="./screen/hystrix/result_timeout.png" width="100%" alter="result" /> </td>
    <td align="center"> <img src="./screen/hystrix/result_timeout_product_log.png" width="100%" alter="result" /> </td>
  </tr>
</table>
하지만 위 결과를 보면 1초 후 Fallback이 반환되었음에도 2초 후 Product 서비스에서 System.out.println 가 작동되었다. <br/>
Log 출력이 가능했던 이유는 Product (불리는 입장) 의 Timeout이 아닌 User (부르는 입장) 의 Timeout 이기때문에 Product (불리는 입장) 에서는 전혀 영향이 없기때문에 정상작동하였다. <br/> 
User (부르는 입장) 에서는 Hystrix 가 Interupt 를 걸었기 때문에 Fallback이 작동. <br/>
<br/>

<b>application.yml 또는 application.properties 에서 Timeout 시간 변경</b>
``` yaml
hystrix:
  command:
  
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 3000
      
    # 특정 commandKey에 대한 설정
    getInfo:
      execution.isolation.thread.timeoutInMilliseconds: 10000
```
다음 속성에 값을 설정하면 Timeout시간이 변경된다. 위 실습의 Product 서비스가 구동되기 위해서 3000 값을 넣으면 User 서비스가 정상적으로 값을 받아오는것을 확인 할 수 있다.

``` java
@HystrixCommand(commandKey = "getInfo", fallbackMethod = "getProductInfoFallback")
```
다음과 같이 @HystrixCommand 적용 시 commandKey 속성을 정의한다면 따로 설정을 달리할 수 있다.<br/>

### Circuit Open 테스트

``` yaml
hystrix:
  command:
    default:
      circuitBreaker:
      
        # n회 이상 시 Circuit Open ( default : 20 ) 
        requestVolumeThreshold: 1
        
        # 정해진 시간 내 n% 의 요청이 Error일 시 Circuit Open ( default : 50 )
        errorThresholdPercentage: 50
        
        # 한번 Open 된 Circuit 을 n 동안 유지 ( default: 5000 )
        SleepWindowInMilliseconds: 5000
        
        # 발생하는 Error를 n 동안 카운팅 ( default : 10000 )
      metrics:
        rollingStats.timeInMilliseconds: 10000
        
        # default 값을 유지 시 
        #   10s 동안 20회 이상 호출될 경우 50% 이상 실패하면 Circuit Open
        
        # 바뀐 설정값 적용 시
        #   10s 동안 1회 이상 호출될 경우 50% 이상 실패하면 Circuit Open   
```
다음 중 <code>requestVolumeThreshold</code>, <code>errorThresholdPercentage</code> 만 위와 같이 바꾸어 Fallback 이 실행 될 수 있는 환경으로 실습해보았다. <br/>

<table>
  <tr>
    <th width="50%" align="center"> Circuit Open 조건 변경 시 </th>
  </tr>
  <tr>
    <td align="center"> <img src="./screen/hystrix/result_circuit_open.png" width="100%" alter="result" /> </td>
  </tr>
</table>

다음과 같이 Product 호출에 실패가 발견될 경우 <code>Hystrix circuit short-circuited and is OPEN</code> 라는 Runtime Exception 이 발생; Circuit 이 Open 됨을 확인할 수 있다. <br/>
이는 <code>SleepWindowInMilliseconds</code> 의 기본값임 5000ms 동안 유지된 후 Circuit Close 상태로 돌아오게된다. <br/>
<br/>

---
<p align="right"> <sup>2022-01-17</sup><br/> </p>

## 2. Declarative Http Client - Feign
Netflix에서 개발된 HTTP Client Binder; REST Template 호출 등을 JPA Repository와 같이 Interface로 추상화. <br/>
MSA를 서로 호출 시 코드의 복잡성이 높아진다. Feign를 통해 복잡성을 낮출수 있음.

<br/>

### Dependency
 < Gradle >
``` gradle
// https://spring.io/projects/spring-cloud

ext {
    set('springCloudVersion', "Hoxton.SR8")
}

dependencyManagement {
  imports {
    mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
  }
}

dependencies {
    implementation "org.springframework.cloud:spring-cloud-starter-openfeign"
    /* 또는 */
    compile("org.springframework.cloud:spring-cloud-starter-openfeign")
}
```
 < Maven >
``` xml 
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

<br/>

### 한번 적용해보기

```
┏━ User ━━━━━━━━━━━━┓ [1]                             ┏━ Product ━━━━━━━━━┓
┃            ┏━━━━━━╋━━( api/../product/{id} call )━━▶┃                   ┃
┃ someFunc(Long id) ┃                                 ┃    product api    ┃
┃                   ┃◀━━━━━━━━━━━━━━( product info )━━╋━━━━━━┛            ┃
┗━━━━━━━━━━━━━━━━━━━┛                             [2] ┗━━━━━━━━━━━━━━━━━━━┛
```

다음과 같이 User 서비스에서 Feign Client를 통해 Product 서비스의 API를 호출해보자.

``` java
@SpringBootApplication
@EnableFeignClients     // 추가
public class UserApplication {
	public static void main(...) {
		SpringApplication.run(...);
	}
}
```
Root Package에 '@EnableFeignClients' Annotation을 추가하여 Feign Client 사용여부를 선언
* backPackage 또는 BasePackageClasses를 지정해주어도 됨

``` java
@FeignClient(
    name="exampleClient", 
    url="${base.url}"            // or like url='http://localhost:9090/.../'
) 
public interface ProductClient {
    @RequestMapping(path = "/{productId}")
    String getProductInfo(@PathVariable("productId") Long productId);
}
```
( @FeignClient Options )
- name: 서비스 이름 (필수 속성)
- url: 실제 호출할 서비스의 URL
- decode404: 404응답이 올 때 FeignExeption을 발생시킬지, 아니면 응답을 decode할 지 여부
- qualifier: beanName
- configuration: 커스텀된 Configuration 삽입
- fallback: Hystrix fallback class 지정 (옵션명이 fallbackFactory일 경우 hystrix fallback factory)

<sup> 
** <a>Hytrix란?</a>
</sup>

``` java
@RestController
@RequestMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

	private final ProductClient productClient;

	public UserController(ProductClient productClient) {
		this.productClient = productClient;
	}

	@GetMapping("/own/product/{id}")
	public String getProductInfo(@PathVariable("id") Long id) {
		return productClient.getProductInfo(id);
	}
}
```

### Result

<table>
  <tr>
    <th width="50%" align="center"> Product에서 Infomation을 가져오는 API를 호출하였을 때 </th>
    <th width="50%" align="center"> User에서 FeignClient를 통해 Product의 Infomation을 가져오는 API를 호출하였을 때 </th>
  </tr>
  <tr>
    <td align="center"> <img src="./screen/feign/result_product.jpg" width="100%" alter="result" /> </td>
    <td align="center"> <img src="./screen/feign/result_user.jpg" width="100%" alter="result" /> </td>
  </tr>
</table>
<br/>

---
### Conway's way
Melvin Conway가 제안한 법칙; 시스템 구조는 설계하는 조직의 커뮤니케이션 구조와 닮았다는 내용. 
시스템을 설계하는 조직은 필연적으로 해당 조직의 커뮤니케이션 구조를 복제한 설계물을 만들게 된다.<br/>
예를 들어 4개의 팀이 하나의 시스템을 만든다면, 시스템 내부는 4단계의 구조를 갖는다.

** Inverse Conway's Law : 조직 구조가 소프트웨어 아키텍처를 결정하는 콘웨이 법칙과는 다르게 소프트웨어 아키텍처 구조가 회사 조직 구조를 결정.
<p align="right"> <sup>TIL; 2022-01-17</sup><br/> </p>