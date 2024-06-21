package guru.sfg.beer.order.service.testcomponents;


import common.events.AllocationBeerOrderRequest;
import common.events.AllocationBeerOrderResponse;
import common.model.BeerOrderDto;
import guru.sfg.beer.order.service.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderAllocationRequestListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATION_BEER_ORDER_REQUEST)
    public void lisn(Message request) {
        Boolean allocationFailed = false;
        Boolean pendingInventory = false;
        AllocationBeerOrderRequest allocationRequest = (AllocationBeerOrderRequest) request.getPayload();

        log.info("===================  Allocation (test) listener was working ====================");
        BeerOrderDto beerOrder = allocationRequest.getBeerOrder();
        beerOrder.getBeerOrderLines().forEach(line -> {
            line.setQuantityAllocated(line.getOrderQuantity());
        });
        pendingInventory = beerOrder.getCustomerRef() != null && beerOrder.getCustomerRef().startsWith("Allocation-pending");
        allocationFailed = beerOrder.getCustomerRef() != null && beerOrder.getCustomerRef().startsWith("Allocation-failed");


        jmsTemplate.convertAndSend(JmsConfig.ALLOCATION_BEER_ORDER_RESPONSE,
                new AllocationBeerOrderResponse(beerOrder, allocationFailed,
                        pendingInventory));

    }
}
