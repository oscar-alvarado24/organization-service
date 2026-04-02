package co.com.organization.model.franchise.gateways;

import co.com.organization.model.franchise.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {
    Mono<String> createFranchise(Mono<Franchise> franchise);
    Mono<String> addBranchToFranchise(String franchiseId,String branchId);
    Mono<Franchise> getFranchiseById(String franchiseId);
    Mono<String> changeName(String franchiseId, String newName);
}
