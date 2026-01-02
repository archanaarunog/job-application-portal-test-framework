#!/usr/bin/env bash
# scripts/verify_tc_tags.sh
# Verify TC IDs listed in the test-plan are present as tags in feature files and vice-versa
set -euo pipefail
PLAN=test-plan/functional/login_test.md
FEATURE_DIR=src/test/resources/features
TMP_PLAN=$(mktemp)
TMP_FEATURE=$(mktemp)

grep -oE 'TC[0-9]+(-[0-9]+)?' "$PLAN" | sort -u > "$TMP_PLAN" || true
# Use -h to suppress filenames so we only capture the tag text (no 'file:path:tag')
grep -h -oE '@TC[0-9]+(-[0-9]+)?' -R "$FEATURE_DIR" | sed 's/@//' | sort -u > "$TMP_FEATURE" || true

MISSING_IN_FEATURE_FILE=$(mktemp)
MISSING_IN_PLAN_FILE=$(mktemp)

comm -23 "$TMP_PLAN" "$TMP_FEATURE" > "$MISSING_IN_FEATURE_FILE" || true
comm -13 "$TMP_PLAN" "$TMP_FEATURE" > "$MISSING_IN_PLAN_FILE" || true

if [ ! -s "$MISSING_IN_FEATURE_FILE" ] && [ ! -s "$MISSING_IN_PLAN_FILE" ]; then
	echo "PASS: All TC IDs from the plan are present in feature tags and vice-versa."
	rm -f "$TMP_PLAN" "$TMP_FEATURE" "$MISSING_IN_FEATURE_FILE" "$MISSING_IN_PLAN_FILE"
	exit 0
fi

echo "In plan but not in features:"
if [ -s "$MISSING_IN_FEATURE_FILE" ]; then
	cat "$MISSING_IN_FEATURE_FILE"
else
	echo "  <none>"
fi

echo "In features but not in plan:"
if [ -s "$MISSING_IN_PLAN_FILE" ]; then
	cat "$MISSING_IN_PLAN_FILE"
else
	echo "  <none>"
fi

rm -f "$TMP_PLAN" "$TMP_FEATURE" "$MISSING_IN_FEATURE_FILE" "$MISSING_IN_PLAN_FILE"
exit 1
