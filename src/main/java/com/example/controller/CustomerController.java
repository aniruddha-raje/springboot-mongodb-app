package com.example.controller;

import com.example.dto.FakeAPIResponse;
import com.example.dto.Greeting;
import com.example.entity.Customer;
import com.example.exception.BadRequestException;
import com.example.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Tag(name = "Customer", description = "Customer CRUD backed by MongoDB")
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    private static final System.Logger logger = System.getLogger(CustomerController.class.getName());

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Operation(summary = "List all customers")
    @GetMapping("/all")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        System.out.println("getAllCustomers called");
        return new ResponseEntity<>(customerService.getAllCustomers(), HttpStatus.OK);
    }

    @Operation(summary = "Greet by name")
    @GetMapping("/hello-world")
    public ResponseEntity<Greeting> sayHello(@RequestParam(name="name", required=false, defaultValue="Stranger") String name) {
        return new ResponseEntity<>(new Greeting(counter.incrementAndGet(), String.format(template, name)), HttpStatus.OK);
    }

    @Operation(summary = "Call external API via RestTemplate")
    @GetMapping("/rest-template")
    public ResponseEntity<FakeAPIResponse> callApi() {
        return new ResponseEntity<>(customerService.callApi().get(), HttpStatus.OK);
    }

    @Operation(summary = "Trigger a sample 400 error")
    @GetMapping("/throw-exception")
    public ResponseEntity<String> globalExceptionTest() {
        throw new BadRequestException("Missing required parameter: id");
    }

    @Operation(summary = "Get a customer by id")
    @GetMapping("/id/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String customerId) {
        Customer customer = customerService.getCustomer(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found with id " + customerId));
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @Operation(summary = "Create a customer")
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer created = customerService.createCustomer(customer);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a customer (partial)")
    @PatchMapping("/id/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable String customerId,
                                                   @RequestBody Customer changes) {
        Customer updated = customerService.updateCustomer(customerId, changes)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found with id " + customerId));
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @Operation(summary = "Delete a customer")
    @DeleteMapping("/id/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        if (!customerService.deleteCustomer(customerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Customer not found with id " + customerId);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
