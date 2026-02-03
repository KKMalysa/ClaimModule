package com.kkmalysa.claimmodule.adapters.out.submission;

import com.kkmalysa.claimmodule.application.submission.ClaimSubmission;
import com.kkmalysa.claimmodule.ports.out.SubmissionGateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InMemorySubmissionGateway implements SubmissionGateway {

    private final List<ClaimSubmission> sent = new ArrayList<>();

    @Override
    public void send(ClaimSubmission submission) {
        sent.add(submission);
    }

    public List<ClaimSubmission> sentSubmissions() {
        return Collections.unmodifiableList(sent);
    }
}
