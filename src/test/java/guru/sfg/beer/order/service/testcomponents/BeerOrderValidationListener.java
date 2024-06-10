package guru.sfg.beer.order.service.testcomponents;


import common.events.ValidateBeerOrderRequest;
import common.events.ValidateBeerOrderResponse;
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
public class BeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.BEER_ORDER_VALIDATION_REQUEST)
    public void lisn(Message request) {
        ValidateBeerOrderRequest validateReq = (ValidateBeerOrderRequest) request.getPayload();

        log.info("===================  Validation (test) listener was working ====================");

        jmsTemplate.convertAndSend(JmsConfig.BEER_ORDER_VALIDATION_RESPONSE,
                new ValidateBeerOrderResponse(validateReq.getBeerOrder().getId(), true));

    }
}
