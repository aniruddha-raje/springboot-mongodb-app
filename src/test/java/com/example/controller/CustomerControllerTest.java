package com.example.controller;

import com.example.entity.Customer;
import com.example.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer unit tests for {@link CustomerController}. The {@link CustomerService}
 * is mocked with {@link MockBean}, so no MongoDB is involved and the write
 * operations (POST / PATCH / DELETE) never touch a real repository.
 */
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private Customer newCustomer(String id, String firstName, String lastName) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        return customer;
    }

    // ---------------------------------------------------------------------
    // GET /customer/all
    // ---------------------------------------------------------------------

    @Test
    void getAllCustomers_returnsCustomerList() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(
                newCustomer("1", "John", "Doe"),
                newCustomer("2", "Jane", "Smith")));

        mockMvc.perform(get("/customer/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].lastName").value("Smith"));

        verify(customerService, times(1)).getAllCustomers();
    }

    // ---------------------------------------------------------------------
    // GET /customer/hello-world
    // ---------------------------------------------------------------------

    @Test
    void sayHello_usesProvidedName() throws Exception {
        mockMvc.perform(get("/customer/hello-world").param("name", "Aniruddha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Aniruddha!"));
    }

    // ---------------------------------------------------------------------
    // GET /customer/id/{id}
    // ---------------------------------------------------------------------

    @Test
    void getCustomerById_whenFound_returnsCustomer() throws Exception {
        when(customerService.getCustomer("1")).thenReturn(Optional.of(newCustomer("1", "John", "Doe")));

        mockMvc.perform(get("/customer/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(customerService).getCustomer("1");
    }

    @Test
    void getCustomerById_whenMissing_returns404() throws Exception {
        when(customerService.getCustomer("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/customer/id/999"))
                .andExpect(status().isNotFound());

        verify(customerService).getCustomer("999");
    }

    // ---------------------------------------------------------------------
    // POST /customer  (write endpoint - service mocked)
    // ---------------------------------------------------------------------

    @Test
    void createCustomer_returns201AndBody() throws Exception {
        Customer request = newCustomer(null, "New", "Customer");
        Customer saved = newCustomer("42", "New", "Customer");
        when(customerService.createCustomer(any(Customer.class))).thenReturn(saved);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("42"))
                .andExpect(jsonPath("$.firstName").value("New"));

        verify(customerService, times(1)).createCustomer(any(Customer.class));
    }

    // ---------------------------------------------------------------------
    // PATCH /customer/id/{id}  (write endpoint - service mocked)
    // ---------------------------------------------------------------------

    @Test
    void updateCustomer_whenFound_returns200AndUpdatedBody() throws Exception {
        Customer changes = new Customer();
        changes.setLastName("Updated");
        Customer updated = newCustomer("1", "John", "Updated");
        when(customerService.updateCustomer(eq("1"), any(Customer.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(patch("/customer/id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"));

        verify(customerService).updateCustomer(eq("1"), any(Customer.class));
    }

    @Test
    void updateCustomer_whenMissing_returns404() throws Exception {
        Customer changes = new Customer();
        changes.setLastName("Updated");
        when(customerService.updateCustomer(eq("999"), any(Customer.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/customer/id/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changes)))
                .andExpect(status().isNotFound());

        verify(customerService).updateCustomer(eq("999"), any(Customer.class));
    }

    // ---------------------------------------------------------------------
    // DELETE /customer/id/{id}  (write endpoint - service mocked)
    // ---------------------------------------------------------------------

    @Test
    void deleteCustomer_whenFound_returns204() throws Exception {
        when(customerService.deleteCustomer("1")).thenReturn(true);

        mockMvc.perform(delete("/customer/id/1"))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer("1");
    }

    @Test
    void deleteCustomer_whenMissing_returns404() throws Exception {
        when(customerService.deleteCustomer("999")).thenReturn(false);

        mockMvc.perform(delete("/customer/id/999"))
                .andExpect(status().isNotFound());

        verify(customerService).deleteCustomer("999");
    }

    // ---------------------------------------------------------------------
    // A read must never trigger a write on the (mocked) service.
    // ---------------------------------------------------------------------

    @Test
    void getAllCustomers_doesNotInvokeAnyWriteOperation() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/customer/all"))
                .andExpect(status().isOk());

        verify(customerService, never()).createCustomer(any());
        verify(customerService, never()).updateCustomer(any(), any());
        verify(customerService, never()).deleteCustomer(any());
    }
}
