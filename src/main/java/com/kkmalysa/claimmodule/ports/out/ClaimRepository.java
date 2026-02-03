package com.kkmalysa.claimmodule.ports.out;

import com.kkmalysa.claimmodule.domain.claim.Claim;

import java.util.Optional;


public interface ClaimRepository {
    Optional<Claim> getById(String claimId);
    void save(Claim claim);
}
