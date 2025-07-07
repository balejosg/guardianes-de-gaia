package com.guardianes.walking.infrastructure.repository;

import com.guardianes.walking.domain.EnergyRepository;
import com.guardianes.walking.domain.EnergyTransaction;
import com.guardianes.walking.domain.EnergyTransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EnergyTransactionPersistenceTest {

    @Autowired
    private EnergyRepository energyRepository;

    @Test
    public void shouldPersistEnergyTransactionToRepository() {
        // Given
        Long guardianId = 1L;
        EnergyTransactionType type = EnergyTransactionType.EARNED;
        int amount = 100;
        String source = "Steps";
        LocalDateTime timestamp = LocalDateTime.now();
        EnergyTransaction transaction = new EnergyTransaction(guardianId, type, amount, source, timestamp);

        // When
        EnergyTransaction savedTransaction = energyRepository.saveTransaction(transaction);

        // Then
        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.getGuardianId()).isEqualTo(guardianId);
        assertThat(savedTransaction.getType()).isEqualTo(type);
        assertThat(savedTransaction.getAmount()).isEqualTo(amount);
        assertThat(savedTransaction.getSource()).isEqualTo(source);
        assertThat(savedTransaction.getTimestamp()).isEqualTo(timestamp);

        // Verify persistence by retrieving from repository
        List<EnergyTransaction> retrievedTransactions = energyRepository.findTransactionsByGuardianId(guardianId);
        assertThat(retrievedTransactions).hasSize(1);
        assertThat(retrievedTransactions.get(0)).isEqualTo(transaction);
    }

    @Test
    public void shouldCalculateCorrectEnergyBalance() {
        // Given
        Long guardianId = 2L;
        
        EnergyTransaction earnedTransaction1 = new EnergyTransaction(
            guardianId, EnergyTransactionType.EARNED, 150, "Steps", LocalDateTime.now().minusHours(2)
        );
        EnergyTransaction earnedTransaction2 = new EnergyTransaction(
            guardianId, EnergyTransactionType.EARNED, 50, "Challenge", LocalDateTime.now().minusHours(1)
        );
        EnergyTransaction spentTransaction = new EnergyTransaction(
            guardianId, EnergyTransactionType.SPENT, 75, "Battle", LocalDateTime.now()
        );

        // When
        energyRepository.saveTransaction(earnedTransaction1);
        energyRepository.saveTransaction(earnedTransaction2);
        energyRepository.saveTransaction(spentTransaction);

        // Then
        int balance = energyRepository.getEnergyBalance(guardianId);
        assertThat(balance).isEqualTo(125); // 150 + 50 - 75 = 125
        
        List<EnergyTransaction> allTransactions = energyRepository.findTransactionsByGuardianId(guardianId);
        assertThat(allTransactions).hasSize(3);
    }
}