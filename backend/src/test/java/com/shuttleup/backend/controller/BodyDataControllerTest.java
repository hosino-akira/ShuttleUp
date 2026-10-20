package com.shuttleup.backend.controller;

import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.entity.UserStatus;
import com.shuttleup.backend.repository.UserRepository;
import com.shuttleup.backend.service.AuthService;
import com.shuttleup.backend.service.JwtTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** MVC validation + real service; repository is isolated from the development DB. */
class BodyDataControllerTest {
    private final UserRepository users = mock(UserRepository.class);
    private final User owner = User.builder().id(10L).name("Owner").email("owner@example.com")
            .status(UserStatus.ACTIVE).build();
    private final User other = User.builder().id(20L).name("Other").email("other@example.com")
            .status(UserStatus.ACTIVE).bodyWeightKg(new BigDecimal("80")).build();
    private MockMvc mvc;

    @BeforeEach void setup() {
        when(users.findById(10L)).thenReturn(Optional.of(owner));
        when(users.findById(20L)).thenReturn(Optional.of(other));
        when(users.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new AuthService(users, mock(PasswordEncoder.class));
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(service, mock(JwtTokenService.class)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        signIn(10L);
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    private void signIn(long id) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(Long.toString(id), "", List.of()));
    }

    @Test void newAccountHasEmptyBodyData() throws Exception {
        mvc.perform(get("/api/auth/me/body-data")).andExpect(status().isOk())
                .andExpect(jsonPath("$.heightCm").isEmpty())
                .andExpect(jsonPath("$.bodyWeightKg").isEmpty())
                .andExpect(jsonPath("$.bodyFatPercentage").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:5173", "http://127.0.0.1:5173"})
    void bothDevelopmentOriginsCanSaveBodyData(String origin) throws Exception {
        mvc.perform(put("/api/auth/me/body-data").header("Origin", origin)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", origin));
    }

    @Test void unrelatedOriginIsRejected() throws Exception {
        mvc.perform(put("/api/auth/me/body-data").header("Origin", "https://unrelated.example")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test void savesAndReadsCurrentValuesWithoutChangingAccount() throws Exception {
        mvc.perform(put("/api/auth/me/body-data").contentType(MediaType.APPLICATION_JSON)
                .content("{\"heightCm\":175.5,\"bodyWeightKg\":68.25,\"bodyFatPercentage\":18.5}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bodyWeightKg").value(68.25));
        mvc.perform(get("/api/auth/me/body-data")).andExpect(status().isOk())
                .andExpect(jsonPath("$.heightCm").value(175.5))
                .andExpect(jsonPath("$.bodyFatPercentage").value(18.5));
        assertEquals("Owner", owner.getName());
        assertEquals("owner@example.com", owner.getEmail());
        assertNotNull(owner.getUpdatedAt());
    }

    @Test void canClearOneFieldAndThenAllFields() throws Exception {
        owner.setHeightCm(new BigDecimal("175"));
        owner.setBodyWeightKg(new BigDecimal("70"));
        owner.setBodyFatPercentage(new BigDecimal("20"));
        mvc.perform(put("/api/auth/me/body-data").contentType(MediaType.APPLICATION_JSON)
                .content("{\"heightCm\":175,\"bodyWeightKg\":null,\"bodyFatPercentage\":20}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bodyWeightKg").isEmpty())
                .andExpect(jsonPath("$.heightCm").value(175));
        mvc.perform(put("/api/auth/me/body-data").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.heightCm").isEmpty())
                .andExpect(jsonPath("$.bodyFatPercentage").isEmpty());
        assertNull(owner.getBodyWeightKg());
    }

    @Test void accountSavePreservesBodyDataAndDoesNotExposeIt() throws Exception {
        owner.setBodyWeightKg(new BigDecimal("70"));
        mvc.perform(put("/api/auth/me").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New name\",\"email\":\"new@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bodyWeightKg").doesNotExist());
        assertEquals(new BigDecimal("70"), owner.getBodyWeightKg());
    }

    @Test void usesAuthenticatedOwnerDespiteUserIdParameter() throws Exception {
        mvc.perform(put("/api/auth/me/body-data").param("userId", "20")
                .contentType(MediaType.APPLICATION_JSON).content("{\"bodyWeightKg\":65}"))
                .andExpect(status().isOk());
        assertEquals(new BigDecimal("65"), owner.getBodyWeightKg());
        assertEquals(new BigDecimal("80"), other.getBodyWeightKg());
        signIn(20L);
        mvc.perform(get("/api/auth/me/body-data").param("userId", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bodyWeightKg").value(80));
    }

    @Test void unauthenticatedReadAndWriteAreRejected() throws Exception {
        SecurityContextHolder.clearContext();
        mvc.perform(get("/api/auth/me/body-data")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/auth/me/body-data").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(users);
    }

    @Test void disabledAccountCannotAccessBodyData() throws Exception {
        owner.setStatus(UserStatus.DISABLED);
        mvc.perform(get("/api/auth/me/body-data")).andExpect(status().isForbidden());
        mvc.perform(put("/api/auth/me/body-data").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verify(users, never()).saveAndFlush(any());
    }

    @Test void acceptsOptionalFieldsAndBoundaryValues() throws Exception {
        mvc.perform(put("/api/auth/me/body-data").contentType(MediaType.APPLICATION_JSON)
                .content("{\"heightCm\":0.01,\"bodyWeightKg\":999.99,\"bodyFatPercentage\":0}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/auth/me/body-data").contentType(MediaType.APPLICATION_JSON)
                .content("{\"bodyFatPercentage\":100}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.heightCm").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"heightCm\":0}", "{\"heightCm\":-1}", "{\"heightCm\":1000}", "{\"heightCm\":175.555}",
            "{\"bodyWeightKg\":0}", "{\"bodyWeightKg\":-1}", "{\"bodyWeightKg\":1000}", "{\"bodyWeightKg\":70.123}",
            "{\"bodyFatPercentage\":-1}", "{\"bodyFatPercentage\":100.01}", "{\"bodyFatPercentage\":18.123}"
    })
    void invalidValuesNeverReachPersistence(String json) throws Exception {
        mvc.perform(put("/api/auth/me/body-data").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isNotEmpty());
        verifyNoInteractions(users);
    }
}
