package guru.sfg.beer.order.service.services;

import common.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;

import java.util.UUID;

public interface BeerOrderManager {
    BeerOrder newBeerOrder(BeerOrder beerOrder);
    void processValidationResult(UUID id, Boolean isValid);
    void processAllocationResult(BeerOrderDto beerOrderDto, Boolean isAllocationError, Boolean isPendingInventory);
    void proccessPickUp(UUID id);
    void cancelOrder(UUID id);
}
