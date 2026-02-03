package com.kkmalysa.claimmodule.adapters.out.attachments;

import com.kkmalysa.claimmodule.ports.out.AttachmentProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FakeAttachmentProvider implements AttachmentProvider {

    private final Map<String, List<String>> attachmentsByClaimId = new ConcurrentHashMap<>();

    @Override
    public List<String> findAttachmentIdsForClaim(String claimId) {
        return attachmentsByClaimId.getOrDefault(claimId, List.of());
    }

    // test helper / setup
    public void put(String claimId, List<String> attachmentIds) {
        attachmentsByClaimId.put(claimId, List.copyOf(attachmentIds));
    }
}
