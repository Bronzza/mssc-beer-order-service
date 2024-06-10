package guru.sfg.beer.order.service.services;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import common.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.beer.order.service.services.interceptor.BeerOrderStateInterceptor;
import guru.sfg.beer.order.service.services.refreshers.BeerOrderStateMachineRefresher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String PAYMENT_ID_HEADER = "beer_order_id";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> smFactory;
    private final BeerOrderRepository repository;
    private final BeerOrderStateInterceptor interceptor;
//    private final BeerOrderStateMachineRefresher refresher;


    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder beerOrderPersisted = repository.save(beerOrder);
        sendBeerOrderEvent(beerOrderPersisted, BeerOrderEventEnum.VALIDATE_ORDER);
        return beerOrderPersisted;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID id, Boolean isValid) {
        BeerOrder beerOrder = repository.findOneById(id);
        sendBeerOrderEvent(
                beerOrder,
                isValid ? BeerOrderEventEnum.VALIDATION_PASSED : BeerOrderEventEnum.VALIDATION_FAILED);

        BeerOrder beerOrderCurrentState = repository.findOneById(id);
        sendBeerOrderEvent(beerOrderCurrentState, BeerOrderEventEnum.ALLOCATE_ORDER);
    }

    @Transactional
    @Override
    public void processAllocationResult(BeerOrderDto beerOrderDto, Boolean isAllocationError, Boolean isPendingInventory) {
        BeerOrder beerOrder = repository.findOneById(beerOrderDto.getId());
        if (isAllocationError) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        } else if (isPendingInventory) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
        }

        updateAllocatedQty(beerOrderDto, beerOrder);
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
        BeerOrder allocatedOrder = repository.findOneById(beerOrderDto.getId());

        allocatedOrder.getBeerOrderLines().forEach(line -> {
            beerOrderDto.getBeerOrderLines().forEach(dtoLine -> {
                if (dtoLine.getId().equals(line.getId())) {
                    line.setQuantityAllocated(dtoLine.getQuantityAllocated());
                }
            });
        });

        //in his implementation he save for same reason beerOrder comes as method param
        repository.saveAndFlush(allocatedOrder);
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum orderEvent) {

        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);


        Message<BeerOrderEventEnum> msg = MessageBuilder.withPayload(orderEvent)
                .setHeader(PAYMENT_ID_HEADER, beerOrder.getId())
                .build();

        sm.sendEvent(msg);

//        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm_v2
//                = refresher.build(beerOrder.getId(), beerOrder.getOrderStatus());
//        Message<BeerOrderEventEnum> msg = MessageBuilder.withPayload(orderEvent)
//                .setHeader(PAYMENT_ID_HEADER, beerOrder.getId())
//                .build();
//        sm_v2.sendEvent(msg);
    }


    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm =
                smFactory.getStateMachine(beerOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.resetStateMachine(
                            new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null,
                                    null, null));
                    sma.addStateMachineInterceptor(interceptor);
                });
        sm.start();

        return sm;
    }
}
