package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.beerservice.BeerService;
import common.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public abstract class BeerOrderLineDecorator implements BeerOrderLineMapper{

    private BeerService beerService;
    private BeerOrderLineMapper beerOrderLineMapper;

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Autowired
    @Qualifier("delegate")
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto result = beerOrderLineMapper.beerOrderLineToDto(line);
        beerService.getBeerDtoByUpc(line.getUpc()).ifPresent(beerDto -> {
            result.setPrice(beerDto.getPrice());
            result.setBeerStyle(beerDto.getBeerStyle());
            result.setBeerName(beerDto.getBeerName());
            result.setBeerId(beerDto.getId());
        });
        return result;
    }
}
