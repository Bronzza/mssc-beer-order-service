package guru.sfg.beer.order.service.web.controllers;

import common.model.CustomerDto;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.web.mappers.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/customers")
@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable("customerId") UUID customerId){
        return ResponseEntity.status(customerRepository.findById(customerId).isPresent() ? HttpStatus.FOUND: HttpStatus.NOT_FOUND)
                .body(customerMapper.customerToDto(customerRepository.findById(customerId).orElse(new Customer())));
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getCustomers(){
        List<Customer> result = customerRepository.findAll();
        return ResponseEntity.status(result.isEmpty() ? HttpStatus.FOUND: HttpStatus.NOT_FOUND)
                .body(result.stream().map(customerMapper::customerToDto).collect(Collectors.toList()));
    }

}
