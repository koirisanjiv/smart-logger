# Smart Logger Distribution Creator

This folder makes the client distribution reproducible. The production POM is standalone and contains all Maven plugin/dependency versions it needs; it does not depend on the old multi-module parent POM.

After any production change:

```bash
./distribution/create-distribution.sh
```

The script will:

1. Read the release version from `pom.xml`.
2. Run `mvn clean test`.
3. Build the production JAR.
4. Copy the production source/resources and release documentation.
5. Include the production JAR.
6. Exclude benchmark/JMH artifacts.
7. Create:

```text
distribution/smart-logger-<version>-distribution.zip
```

## Release version

For a new release, update the version in `pom.xml`, then run:

```bash
./distribution/create-distribution.sh
```

You can also override the package version explicitly:

```bash
./distribution/create-distribution.sh 0.3.5
```

Use the explicit version only when the POM has already been updated consistently.

## What the client receives

```text
smart-logger-<version>/
├── pom.xml
├── README.md
├── readTechnicalStructure.md
├── CLIENT-DEPENDENCY.xml
├── PUBLISHING.md
├── RELEASE-METADATA.txt
├── target/
│   └── smart-logger-<version>.jar
└── src/
    └── main/
```

The distribution is for client/reference use. The normal long-term delivery mechanism is Maven dependency resolution from the published GitHub/Maven repository.

## GitHub release flow

Recommended:

```text
Code change
   ↓
mvn clean test
   ↓
./distribution/create-distribution.sh
   ↓
Review ZIP/JAR
   ↓
Commit + push to GitHub
   ↓
Create version tag
   ↓
GitHub/Maven publication
   ↓
Client pom.xml dependency
```

Client:

```xml
<dependency>
    <groupId>com.qaverse.smart</groupId>
    <artifactId>smart-logger</artifactId>
    <version>0.3.4</version>
</dependency>
```

Do not make clients depend on the distribution ZIP. The ZIP is a release/support artifact; the Maven artifact is the actual client dependency.
