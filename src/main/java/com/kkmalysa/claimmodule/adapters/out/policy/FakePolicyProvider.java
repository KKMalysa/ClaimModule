package com.kkmalysa.claimmodule.adapters.out.policy;

import com.kkmalysa.claimmodule.ports.out.PolicyProvider;

import java.util.Optional;

public final class FakePolicyProvider implements PolicyProvider {

    @Override
    public Optional<String> getPolicyNumber(String policyId) {
        // simple fake
        return Optional.of("PN-" + policyId);
    }
}
