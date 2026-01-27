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
