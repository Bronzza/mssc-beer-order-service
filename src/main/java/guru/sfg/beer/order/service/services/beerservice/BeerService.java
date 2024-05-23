package guru.sfg.beer.order.service.services.beerservice;

import guru.sfg.beer.order.service.web.model.BeerDto;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


public interface BeerService {
    Optional<BeerDto> getBeerDtoById(UUID beerId);
    Optional<BeerDto> getBeerDtoByUpc(String upc);
}
