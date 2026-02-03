package com.kkmalysa.claimmodule.ports.out;

import com.kkmalysa.claimmodule.application.submission.ClaimSubmission;

public interface SubmissionGateway {
    void send(ClaimSubmission submission);
}
