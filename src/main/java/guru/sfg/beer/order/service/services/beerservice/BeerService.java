package guru.sfg.beer.order.service.services.beerservice;

import common.model.BeerDto;

import java.util.Optional;
import java.util.UUID;


public interface BeerService {
    Optional<BeerDto> getBeerDtoById(UUID beerId);
    Optional<BeerDto> getBeerDtoByUpc(String upc);
}
