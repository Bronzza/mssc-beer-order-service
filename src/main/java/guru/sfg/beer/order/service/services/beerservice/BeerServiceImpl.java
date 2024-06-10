package guru.sfg.beer.order.service.services.beerservice;

import common.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Component
public class BeerServiceImpl implements BeerService{

    public final static String UUID_PATH = "/api/v1/beer/{beerId}";
    public final static String UPC_PATH = "/api/v1/beerUpc/";
    private final RestTemplate restTemplate;
    private  String beerServiceHost;


    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
//        this.beerServiceHost = "localhost:8083";
    }

    @Override
    public Optional<BeerDto> getBeerDtoById(UUID beerId) {
        log.info("Calling Beer Service, UUID");
        ResponseEntity<Optional<BeerDto>> response = restTemplate.exchange(beerServiceHost + UUID_PATH, HttpMethod.GET, null,
                new ParameterizedTypeReference<Optional<BeerDto>>() {}, (Object) beerId);
        return response.getBody();
    }

    @Override
    public Optional<BeerDto>  getBeerDtoByUpc(String upc) {
        log.info("Calling Beer Service, UPC");
        return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + UPC_PATH + upc, BeerDto.class));
    }
}
