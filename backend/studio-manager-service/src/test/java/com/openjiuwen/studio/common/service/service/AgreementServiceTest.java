package com.openjiuwen.studio.common.service.service;

import com.openjiuwen.studio.common.service.entity.AgreementEntity;
import com.openjiuwen.studio.common.service.repository.AgreementRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgreementServiceTest {

    @Mock
    private AgreementRepository agreementRepository;

    @InjectMocks
    private AgreementService agreementService;

    @Test
    void testCreateAgreement() {
        AgreementEntity entity = new AgreementEntity();
        entity.setId("id-1");
        entity.setDomainId("domain-1");

        agreementService.createAgreement(entity);

        verify(agreementRepository).insert(entity);
    }

    @Test
    void testUpdateAgreement() {
        AgreementEntity entity = new AgreementEntity();
        entity.setId("id-1");
        entity.setAgreeFlag("Y");

        agreementService.updateAgreement(entity);

        verify(agreementRepository).update(entity);
    }

    @Test
    void testSelectByDomainId() {
        AgreementEntity entity = new AgreementEntity();
        entity.setDomainId("domain-1");
        when(agreementRepository.selectByDomainId("domain-1")).thenReturn(entity);

        AgreementEntity result = agreementService.selectByDomainId("domain-1");

        assertNotNull(result);
        assertEquals("domain-1", result.getDomainId());
    }

    @Test
    void testSelectByDomainId_NotFound() {
        when(agreementRepository.selectByDomainId("unknown")).thenReturn(null);

        AgreementEntity result = agreementService.selectByDomainId("unknown");

        assertNull(result);
    }

    @Test
    void testQueryList() {
        AgreementEntity entity1 = new AgreementEntity();
        entity1.setDomainId("domain-1");
        AgreementEntity entity2 = new AgreementEntity();
        entity2.setDomainId("domain-1");
        when(agreementRepository.queryList("domain-1")).thenReturn(List.of(entity1, entity2));

        List<AgreementEntity> result = agreementService.queryList("domain-1");

        assertEquals(2, result.size());
    }

    @Test
    void testQueryList_Empty() {
        when(agreementRepository.queryList("domain-1")).thenReturn(List.of());

        List<AgreementEntity> result = agreementService.queryList("domain-1");

        assertTrue(result.isEmpty());
    }
}
