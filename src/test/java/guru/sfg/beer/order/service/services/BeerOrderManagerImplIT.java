package guru.sfg.beer.order.service.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.enums.BeerOrderStatusEnum;
import common.model.BeerDto;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.services.BeerOrderManagerLoseCouplingImpl;
import guru.sfg.beer.order.service.services.beerservice.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerImplIT {

    @Autowired
    BeerOrderManagerImpl beerOrderManager;

    @Autowired
    BeerOrderRepository repository;

    @Autowired
    ObjectMapper objMapper;

    @Autowired
    WireMockServer mockServer;

    @Autowired
    CustomerRepository customerRepository;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilder {

        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = with(WireMockConfiguration.wireMockConfig().port(8083));
            server.start();
            return server;
        }

    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
                .customerName("Test customer")
                .build());
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder()
                .id(beerId)
                .upc("12345")
                .build();


        mockServer.stubFor(get(BeerServiceImpl.UPC_PATH + "12345")
                .willReturn(okJson(objMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder saved = beerOrderManager.newBeerOrder(beerOrder);
        System.out.println("Test__0");

        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();

//            assertEquals(BeerOrderStatusEnum.VALIDATION_PENDING, found.getOrderStatus());
            assertEquals(BeerOrderStatusEnum.ALLOCATED, found.getOrderStatus());
        });
        System.out.println("Test__1");

        BeerOrder result = repository.findById(beerOrder.getId()).get();

        assertNotNull(result);
//        assertEquals(BeerOrderStatusEnum.VALIDATION_PENDING, result.getOrderStatus());

        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();
            BeerOrderLine next = found.getBeerOrderLines().iterator().next();
//            assertEquals(BeerOrderStatusEnum.ALLOCATED, found.getOrderStatus());
            assertEquals(next.getQuantityAllocated(), next.getOrderQuantity());
        });
        System.out.println("Test__2");

        BeerOrder allocated = repository.findById(beerOrder.getId()).get();

        assertNotNull(allocated);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, allocated.getOrderStatus());


    }

    public BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine
                .builder()
                .beerId(beerId)
                .upc("12345")
                .orderQuantity(1)
                .build());
        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}
