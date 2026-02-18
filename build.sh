#!/bin/bash
#
# Build script for AlgorithmicTypography Processing Library
# 
# This script compiles the library and creates the JAR file.
# Requires: Java 11+, Processing installed
#

set -e

LIBRARY_NAME="AlgorithmicTypography"
VERSION="1.0.0"
SRC_DIR="src"
BUILD_DIR="build"
LIBRARY_DIR="library"
REFERENCE_DIR="reference"

if [ -d "/Applications/Processing.app/Contents/app/resources/core/library" ]; then
    PROCESSING_CORE_DIR="/Applications/Processing.app/Contents/app/resources/core/library"
elif [ -d "/Applications/Processing.app/Contents/Java/core/library" ]; then
    PROCESSING_CORE_DIR="/Applications/Processing.app/Contents/Java/core/library"
elif [ -d "$HOME/Processing/core/library" ]; then
    PROCESSING_CORE_DIR="$HOME/Processing/core/library"
elif [ -d "/usr/share/processing/core/library" ]; then
    PROCESSING_CORE_DIR="/usr/share/processing/core/library"
else
    echo "Error: Could not find Processing installation"
    echo "Please set PROCESSING_CORE_DIR manually in this script"
    exit 1
fi

CORE_JAR=$(find "$PROCESSING_CORE_DIR" -name "core*.jar" -not -name "*natives*" | head -1)

if [ -z "$CORE_JAR" ] || [ ! -f "$CORE_JAR" ]; then
    echo "Error: core.jar not found in ${PROCESSING_CORE_DIR}"
    exit 1
fi

echo "========================================="
echo "Building ${LIBRARY_NAME} ${VERSION}"
echo "Core JAR: ${CORE_JAR}"
echo "========================================="

echo "Cleaning previous build..."
rm -rf "${BUILD_DIR}"
rm -f "${LIBRARY_DIR}"/*.jar

mkdir -p "${BUILD_DIR}"
mkdir -p "${LIBRARY_DIR}"

echo "Compiling source..."
find "${SRC_DIR}" -name "*.java" > /tmp/at_sources.txt
javac -cp "${CORE_JAR}" \
      --release 17 \
      -d "${BUILD_DIR}" \
      -sourcepath "${SRC_DIR}" \
      @/tmp/at_sources.txt

echo "Creating JAR..."
jar cvf "${LIBRARY_DIR}/${LIBRARY_NAME}.jar" \
    -C "${BUILD_DIR}" .

if command -v javadoc &> /dev/null; then
    echo "Generating Javadoc..."
    mkdir -p "${REFERENCE_DIR}"
    javadoc -d "${REFERENCE_DIR}" \
            -classpath "${CORE_JAR}" \
            -sourcepath "${SRC_DIR}" \
            algorithmic.typography
fi

echo ""
echo "========================================="
echo "Build complete!"
echo "========================================="
echo "Library JAR: ${LIBRARY_DIR}/${LIBRARY_NAME}.jar"
echo ""
echo "To install:"
echo "  1. Copy this folder to Documents/Processing/libraries/"
echo "  2. Restart Processing"
echo "  3. Use Sketch → Import Library → AlgorithmicTypography"
