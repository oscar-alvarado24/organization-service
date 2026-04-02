package co.com.organization.usecase.createfranchise;

import co.com.organization.model.franchise.Franchise;
import co.com.organization.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateFranchiseUseCase {
    private final FranchiseRepository franchiseRepository;

    Mono<String> execute(Mono<Franchise> franchise){
        return franchiseRepository.createFranchise(franchise);
    }
}
