commit 1: Initial project setup (Maven, builder, claim module)

commit 2: Implement StepBuilder on incident creation process
           This is our First Design Pattern - Builder in this project.
           Step Builder = a Builder variant that enforces the build order and required data at compile time.
           The build process is split into steps (interfaces), and available methods depend on the current step
           and chosen IncidentType. This prevents invalid object states and "setter chaos" by construction.
           
commit 3: Implement SequenceBuilder on claim creation process
           Builder #2 - ClaimBuilder (FNOL = First Notice of Loss).
           fnol() -> PolicyStep -> IncidentStep -> ReporterStep -> ChannelStep -> OptionalStep -> build()
           This is a Process/Workflow Builder: it guides the creation of a Claim through the FNOL flow,
           combining required domain inputs (policyId + Incident) and request context (createdBy + channel).
           Step Builder is used here to enforce the build order and mandatory data at compile time
           (PolicyStep -> IncidentStep -> ReporterStep -> ChannelStep -> build()).
           The BuilderImpl implements all step interfaces and returns "this" cast to the next interface.
           In build() we apply FNOL defaults/business rules:
           - generate claimId and createdAt (Clock injected for testability),
           - derive initial ClaimStatus from channel (e.g. MANUAL -> DRAFT, AUTO -> SUBMITTED).
           Future hook: this is the natural place (or the surrounding use-case) to emit ClaimCreatedEvent.

commit 4: Implement ClaimSubmission process with Step + Variant Builder
            Builder #3 – ClaimSubmissionBuilder (application layer).
            This builder assembles a submission package from multiple sources (Claim, policy data, attachments, request context) and applies channel-specific business rules.
            Step Builder is used to enforce the required build order and prevent incomplete submissions.
            A variant decision point (manual() / auto()) defines different validation paths:
            AUTO channel requires at least one attachment and accepted terms,
            MANUAL channel allows incomplete data for later completion by an agent.
            A new SubmitClaimUseCase orchestrates the submission flow:
            fetches required data via ports,
            builds a validated ClaimSubmission,
            sends it through a submission gateway,
            updates the Claim status accordingly.
            External integrations are mocked via ports and in-memory/fake adapters, keeping the domain clean and the process fully testable.

commit 5: Introduce domain events as first-class result of submission process
            Builder #4 – DomainEventBuilder.
            Domain events are introduced as explicit outcomes of the submission use-case,
            not as hidden side effects.
            SubmitClaimUseCase now returns a SubmissionResult object, which represents
            the full business outcome of the process:
            - the created ClaimSubmission,
            - the updated Claim state,
            - a domain event describing what happened (ClaimSubmittedEvent).
            DomainEventBuilder encapsulates the construction of consistent domain events,
            including event type, timestamps and contextual data (claim, submission, actor),
            keeping event creation logic centralized and reusable.
            This change prepares the application for event-driven workflows and observers,
            while keeping the domain model clean and free of infrastructural concerns.
 
