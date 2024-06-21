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

    public static final String UPC = "12345";
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
        setUpMockServer();
        BeerOrder beerOrder = createBeerOrder();
        BeerOrder saved = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();
//            assertEquals(BeerOrderStatusEnum.VALIDATION_PENDING, found.getOrderStatus());
            assertEquals(BeerOrderStatusEnum.ALLOCATED, found.getOrderStatus());
        });

        BeerOrder result = repository.findById(beerOrder.getId()).get();
        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, found.getOrderStatus());
        });

        BeerOrder allocated = repository.findById(beerOrder.getId()).get();
        assertNotNull(allocated);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, allocated.getOrderStatus());


    }

    @Test
    void testForPickUp() throws JsonProcessingException {
        BeerOrder beerOrder = createBeerOrder();
        setUpMockServer();

        BeerOrder saved = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, found.getOrderStatus());
        });

        beerOrderManager.proccessPickUp(saved.getId());

        BeerOrder allocated = repository.findById(beerOrder.getId()).get();


        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.PICKED_UP, found.getOrderStatus());
        });

        assertNotNull(allocated);
        assertEquals(BeerOrderStatusEnum.PICKED_UP, allocated.getOrderStatus());
    }

    @Test
    public void testValidationFailed() throws JsonProcessingException {
        setUpMockServer();
        BeerOrder expectToFailOrder = createBeerOrder();
        expectToFailOrder.setCustomerRef("Invalid customer");
        BeerOrder beerOrder = beerOrderManager.newBeerOrder(expectToFailOrder);

        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, found.getOrderStatus());
        });
        BeerOrder result = repository.findById(beerOrder.getId()).get();
        assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, result.getOrderStatus());
    }

    @Test
    public void testAllocationFailed() throws JsonProcessingException {
        setUpMockServer();
        BeerOrder expectToFailOrder = createBeerOrder();
        expectToFailOrder.setCustomerRef("Allocation-failed customer");
        BeerOrder beerOrder = beerOrderManager.newBeerOrder(expectToFailOrder);

        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, found.getOrderStatus());
        });
        BeerOrder result = repository.findById(beerOrder.getId()).get();
        assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, result.getOrderStatus());
    }

    @Test
    public void testAllocationInvenotryPending() throws JsonProcessingException {
        setUpMockServer();
        BeerOrder expectToFailOrder = createBeerOrder();
        expectToFailOrder.setCustomerRef("Allocation-pending customer");
        BeerOrder beerOrder = beerOrderManager.newBeerOrder(expectToFailOrder);

        await().untilAsserted(() -> {
            BeerOrder found = repository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATION_PENDING, found.getOrderStatus());
        });
        BeerOrder result = repository.findById(beerOrder.getId()).get();
        assertEquals(BeerOrderStatusEnum.ALLOCATION_PENDING, result.getOrderStatus());
    }

    private void setUpMockServer() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder()
                .id(beerId)
                .upc(UPC)
                .build();

        mockServer.stubFor(get(BeerServiceImpl.UPC_PATH + UPC)
                .willReturn(okJson(objMapper.writeValueAsString(beerDto))));
    }



    public BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine
                .builder()
                .beerId(beerId)
                .upc(UPC)
                .orderQuantity(1)
                .build());
        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}
