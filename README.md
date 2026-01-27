commit 1: Initial project setup (Maven, builder, claim module)

commit 2: Implement StepBuilder on incident creation process
           This is our First Design Pattern - Builder in this project.
           Step Builder = a Builder variant that enforces the build order and required data at compile time.
           The build process is split into steps (interfaces), and available methods depend on the current step
           and chosen IncidentType. This prevents invalid object states and "setter chaos" by construction.
