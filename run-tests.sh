#!/bin/bash

echo "========================================="
echo " FutureX Course Catalog - Test Suite"
echo "========================================="
echo

echo "Ejecutando pruebas..."
mvn test

echo
echo "========================================="
echo " Resultados"
echo "========================================="
echo "Ver reportes detallados en: target/surefire-reports/"
echo