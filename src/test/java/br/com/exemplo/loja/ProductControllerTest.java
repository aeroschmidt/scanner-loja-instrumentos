package br.com.exemplo.loja;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSeededProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name").value("Cabo P10 de 5 m"));
    }

    @Test
    void createsUpdatesAndDeletesProduct() throws Exception {
        String product = """
                {"name":"Baixo elétrico","category":"Cordas","price":1299.90,"stock":3}
                """;
        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(product))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Baixo elétrico"))
                .andReturn().getResponse().getContentAsString();

        Number generatedId = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        long id = generatedId.longValue();
        String update = """
                {"name":"Baixo elétrico 5 cordas","category":"Cordas","price":1499.90,"stock":2}
                """;
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(2));

        mockMvc.perform(delete("/api/products/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsNegativeStock() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" 
                                {"name":"Item","category":"Teste","price":20.00,"stock":-1}
                                """))
                .andExpect(status().isBadRequest());
    }
}
