package com.example.service;

import com.example.dto.FakeAPIResponse;
import com.example.entity.Customer;
import com.example.entity.Person;
import com.example.repository.CustomerRepository;
import com.example.utils.Config;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Scope("singleton")
public class CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Config config;

    @PostConstruct
    private void postConstructExample(){
        log.info("postConstructExample called");
    }

    @PreDestroy
    private void preDestroyExample(){
        log.info("preDestroyExample called");
    }

    @Cacheable("customers")
    public List<Customer> getAllCustomers(){
        log.info("inside getAllCustomers");

        log.info("from config => "+ config.getJsonPlaceholderUrl() + config.getBaseUrl() + config.getPath());

        var customers = customerRepository.findAll();
        log.info("customers => {}", customers);
        return customers;
    }

    public Optional<FakeAPIResponse> callApi() {

        Person person = new Person("Aniruddha", "Pune", 30);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);

        try{
            ResponseEntity<FakeAPIResponse> fakeAPIResponse = restTemplate.exchange(
                    config.getJsonPlaceholderUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    FakeAPIResponse.class
            );

            return Optional.ofNullable(fakeAPIResponse.getBody());
        }
        catch (Exception e) {
            log.info(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Customer> getCustomer(String id) {
        log.info("inside getCustomer");
        return customerRepository.findById(id);
    }

    @CacheEvict(value = "customers", allEntries = true)
    public Customer createCustomer(Customer customer) {
        log.info("inside createCustomer");
        return customerRepository.save(customer);
    }

    /**
     * Partial update: only the non-null fields of {@code changes} are applied.
     * Returns an empty Optional when no customer exists with the given id.
     */
    @CacheEvict(value = "customers", allEntries = true)
    public Optional<Customer> updateCustomer(String id, Customer changes) {
        log.info("inside updateCustomer");
        return customerRepository.findById(id).map(existing -> {
            if (changes.getFirstName() != null) {
                existing.setFirstName(changes.getFirstName());
            }
            if (changes.getLastName() != null) {
                existing.setLastName(changes.getLastName());
            }
            return customerRepository.save(existing);
        });
    }

    /** Returns {@code false} when no customer exists with the given id. */
    @CacheEvict(value = "customers", allEntries = true)
    public boolean deleteCustomer(String id) {
        log.info("inside deleteCustomer");
        if (!customerRepository.existsById(id)) {
            return false;
        }
        customerRepository.deleteById(id);
        return true;
    }

    @Async
    public CompletableFuture<String> asyncTest() {
        return CompletableFuture.completedFuture("");
    }
}
