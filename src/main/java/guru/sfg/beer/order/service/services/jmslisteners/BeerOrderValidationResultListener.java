package guru.sfg.beer.order.service.services.jmslisteners;


import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import common.events.ValidateBeerOrderResponse;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.beer.order.service.services.senders.BeerOrderStateMachineEventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderValidationResultListener {

    private final BeerOrderManager beerOrderManagerImpl;
//    private final BeerOrderManager beerOrderManagerLoseCouplingImpl;

    @Transactional
    @JmsListener(destination = JmsConfig.BEER_ORDER_VALIDATION_RESPONSE)
    public void listen(ValidateBeerOrderResponse event) {
        Boolean isValid = event.getIsValid();

        beerOrderManagerImpl.processValidationResult(event.getOrderId(), event.getIsValid());
//        beerOrderManagerLoseCouplingImpl.processValidationResult(event.getOrderId(), event.getIsValid());

        log.info("Event sent to state machine, with status: {} /n and event: {}",
                isValid ? BeerOrderStatusEnum.VALIDATED : BeerOrderStatusEnum.VALIDATION_EXCEPTION,
                isValid ? BeerOrderEventEnum.VALIDATION_PASSED : BeerOrderEventEnum.VALIDATION_FAILED);
    }
}
