package common.events;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateBeerOrderResponse implements Serializable {

    private static final long serialVersionUID = -3996005047391807951L;
    private UUID orderId;
    private Boolean isValid;
}
