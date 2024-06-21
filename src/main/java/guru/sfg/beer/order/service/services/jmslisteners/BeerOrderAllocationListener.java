package guru.sfg.beer.order.service.services.jmslisteners;

import common.events.AllocationBeerOrderResponse;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderAllocationListener {

    @Qualifier("default") private final BeerOrderManager beerOrderManagerImpl;
//    @Qualifier("secondary")private final BeerOrderManager beerOrderManagerLoseCouplingImpl;

    @Transactional
    @JmsListener(destination = JmsConfig.ALLOCATION_BEER_ORDER_RESPONSE)
    public void listen(AllocationBeerOrderResponse event) {

        log.info("Processing allocation response, object : {}, allocationError: {}, pendingInventory: {}",
                event.getBeerOrder(), event.getAllocationError(), event.getPendingInventory());

        beerOrderManagerImpl.processAllocationResult(event.getBeerOrder(), event.getAllocationError(),
                event.getPendingInventory());
//        beerOrderManagerLoseCouplingImpl.processAllocationResult(event.getBeerOrder(),
//                event.getAllocationError(),                event.getPendingInventory());
    }
}
