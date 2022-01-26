package name.junnikym.user.serivce;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
		return productId.toString()+" is product is sold out";
	}

}
