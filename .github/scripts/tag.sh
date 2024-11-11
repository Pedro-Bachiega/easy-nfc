#!/bin/bash

message=$(git show -s --format=%B)

# Verify if the commit is a merge
merge_message=$(grep -E '(merge|Merge) pull request' <<< "$message" || true)
if [ -z "$merge_message" ]; then
    echo -e "MUST BE A MERGE MERGE!"
    echo "$message"
    exit 1
fi

# Verify if the commit is a merge following the pattern release/0.0.0 or hotfix/0.0.0
# A release may be named as alpha or a related suffix by appending its name and number
# Ex.: '-alpha01'
matched_message=$(grep -E '(release|hotfix)\/([0-9]+.[0-9]+.[0-9]+(-[a-z]+[0-9]+)*)' <<< "$message" || true)
if [ -z "$matched_message" ]; then
    echo -e "To generate a new release, you must name your branch as either a 'release' or a 'hotfix'"
    echo "$message"
    exit 1
fi

# Get version in message
version=$(sed -E 's?.+(release|hotfix)/([0-9]+.[0-9]+.[0-9]+(-[a-z]+[0-9]+)*)?\2?g' <<< "$matched_message")
echo "Tag: $version"

# Create and push tag
git tag -a "$version" -m "$version"
git push -u origin "$version"