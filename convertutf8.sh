#!/bin/bash
find . -name \*.txt -type f | \
  (while read file; do
    iconv -f ISO-8859-1 -t UTF-8 "$file" > "${file%.txt}.utf8";
    rm -f "$file";
    mv "${file%.txt}.utf8" "$file"
  done);