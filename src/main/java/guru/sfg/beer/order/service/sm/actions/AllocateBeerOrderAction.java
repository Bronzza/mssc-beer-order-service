package guru.sfg.beer.order.service.sm.actions;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import common.events.AllocationBeerOrderRequest;
import common.events.ValidateBeerOrderRequest;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
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
public class AllocateBeerOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper mapper;
    private final BeerOrderRepository repository;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        log.info("Allocation action:...");

        UUID orderId = context.getMessage().getHeaders().get(BeerOrderManagerImpl.PAYMENT_ID_HEADER, UUID.class);
        BeerOrder beerOrder = repository.findOneById(orderId);
        AllocationBeerOrderRequest allocationBeerOrderRequest = new AllocationBeerOrderRequest(mapper.beerOrderToDto(beerOrder));

        log.info("Allocation request was send to: {}, //n with message {}", JmsConfig.BEER_ORDER_VALIDATION_REQUEST,
                allocationBeerOrderRequest);
        jmsTemplate.convertAndSend(JmsConfig.BEER_ORDER_ALLOCATION_REQUEST, allocationBeerOrderRequest);
    }
}
