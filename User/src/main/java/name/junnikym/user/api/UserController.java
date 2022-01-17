package name.junnikym.user.api;

import lombok.RequiredArgsConstructor;
import name.junnikym.user.client.ProductClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
