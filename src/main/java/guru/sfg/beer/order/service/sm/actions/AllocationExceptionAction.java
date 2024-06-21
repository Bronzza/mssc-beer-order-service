package guru.sfg.beer.order.service.sm.actions;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import common.events.AllocationBeerOrderRequest;
import common.events.AllocationFailureResponse;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationExceptionAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        log.info("Allocation exception action:...");
        UUID orderId = (UUID) context.getMessageHeader(BeerOrderManagerImpl.PAYMENT_ID_HEADER);
        log.info("Allocation failed, do compensation transaction for order with id: {}", orderId );
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATION_FAILURE, new AllocationFailureResponse(orderId));
    }
}
