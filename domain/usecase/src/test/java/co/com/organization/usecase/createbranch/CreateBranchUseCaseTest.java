package co.com.organization.usecase.createbranch;

import co.com.organization.model.branch.Branch;
import co.com.organization.model.branch.gateways.BranchRepository;
import co.com.organization.model.franchise.gateways.FranchiseRepository;
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
class CreateBranchUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private CreateBranchUseCase createBranchUseCase;

    private static final String FRANCHISE_ID = "franchise-001";
    private static final String BRANCH_ID    = "branch-001";
    private static final String BRANCH_NAME  = "Branch Test";

    private Branch buildBranch() {
        return Branch.builder()
                .id(BRANCH_ID)
                .name(BRANCH_NAME)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  Happy path
    // ------------------------------------------------------------------ //

    @Test
    void createBranchShouldSucceedWhenBranchIsCreatedAndAddedToFranchise() {
        Branch branch = buildBranch();
        Mono<Branch> branchMono = Mono.just(branch);

        when(branchRepository.createBranch(any())).thenReturn(Mono.just(Boolean.TRUE));
        when(franchiseRepository.addBranchToFranchise(FRANCHISE_ID, BRANCH_ID))
                .thenReturn(Mono.just(FRANCHISE_ID));

        StepVerifier.create(createBranchUseCase.createBranch(branchMono, FRANCHISE_ID))
                .expectNext(FRANCHISE_ID)
                .verifyComplete();

        verify(branchRepository, times(1)).createBranch(any());
        verify(franchiseRepository, times(1)).addBranchToFranchise(FRANCHISE_ID, BRANCH_ID);
        verify(branchRepository, never()).delete(any());
    }

    // ------------------------------------------------------------------ //
    //  Error en addBranchToFranchise → rollback (delete) + error propagado
    // ------------------------------------------------------------------ //

    @Test
    void createBranchShouldDeleteBranchAndPropagateErrorWhenAddBranchToFranchiseFails() {
        Branch branch = buildBranch();
        Mono<Branch> branchMono = Mono.just(branch);
        RuntimeException error = new RuntimeException("Franchise not found");

        when(branchRepository.createBranch(any())).thenReturn(Mono.just(Boolean.TRUE));
        when(franchiseRepository.addBranchToFranchise(FRANCHISE_ID, BRANCH_ID))
                .thenReturn(Mono.error(error));
        when(branchRepository.delete(BRANCH_ID)).thenReturn(Mono.empty());

        StepVerifier.create(createBranchUseCase.createBranch(branchMono, FRANCHISE_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("Franchise not found"))
                .verify();

        verify(branchRepository, times(1)).createBranch(any());
        verify(franchiseRepository, times(1)).addBranchToFranchise(FRANCHISE_ID, BRANCH_ID);
        verify(branchRepository, times(1)).delete(BRANCH_ID);
    }

    // ------------------------------------------------------------------ //
    //  Error en createBranch → rollback (delete) + error propagado
    // ------------------------------------------------------------------ //

    @Test
    void createBranchShouldPropagateErrorWhenCreateBranchFails() {
        Branch branch = buildBranch();
        Mono<Branch> branchMono = Mono.just(branch);
        RuntimeException error = new RuntimeException("DB connection error");

        when(branchRepository.createBranch(any())).thenReturn(Mono.error(error));

        StepVerifier.create(createBranchUseCase.createBranch(branchMono, FRANCHISE_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("DB connection error"))
                .verify();

        verify(branchRepository, times(1)).createBranch(any());
        verify(franchiseRepository, never()).addBranchToFranchise(any(), any()); // ✅ nunca llega aquí
        verify(branchRepository, never()).delete(any());                          // ✅ no hay rollback
    }

    // ------------------------------------------------------------------ //
    //  Rollback también falla → el error original se propaga igual
    // ------------------------------------------------------------------ //

    @Test
    void createBranchShouldPropagateOriginalErrorWhenRollbackAlsoFails() {
        Branch branch = buildBranch();
        Mono<Branch> branchMono = Mono.just(branch);
        RuntimeException originalError  = new RuntimeException("Franchise not found");
        RuntimeException rollbackError  = new RuntimeException("Delete also failed");

        when(branchRepository.createBranch(any())).thenReturn(Mono.just(Boolean.TRUE));
        when(franchiseRepository.addBranchToFranchise(FRANCHISE_ID, BRANCH_ID))
                .thenReturn(Mono.error(originalError));
        when(branchRepository.delete(BRANCH_ID)).thenReturn(Mono.error(rollbackError));

        StepVerifier.create(createBranchUseCase.createBranch(branchMono, FRANCHISE_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("Delete also failed"))
                .verify();

        verify(branchRepository, times(1)).delete(BRANCH_ID);
    }

    // ------------------------------------------------------------------ //
    //  Mono<Branch> vacío → no se ejecuta ninguna operación
    // ------------------------------------------------------------------ //

    @Test
    void createBranchShouldCompleteEmptyWhenBranchMonoIsEmpty() {
        Mono<Branch> emptyBranchMono = Mono.empty();

        StepVerifier.create(createBranchUseCase.createBranch(emptyBranchMono, FRANCHISE_ID))
                .verifyComplete();

        verify(branchRepository, never()).createBranch(any());
        verify(franchiseRepository, never()).addBranchToFranchise(any(), any());
        verify(branchRepository, never()).delete(any());
    }
}