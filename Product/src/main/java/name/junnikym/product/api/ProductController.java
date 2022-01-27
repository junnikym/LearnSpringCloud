package name.junnikym.product.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/product", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

	@GetMapping("/{id}")
	public String getProductInfo(@PathVariable("id") Long id) {
//		throw new RuntimeException("I/O Exception");
//		try {
//			Thread.sleep(2000);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("Call Product ID : " + id);
		return "product id is " + String.valueOf(id);
	}

}
