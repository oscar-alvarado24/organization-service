package co.com.organization.usecase.createbranch;

import co.com.organization.model.branch.Branch;
import co.com.organization.model.branch.gateways.BranchRepository;
import co.com.organization.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateBranchUseCase {
    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;

    Mono<String> createBranch(Mono<Branch> branch, String franchiseId) {
        return branch.flatMap(b ->
                branchRepository.createBranch(Mono.just(b))
                        .flatMap(created ->
                                franchiseRepository.addBranchToFranchise(franchiseId, b.getId())
                                        .onErrorResume(e ->
                                                branchRepository.delete(b.getId())
                                                        .then(Mono.error(e))
                                        )
                        )
        );
    }
}
