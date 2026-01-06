#!/bin/bash

echo "========================================"
echo "Starting Mock Service..."
echo "========================================"

cd "$(dirname "$0")"

echo "Building project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Starting application..."
java -jar target/mock-service-1.0.0.jar
