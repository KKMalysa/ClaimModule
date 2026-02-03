package com.kkmalysa.claimmodule.ports.out;

import java.util.Optional;

public interface PolicyProvider {
    Optional<String> getPolicyNumber(String policyId);
}
