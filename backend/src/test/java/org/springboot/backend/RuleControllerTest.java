package org.springboot.backend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springboot.backend.controller.RuleController;
import org.springboot.backend.service.RuleImportService;
import org.springboot.backend.service.RuleRetrievalService;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RuleControllerTest {

    @Test
    void importRunsThroughController() throws Exception {
        RuleImportService importer = mock(RuleImportService.class);
        when(importer.importIfEmpty()).thenReturn(new RuleImportService.ImportResult(0, 106, false));
        var controller = new RuleController(
                mock(RuleRetrievalService.class), importer);
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/v1/rules/imports:run"))
                .andExpect(status().isOk());
        verify(importer).importIfEmpty();
    }
}
