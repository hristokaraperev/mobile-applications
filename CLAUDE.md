# Project

## Technologies

- Android front-end: Kotlin
- Back-end server: Java/Spring Boot
- Database: 
  - Front-end: SQLite, if any data to be saved on the front-end
  - Back-end: Postgres
- Deployment: hosted API, containerized

## Nice-to-haves

- HTTPS communication

## General guidelines

- When creating new issues, *ALWAYS* use the templates in the `.github` folder.
- When starting to work on a new issue *ALWAYS* create a new branch: <fix|feature|or something else>/<short description of the issue> on a new git worktree. *ALWAYS* create worktree folder in `/tmp`. *ALWAYS* clean up branch and worktree after creating pull request.
- Commit at small, incremental steps, when enough work has been completed to be considered a separate unit of work. Use the commit template from `.gitmessage`. Keep it brief. *NEVER* add co-authored messages or extended commit descriptions. Be straight-to-the-point when writing commit meesages.
- When user has been satisfied that there are no more work to be done, open a pull request. Each pull request *MUST* follow the repository's template provide description of how to test the changes manually.

## Code quality

- Always add javadocs to classes and public and private API that are not self-explanatory with the code. 
- Always leave an empty line before return statements if it is not the only statement in a method or in a control block.

## Backend applications - instructions

- Create a postman collection and always keep up adding new endpoints in for testing purposes. 

## Agent skills

### Issue tracker

Issues live in GitHub Issues for `hristokaraperev/mobile-applications` (uses the `gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
