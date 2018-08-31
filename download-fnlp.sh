#! /bin/bash

[ ! -f "resources/fnlp-core-2.1.jar" ] && wget -q -P resources/ "https://repo1.maven.org/maven2/org/fnlp/fnlp-core/2.1/fnlp-core-2.1.jar"

echo "================================="
echo ""
echo ""
echo "Downloaded fnlp-core-2.1.jar"
echo ""
echo ""
echo "================================="

mkdir -p models

[ ! -f "models/dep.m" ] && wget -q -P models/ "https://github.com/FudanNLP/fnlp/releases/download/v2.1/dep.m"
[ ! -f "models/seg.m" ] && wget -q -P models/ "https://github.com/FudanNLP/fnlp/releases/download/v2.1/seg.m"
[ ! -f "models/dep.m" ] && wget -q -P models/ "https://github.com/FudanNLP/fnlp/releases/download/v2.1/pos.m"

exit 0 # if anything it will fail at compile or run time
