package guru.sfg.beer.order.service.testcomponents;


import common.events.ValidateBeerOrderRequest;
import common.events.ValidateBeerOrderResponse;
import common.model.BeerOrderDto;
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
public class BeerOrderValidationRequestListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATION_BEER_ORDER_REQUEST)
    public void lisn(Message request) {
        Boolean isValid = true;
        ValidateBeerOrderRequest validateReq = (ValidateBeerOrderRequest) request.getPayload();
        BeerOrderDto beerOrder = validateReq.getBeerOrder();
        if (beerOrder.getCustomerRef() != null) {
            isValid = !beerOrder.getCustomerRef().startsWith("Invalid");
        }

        log.info("===================  Validation (test) listener was working ====================");

        jmsTemplate.convertAndSend(JmsConfig.VALIDATION_BEER_ORDER_RESPONSE,
                new ValidateBeerOrderResponse(validateReq.getBeerOrder().getId(), isValid));

    }
}
