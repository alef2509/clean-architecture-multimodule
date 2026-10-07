# 1. The dependency rule is enforced by the build, not by convention

- Status: accepted
- Date: 2026-10-06

## Context

Clean Architecture has one non-negotiable rule: source code dependencies point inwards.
Entities know nothing of use cases; use cases know nothing of the web, the database
or the framework. In a single-module project this rule depends on discipline (or on
architecture tests, as in the hexagonal project of this portfolio): nothing stops an
`import org.springframework...` inside the domain from compiling.

## Decision

One Maven module per ring, so the **compiler** enforces the rule:

| Module | Ring | May depend on |
|---|---|---|
| `library-domain` | Entities | nothing |
| `library-application` | Use cases | domain |
| `library-persistence` | Interface adapter | application (and JPA) |
| `library-web` | Interface adapter | application (and Spring MVC) |
| `library-app` | Main / frameworks | everything (composition root) |

On top of the module graph, the core modules run the **maven-enforcer `bannedDependencies`**
rule, so a framework added to the core, even transitively, fails the build with
*"The core must not depend on frameworks"*.

The adapters do not know each other: `library-web` cannot reference a JPA entity because
`library-persistence` is not on its classpath.

Other choices, contrasting with the hexagonal project:

- **Input boundaries and response models**: each use case is an interface
  (`BorrowCopy`) with its own `Request`/`Response` records, implemented by an interactor.
  The web layer never sees an entity.
- **Transactions as a port** (`UnitOfWork`): use cases declare what is atomic. The
  persistence adapter implements it with a database transaction and translates
  concurrency failures into a business `conflict`, so no Spring exception crosses the boundary.
- **Composition root**: interactors are created with plain constructors in
  `library-app`; the inner modules carry no annotations at all.

## Consequences

- Violations are impossible to merge, and the build says why.
- Use cases are tested with in-memory gateways (shared as a test-jar) in milliseconds.
- More modules and more mapping code (entity ↔ domain ↔ response model). It pays off
  when the domain is rich and long-lived, and when adapters may be replaced (another
  database, a messaging entry point). For a small CRUD service it is ceremony.
