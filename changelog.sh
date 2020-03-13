#!/bin/bash

firstTag=$(git tag | sort -r | head -1)
secondTag=$(git tag | sort -r | head -2 | awk '{split($0, tags, "\n")} END {print tags[1]}')
echo "Changes between ${secondTag} and ${firstTag}\n"
git log  --pretty=format:' * %s' ${secondTag}..${firstTag}
