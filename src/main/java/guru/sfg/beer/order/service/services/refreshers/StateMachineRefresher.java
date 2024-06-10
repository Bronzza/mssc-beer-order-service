package guru.sfg.beer.order.service.services.refreshers;

import org.springframework.statemachine.StateMachine;

public interface StateMachineRefresher <S,E,I> {

    StateMachine<S,E> build(I id, S currentState);
}
