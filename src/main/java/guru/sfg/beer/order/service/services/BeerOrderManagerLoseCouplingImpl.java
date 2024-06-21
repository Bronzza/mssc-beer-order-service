package guru.sfg.beer.order.service.services;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import common.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.senders.BeerOrderStateMachineEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Qualifier("secondary")
public class BeerOrderManagerLoseCouplingImpl implements BeerOrderManager {

    private final BeerOrderRepository repository;
    private final BeerOrderStateMachineEventSender sender;

    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder beerOrderPersisted = repository.save(beerOrder);
        sender.sendEvent(beerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return beerOrderPersisted;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID id, Boolean isValid) {
        BeerOrder beerOrder = repository.findById(id).orElseThrow();

        if (isValid) {
            sender.sendEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            BeerOrder beerOrderCurrentState = repository.findById(id).orElseThrow();
            sender.sendEvent(beerOrderCurrentState, BeerOrderEventEnum.ALLOCATE_ORDER);
        } else {
            sender.sendEvent(
                    beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }
    }

    @Transactional
    @Override
    public void processAllocationResult(BeerOrderDto beerOrderDto, Boolean isAllocationError, Boolean isPendingInventory) {
        BeerOrder beerOrder = repository.findById(beerOrderDto.getId()).orElseThrow();
        if (isAllocationError) {
            sender.sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        } else if (isPendingInventory) {
            sender.sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
        } else {
            sender.sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
        }

        updateAllocatedQty(beerOrderDto, beerOrder);
    }

    @Override
    public void proccessPickUp(UUID id) {

    }

    @Override
    public void cancelOrder(UUID id) {

    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
        BeerOrder allocatedOrder = repository.findById(beerOrderDto.getId()).orElseThrow();

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
}
