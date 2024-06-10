package guru.sfg.beer.order.service.services.senders;

public interface EventSender <E,  T>{

    void sendEvent(T entity, E event);

}
