package guru.sfg.beer.order.service.sm.actions;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateBeerOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper mapper;
    private final BeerOrderRepository repository;

    @Transactional
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        log.info("Validation action:...");

        UUID orderId = (UUID) context.getMessageHeader(BeerOrderManagerImpl.PAYMENT_ID_HEADER);
//        UUID orderId = (UUID) context.getMessage().getHeaders().get(BeerOrderManagerImpl.PAYMENT_ID_HEADER, UUID.class);
        BeerOrder beerOrder = repository.findById(orderId).orElseThrow();
        ValidateBeerOrderRequest validateBeerOrderEvent = new ValidateBeerOrderRequest(mapper.beerOrderToDto(beerOrder));

        log.info("Validation request was send to: {}, //n with message {}", JmsConfig.VALIDATION_BEER_ORDER_REQUEST,
                validateBeerOrderEvent);
        jmsTemplate.convertAndSend(JmsConfig.VALIDATION_BEER_ORDER_REQUEST, validateBeerOrderEvent);


    }
}
