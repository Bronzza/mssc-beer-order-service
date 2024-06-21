package common.events;

import common.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocationFailureResponse implements Serializable {

    private static final long serialVersionUID = 4960961462586666450L;
    private UUID orderId;
}
