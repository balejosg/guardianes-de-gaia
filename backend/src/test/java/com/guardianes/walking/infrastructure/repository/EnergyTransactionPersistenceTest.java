package com.guardianes.walking.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.testconfig.NoRedisTestConfiguration;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergySource;
import com.guardianes.walking.domain.model.EnergyTransaction;
import com.guardianes.walking.domain.model.EnergyTransactionType;
import com.guardianes.walking.domain.repository.EnergyRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(NoRedisTestConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EnergyTransactionPersistenceTest {

    @Autowired private EnergyRepository energyRepository;

    @Test
    public void shouldPersistEnergyTransactionToRepository() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        Energy amount = Energy.of(100);
        EnergySource source = EnergySource.steps();
        EnergyTransaction transaction = EnergyTransaction.earned(guardianId, amount, source);

        // When
        EnergyTransaction savedTransaction = energyRepository.saveTransaction(transaction);

        // Then
        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.getGuardianId()).isEqualTo(guardianId);
        assertThat(savedTransaction.getType()).isEqualTo(EnergyTransactionType.EARNED);
        assertThat(savedTransaction.getAmount()).isEqualTo(amount);
        assertThat(savedTransaction.getSource()).isEqualTo(source);

        // Verify persistence by retrieving from repository
        List<EnergyTransaction> retrievedTransactions =
                energyRepository.findTransactionsByGuardianId(guardianId);
        assertThat(retrievedTransactions).hasSize(1);
        assertThat(retrievedTransactions.get(0).getGuardianId()).isEqualTo(guardianId);
    }

    @Test
    public void shouldCalculateCorrectEnergyBalance() {
        // Given
        GuardianId guardianId = GuardianId.of(2L);

        EnergyTransaction earnedTransaction1 =
                EnergyTransaction.earned(guardianId, Energy.of(150), EnergySource.steps());
        EnergyTransaction earnedTransaction2 =
                EnergyTransaction.earned(guardianId, Energy.of(50), EnergySource.of("CHALLENGE"));
        EnergyTransaction spentTransaction =
                EnergyTransaction.spent(guardianId, Energy.of(75), EnergySource.of("BATTLE"));

        // When
        energyRepository.saveTransaction(earnedTransaction1);
        energyRepository.saveTransaction(earnedTransaction2);
        energyRepository.saveTransaction(spentTransaction);

        // Then
        Energy balance = energyRepository.getEnergyBalance(guardianId);
        assertThat(balance.amount()).isEqualTo(125); // 150 + 50 - 75 = 125

        List<EnergyTransaction> allTransactions =
                energyRepository.findTransactionsByGuardianId(guardianId);
        assertThat(allTransactions).hasSize(3);
    }
}
