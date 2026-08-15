#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./distribution/release.sh 0.3.5
#
# This helper:
#   - updates the POM version
#   - runs tests
#   - creates the client distribution
#   - creates an annotated Git tag
#   - optionally pushes the commit and tag
#
# Publishing itself is performed by GitHub Actions after the tag is pushed.
# It therefore never needs Maven Central credentials on the developer machine.

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NEW_VERSION="${1:-}"

if [[ -z "$NEW_VERSION" ]]; then
  echo "Usage: $0 <release-version>"
  echo "Example: $0 0.3.5"
  exit 1
fi

if [[ "$NEW_VERSION" == *"-SNAPSHOT" ]]; then
  echo "ERROR: Release version must not contain -SNAPSHOT."
  exit 1
fi

cd "$ROOT"

CURRENT_VERSION="$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version)"
echo "Current version: $CURRENT_VERSION"
echo "New version:     $NEW_VERSION"

if git diff --quiet && git diff --cached --quiet; then
  :
else
  echo "ERROR: Working tree has uncommitted changes. Commit your changes before releasing."
  exit 1
fi

mvn -q versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false

mvn -q clean test
./distribution/create-distribution.sh "$NEW_VERSION"

git add pom.xml README.md document/readTechnicalStructure.md document/PUBLISHING.md CLIENT-DEPENDENCY.xml distribution .github .gitignore
git commit -m "Release Smart Logger $NEW_VERSION"
git tag -a "v$NEW_VERSION" -m "Smart Logger $NEW_VERSION"

echo
echo "Release prepared locally."
echo "Push with:"
echo "  git push origin main --follow-tags"
echo
echo "GitHub Actions will validate the tag and publish to Maven Central."
