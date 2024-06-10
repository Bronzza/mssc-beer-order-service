package guru.sfg.beer.order.service.testcomponents;


import common.events.AllocationBeerOrderRequest;
import common.events.AllocationBeerOrderResponse;
import guru.sfg.beer.order.service.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderAllocationListenerTestBean {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.BEER_ORDER_ALLOCATION_REQUEST)
    public void lisn(Message request) {
        AllocationBeerOrderRequest allocationRequest = (AllocationBeerOrderRequest) request.getPayload();

        log.info("===================  Allocation (test) listener was working ====================");

        allocationRequest.getBeerOrder().getBeerOrderLines().forEach(line -> {
            line.setQuantityAllocated(line.getOrderQuantity());
        });

        jmsTemplate.convertAndSend(JmsConfig.BEER_ORDER_ALLOCATION_RESPONSE,
                new AllocationBeerOrderResponse(allocationRequest.getBeerOrder(), false, false));

    }
}
