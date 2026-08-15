# Publishing Smart Logger

Smart Logger is designed to be published as a normal Maven artifact so clients can use one dependency:

```xml
<dependency>
    <groupId>com.qaverse.smart</groupId>
    <artifactId>smart-logger</artifactId>
    <version>0.3.4</version>
</dependency>
```

The recommended public distribution is **Maven Central** through Sonatype Central Publisher Portal.

The current Maven Central documentation uses `central-publishing-maven-plugin` and user-token credentials. Automatic publishing is supported by setting `autoPublish=true`. See the official documentation: https://central.sonatype.org/publish/publish-portal-maven/

## 1. One-time setup

### A. GitHub repository

Push this project to GitHub.

Before the first release, replace these placeholders in `pom.xml`:

```text
REPLACE_WITH_YOUR_GITHUB_OWNER
```

with the actual GitHub owner/repository URL.

### B. Maven Central / Sonatype

Create/verify your namespace in the Central Publisher Portal and create a user token for publishing.

The GitHub Actions workflow expects these repository secrets:

```text
MAVEN_CENTRAL_USERNAME
MAVEN_CENTRAL_PASSWORD
MAVEN_GPG_PRIVATE_KEY
MAVEN_GPG_PASSPHRASE
```

The Central username/password are the Sonatype user-token credentials.

The GPG secrets are the private signing key and its passphrase. Never commit either to Git.

### C. GPG signing key

Maven Central releases need signed artifacts. The workflow imports the private key and the Maven GPG plugin signs the release artifacts.

The public key must be available through a public keyserver/key infrastructure appropriate for your release process.

## 2. Normal development

```bash
mvn clean test
```

No Maven Central credentials are required.

## 3. Create a release

After your code changes are committed:

```bash
./distribution/release.sh 0.3.5
```

The script:

1. Updates the Maven version.
2. Runs the full test suite.
3. Creates the client distribution ZIP.
4. Creates an annotated Git tag `v0.3.5`.
5. Commits the release version.
6. Leaves publishing to GitHub Actions.

Then:

```bash
git push origin main --follow-tags
```

## 4. What happens automatically

GitHub Actions sees:

```text
v0.3.5
```

and performs:

```text
Checkout
   ↓
Java 21
   ↓
Validate POM version == tag
   ↓
mvn clean test
   ↓
Import GPG signing key
   ↓
Configure Maven Central credentials
   ↓
mvn -Prelease deploy
   ↓
Create JAR
   ↓
Create sources JAR
   ↓
Create Javadoc JAR
   ↓
Create GPG signatures
   ↓
Central Publisher Portal validation
   ↓
Automatic publish
   ↓
Maven Central
```

The official Central Publisher Portal documentation confirms that `mvn deploy` can upload the bundle and that `autoPublish=true` enables automatic publishing. urlCentral Maven publishing documentationhttps://central.sonatype.org/publish/publish-portal-maven/

## 5. Important version rule

Maven Central releases are immutable. Once `0.3.5` is published, do not modify/re-upload `0.3.5`.

For every change:

```text
0.3.4
  ↓
0.3.5
  ↓
0.3.6
```

Use a new version.

## 6. Distribution ZIP vs Maven artifact

The generated ZIP is:

```text
distribution/smart-logger-0.3.5-distribution.zip
```

It is a client/support/release artifact.

Clients should **not** depend on the ZIP.

Clients should use:

```xml
<dependency>
    <groupId>com.qaverse.smart</groupId>
    <artifactId>smart-logger</artifactId>
    <version>0.3.5</version>
</dependency>
```

Maven Central is the public repository from which Maven resolves the dependency.

## 7. Manual publishing from a developer machine

A developer with properly configured Maven Central credentials and GPG can publish with:

```bash
mvn -Prelease clean deploy
```

However, the recommended team workflow is the GitHub Actions release because credentials stay in GitHub Secrets rather than developer machines.

## 8. Who can publish?

Do **not** make public repository contributors able to publish automatically.

Anyone can:

- clone the project
- modify code
- run tests
- build a distribution

Only an authorized maintainer with GitHub permission to push release tags and access to the repository's publishing secrets can publish to Maven Central.

This is the safe meaning of "anyone on the team can publish": the process is standardized; credentials remain protected.

## 9. Failed release

If tests, GPG signing, Central validation, or publishing fail:

```text
GitHub Actions → failed
Maven Central → no published release
```

Fix the problem, create a new version if a deployment has already been published, and run the release again.

## 10. Maven Central facts

Central's current documentation states that published components cannot be modified/updated/deleted after publication, so every public release must use a new version. citeturn0search3turn0search5

OSSRH is no longer the primary publishing service; it reached end of life on June 30, 2025. Use the Central Publisher Portal flow instead. citeturn0search9
