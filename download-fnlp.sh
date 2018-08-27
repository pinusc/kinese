#! /bin/bash

[ ! -f "resources/fnlp-core-2.1.jar" ] && wget -P resources/ "https://repo1.maven.org/maven2/org/fnlp/fnlp-core/2.1/fnlp-core-2.1.jar"

mkdir -p models

[ ! -f "models/dep.m" ] && wget -P models/ "https://github.com/FudanNLP/fnlp/releases/download/v2.1/dep.m"
[ ! -f "models/seg.m" ] && wget -P models/ "https://github.com/FudanNLP/fnlp/releases/download/v2.1/seg.m"
[ ! -f "models/dep.m" ] && wget -P models/ "https://github.com/FudanNLP/fnlp/releases/download/v2.1/pos.m"
