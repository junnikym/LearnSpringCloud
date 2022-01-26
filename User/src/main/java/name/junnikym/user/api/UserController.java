package name.junnikym.user.api;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.RequiredArgsConstructor;
import name.junnikym.user.client.ProductClient;
import name.junnikym.user.serivce.ProductRemoteService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

//	private final ProductClient productClient;
	private final ProductRemoteService productRemoteService;

	@GetMapping("/own/product/{productId}")
	public String getProductInfo(@PathVariable("productId") Long productId) {
		return productRemoteService.getProductInfo(productId);
	}

}
