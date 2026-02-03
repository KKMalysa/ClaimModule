package com.kkmalysa.claimmodule.adapters.out.persistence;

import com.kkmalysa.claimmodule.domain.claim.Claim;
import com.kkmalysa.claimmodule.ports.out.ClaimRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryClaimRepository implements ClaimRepository {

    private final Map<String, Claim> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Claim> getById(String claimId) {
        return Optional.ofNullable(store.get(claimId));
    }

    @Override
    public void save(Claim claim) {
        store.put(claim.getClaimId(), claim);
    }
}
