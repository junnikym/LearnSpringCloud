# TIL; Spring Cloud

## Index
1. [Feign](#1.-Spring-Cloud-Feign)

<br/>

---
<p align="right"> <sup>2022-01-17</sup><br/> </p>

## 1. Spring Cloud Feign
Netflix에서 개발된 HTTP Client Binder; REST Template 호출 등을 JPA Repository와 같이 Interface로 추상화 <br/>
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
Root Package에 '@EnableFeignClients' Annotation을 추가하여 FeignC Client 사용여부를 선언
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

** [Hytrix란?]()

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