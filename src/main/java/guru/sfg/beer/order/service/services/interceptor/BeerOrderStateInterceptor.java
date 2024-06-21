package guru.sfg.beer.order.service.services.interceptor;

import guru.sfg.beer.order.service.domain.BeerOrder;
import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderStateInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository repository;

    @Override
    public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
                               Message<BeerOrderEventEnum> message,
                               Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
                               StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {

        final Consumer<UUID> updateBeerOrderAccordingStatusInEvent = (paymentId) -> {
            BeerOrder toSave = repository.findById(paymentId).orElseThrow();
            toSave.setOrderStatus(state.getId());
            repository.saveAndFlush(toSave);
        };

        Optional.ofNullable(message)
                .map(msg -> (UUID) msg.getHeaders().getOrDefault(BeerOrderManagerImpl.PAYMENT_ID_HEADER, null))
                .ifPresent(updateBeerOrderAccordingStatusInEvent);
    }
}
