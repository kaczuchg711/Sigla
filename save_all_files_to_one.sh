#!/bin/bash

# Check if two arguments are given
#if [ "$#" -ne 2 ]; then
#    echo "Usage: $0 <directory> <output_file>"
#    exit 1
#fi

# Assign arguments to variables
DIR="src"
OUTPUT_FILE="all_files.txt"

# Check if directory exists
if [ ! -d "$DIR" ]; then
    echo "Directory $DIR does not exist."
    exit 1
fi

# Empty the output file in case it already exists
> "$OUTPUT_FILE"

# Concatenate all files from directory and subdirectories
find "$DIR" -type f \( -name "*.txt" -o -name "*.java" -o -name "*.sh" -o -name "*.properties" -o -name "*.md" \) | while read -r file; do
    echo "---- Content of $file ----" >> "all_files.txt"
    cat "$file" >> "all_files.txt"
    echo -e "\n" >> "all_files.txt"
done

echo "---- Content of pom.xml ----" >> all_files.txt
cat pom.xml >> all_files.txt
echo -e "\n" >> all_files.txt

echo "All files in $DIR and its subdirectories have been concatenated into $OUTPUT_FILE"