package guru.sfg.beer.order.service.config;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    public static final String VALIDATION_BEER_ORDER_REQUEST = "validate-order";
    public static final String VALIDATION_BEER_ORDER_RESPONSE = "validate-order-result";
    public static final String ALLOCATION_BEER_ORDER_REQUEST = "allocate-order";
    public static final String ALLOCATION_BEER_ORDER_RESPONSE = "allocate-order-result";
    public static final String ALLOCATION_FAILURE = "allocate-failure";
    public static final String PICK_UP_BEER_ORDER_RESPONSE = "pickup-order";
    public static final String PICK_UP_BEER_ORDER_REQUEST = "pickup-order-result";
    public static final String DE_ALLOCATION_BEER_ORDER_REQUEST = "deallocate-order";

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);

        return converter;
    }
}
