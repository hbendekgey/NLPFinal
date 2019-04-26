#! /bin/bash

java -Xmx3G -cp "./bin" nlp.lm.LambdaLMModel $1 $2 $3 $4
