package common.events;

import common.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeAllocationBeerOrderRequest implements Serializable {

    private static final long serialVersionUID = 8097660553172316720L;
    private BeerOrderDto beerOrder;
}
