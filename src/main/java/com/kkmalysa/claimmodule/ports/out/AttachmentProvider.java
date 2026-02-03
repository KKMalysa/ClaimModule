package com.kkmalysa.claimmodule.ports.out;

import java.util.List;

public interface AttachmentProvider {
    List<String> findAttachmentIdsForClaim(String claimId);
}
