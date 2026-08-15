#!/usr/bin/env bash
set -euo pipefail

# Rebuild the client distribution from the current production project.
# Usage:
#   ./distribution/create-distribution.sh
# Optional:
#   ./distribution/create-distribution.sh 0.3.5

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="$PROJECT_ROOT/distribution"
VERSION="${1:-}"

if [[ ! -f "$PROJECT_ROOT/pom.xml" ]]; then
  echo "ERROR: pom.xml not found at $PROJECT_ROOT"
  exit 1
fi

if [[ -z "$VERSION" ]]; then
  VERSION="$(sed -n 's:.*<version>\([^<]*\)</version>.*:\1:p' "$PROJECT_ROOT/pom.xml" | head -1)"
fi

if [[ -z "$VERSION" ]]; then
  echo "ERROR: Could not determine version from pom.xml"
  exit 1
fi

PACKAGE_NAME="smart-logger-${VERSION}"
STAGE="$DIST_DIR/.stage/$PACKAGE_NAME"
OUTPUT="$DIST_DIR/${PACKAGE_NAME}-distribution.zip"

rm -rf "$DIST_DIR/.stage"
rm -f "$OUTPUT"
mkdir -p "$STAGE"

echo "==> Building and testing Smart Logger $VERSION"
mvn -q -f "$PROJECT_ROOT/pom.xml" clean test

echo "==> Creating production artifact"
mvn -q -f "$PROJECT_ROOT/pom.xml" package -DskipTests

echo "==> Copying distribution files"
cp "$PROJECT_ROOT/pom.xml" "$STAGE/"
cp "$PROJECT_ROOT/README.md" "$STAGE/"
cp "$PROJECT_ROOT/readTechnicalStructure.md" "$STAGE/"
cp "$PROJECT_ROOT/CLIENT-DEPENDENCY.xml" "$STAGE/"
cp "$PROJECT_ROOT/PUBLISHING.md" "$STAGE/"

mkdir -p "$STAGE/src"
cp -R "$PROJECT_ROOT/src/main" "$STAGE/src/"

# Include the built production JAR so a client/internal release process has it
# immediately available. Do not include benchmark artifacts.
mkdir -p "$STAGE/target"
cp "$PROJECT_ROOT/target/smart-logger-${VERSION}.jar" "$STAGE/target/"

# Re-create a small release metadata file.
cat > "$STAGE/RELEASE-METADATA.txt" <<EOF
Smart Logger
Version: $VERSION
GroupId: com.qaverse.smart
ArtifactId: smart-logger
Created: $(date -u +"%Y-%m-%dT%H:%M:%SZ")

This distribution contains the production Smart Logger module only.
Benchmarks/JMH are intentionally excluded.
EOF

echo "==> Creating ZIP (client/support artifact; Maven Central publication is handled by GitHub Actions)"
(
  cd "$DIST_DIR/.stage"
  zip -qr "$OUTPUT" "$PACKAGE_NAME"
)

rm -rf "$DIST_DIR/.stage"

echo
echo "Distribution created successfully:"
echo "  $OUTPUT"
echo
echo "Maven coordinates:"
echo "  com.qaverse.smart:smart-logger:$VERSION"
