package guru.sfg.beer.order.service.sm.actions;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import common.events.AllocationBeerOrderRequest;
import common.events.DeAllocationBeerOrderRequest;
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

import javax.jms.Destination;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class DeAllocateBeerOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper mapper;
    private final BeerOrderRepository repository;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        log.info("Allocation action:...");

        UUID orderId = context.getMessage().getHeaders().get(BeerOrderManagerImpl.PAYMENT_ID_HEADER, UUID.class);
        BeerOrder beerOrder = repository.findById(orderId).orElseThrow();
        DeAllocationBeerOrderRequest deAllocationBeerOrderRequest = new DeAllocationBeerOrderRequest(mapper.beerOrderToDto(beerOrder));

        log.info("Allocation request was send to: {}, //n with message {}", JmsConfig.DE_ALLOCATION_BEER_ORDER_REQUEST,
                deAllocationBeerOrderRequest);
        jmsTemplate.convertAndSend(JmsConfig.DE_ALLOCATION_BEER_ORDER_REQUEST, deAllocationBeerOrderRequest);
    }
}