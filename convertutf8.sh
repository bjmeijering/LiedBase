#!/bin/bash

if [ $# -ne 1 ]; then
  echo "usage: $0 <file extention>"
  exit 1
 fi
 
 EXT=$1

find . -name \*.$EXT -type f | \
while read file; do
  iconv -f ISO-8859-1 -t UTF-8 "$file" > "${file%.$EXT}.utf8";
  rm -f "$file";
  mv "${file%.$EXT}.utf8" "$file"
done;
