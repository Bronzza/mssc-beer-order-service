package guru.sfg.beer.order.service.sm.actions;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationExceptionBeerOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        log.info("Validation exception action:...");
        UUID orderId = (UUID) context.getMessageHeader(BeerOrderManagerImpl.PAYMENT_ID_HEADER);
        log.info("Validation failed, do compensation transaction for order with id: {}", orderId );
    }
}
