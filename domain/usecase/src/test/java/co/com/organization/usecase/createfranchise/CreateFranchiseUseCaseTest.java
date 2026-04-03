package co.com.organization.usecase.createfranchise;

import co.com.organization.model.franchise.Franchise;
import co.com.organization.model.franchise.gateways.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFranchiseUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private CreateFranchiseUseCase createFranchiseUseCase;

    private Mono<Franchise> franchiseMono;

    @BeforeEach
    void setUp() {
        Franchise franchise = Franchise.builder()
                .id("franchise-001")
                .name("Franchise Test")
                .build();
        franchiseMono = Mono.just(franchise);
    }

    // ==================== HAPPY PATH ====================

    @Test
    void executeWhenFranchiseIsValidShouldReturnCreatedId() {
        String response = "Franchise created successfully";

        when(franchiseRepository.createFranchise(franchiseMono))
                .thenReturn(Mono.just(response));

        StepVerifier.create(createFranchiseUseCase.execute(franchiseMono))
                .expectNext(response)
                .verifyComplete();

        verify(franchiseRepository, times(1)).createFranchise(franchiseMono);
    }

    // ==================== ERROR PATH ====================

    @Test
    void executeWhenRepositoryThrowsExceptionShouldPropagateError() {

        RuntimeException repositoryException = new RuntimeException("Database connection error");

        when(franchiseRepository.createFranchise(franchiseMono))
                .thenReturn(Mono.error(repositoryException));

        StepVerifier.create(createFranchiseUseCase.execute(franchiseMono))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Database connection error"))
                .verify();

        verify(franchiseRepository, times(1)).createFranchise(franchiseMono);
    }

    @Test
    void executeWhenFranchiseMonoHasErrorShouldPropagateError() {

        IllegalArgumentException inputException = new IllegalArgumentException("Invalid franchise data");
        Mono<Franchise> errorMono = Mono.error(inputException);

        when(franchiseRepository.createFranchise(any()))
                .thenReturn(Mono.error(inputException));

        StepVerifier.create(createFranchiseUseCase.execute(errorMono))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals("Invalid franchise data"))
                .verify();

        verify(franchiseRepository, times(1)).createFranchise(any());
    }

    // ==================== EMPTY PATH ====================

    @Test
    void execute_whenRepositoryReturnsEmpty_shouldCompleteWithoutValue() {

        when(franchiseRepository.createFranchise(franchiseMono))
                .thenReturn(Mono.empty());

        StepVerifier.create(createFranchiseUseCase.execute(franchiseMono))
                .verifyComplete();

        verify(franchiseRepository, times(1)).createFranchise(franchiseMono);
    }

    @Test
    void execute_whenFranchiseMonoIsEmpty_shouldCompleteWithoutValue() {

        Mono<Franchise> emptyMono = Mono.empty();

        when(franchiseRepository.createFranchise(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(createFranchiseUseCase.execute(emptyMono))
                .verifyComplete();

        verify(franchiseRepository, times(1)).createFranchise(any());
    }
}