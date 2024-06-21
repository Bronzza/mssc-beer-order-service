package guru.sfg.beer.order.service.services.jmslisteners;


import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import common.events.ValidateBeerOrderResponse;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BeerOrderValidationResultListener {

    @Autowired
    @Qualifier("default") private  BeerOrderManager beerOrderManagerImpl;

    @Transactional
    @JmsListener(destination = JmsConfig.VALIDATION_BEER_ORDER_RESPONSE)
    public void listen(ValidateBeerOrderResponse event) {
        Boolean isValid = event.getIsValid();

        beerOrderManagerImpl.processValidationResult(event.getOrderId(), event.getIsValid());

        log.info("Event sent to state machine, with status: {} /n and event: {}",
                isValid ? BeerOrderStatusEnum.VALIDATED : BeerOrderStatusEnum.VALIDATION_EXCEPTION,
                isValid ? BeerOrderEventEnum.VALIDATION_PASSED : BeerOrderEventEnum.VALIDATION_FAILED);
    }
}
