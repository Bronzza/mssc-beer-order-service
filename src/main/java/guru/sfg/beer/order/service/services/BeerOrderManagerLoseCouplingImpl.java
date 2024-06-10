package guru.sfg.beer.order.service.services;

import common.enums.BeerOrderEventEnum;
import common.enums.BeerOrderStatusEnum;
import common.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.senders.BeerOrderStateMachineEventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
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

    @Override
    public void processValidationResult(UUID id, Boolean isValid) {
        BeerOrder beerOrder = repository.findOneById(id);
        sender.sendEvent(
                BeerOrder.builder()
                        .id(id)
                        .orderStatus(isValid ? BeerOrderStatusEnum.VALIDATED : BeerOrderStatusEnum.VALIDATION_EXCEPTION)
                        .build(),
//                beerOrder,
                isValid ? BeerOrderEventEnum.VALIDATION_PASSED : BeerOrderEventEnum.VALIDATION_FAILED);

        BeerOrder beerOrderCurrentState = repository.findOneById(id);
        sender.sendEvent(beerOrderCurrentState, BeerOrderEventEnum.ALLOCATE_ORDER);
    }

    @Transactional
    @Override
    public void processAllocationResult(BeerOrderDto beerOrderDto, Boolean isAllocationError, Boolean isPendingInventory) {
        BeerOrder beerOrder = repository.findOneById(beerOrderDto.getId());
        if (isAllocationError) {
            sender.sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
            return;
        } else if (isPendingInventory) {
            sender.sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
        } else {
            sender.sendEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
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
}
