package guru.sfg.beer.order.service.services.refreshers;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.services.interceptor.BeerOrderStateInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BeerOrderStateMachineRefresher implements StateMachineRefresher<BeerOrderStatusEnum, BeerOrderEventEnum, UUID> {

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> smFactory;
    private final BeerOrderStateInterceptor interceptor;

    @Override
    public StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(UUID id, BeerOrderStatusEnum statusToRefresh) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm =
                smFactory.getStateMachine(id);

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {sma.resetStateMachine(
                        new DefaultStateMachineContext<>(statusToRefresh, null,
                                null, null));
                    sma.addStateMachineInterceptor(interceptor);
                });
        sm.start();

        return sm;
    }
}
