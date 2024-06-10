package guru.sfg.beer.order.service.services.senders;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.services.refreshers.BeerOrderStateMachineRefresher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BeerOrderStateMachineEventSender implements EventSender<BeerOrderEventEnum, BeerOrder> {

    public static final String PAYMENT_ID_HEADER = "beer_order_id";

    private final BeerOrderStateMachineRefresher refresher;

    @Override
    public void sendEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm =
                refresher.build(beerOrder.getId(), beerOrder.getOrderStatus());

        Message<BeerOrderEventEnum> msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, beerOrder.getId())
                .build();

        sm.sendEvent(msg);
    }
}
