package guru.sfg.beer.order.service.web.mappers;

import common.model.BeerOrderLineDto;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-06-10T18:22:53+0700",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
@Primary
public class BeerOrderLineMapperImpl extends BeerOrderLineDecorator implements BeerOrderLineMapper {

    @Autowired
    @Qualifier("delegate")
    private BeerOrderLineMapper delegate;

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto)  {
        return delegate.dtoToBeerOrderLine( dto );
    }
}
