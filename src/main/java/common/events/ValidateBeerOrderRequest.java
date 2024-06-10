package common.events;


import common.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateBeerOrderRequest implements Serializable {

    private static final long serialVersionUID = 5452820620509472318L;

    private BeerOrderDto beerOrder;
}
