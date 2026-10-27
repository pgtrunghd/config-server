#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "$0")/.." && pwd)"
repo_dir="$(dirname "$project_dir")/medical-config-repo"

# Do not overwrite an existing repository or configuration.
if [[ -e "$repo_dir" ]]; then
    if git -C "$repo_dir" rev-parse --show-toplevel >/dev/null 2>&1 \
        && [[ -f "$repo_dir/patient-service.properties" ]] \
        && git -C "$repo_dir" rev-parse --verify main >/dev/null 2>&1; then
        echo "Kho cau hinh da san sang: $repo_dir"
        exit 0
    fi
    echo "Thu muc da ton tai nhung chua phai kho cau hinh main hop le: $repo_dir" >&2
    exit 1
fi

mkdir -p "$repo_dir"
git -C "$repo_dir" init -b main
cp "$project_dir/config-repo/patient-service.properties" "$repo_dir/"
git -C "$repo_dir" add patient-service.properties
git -C "$repo_dir" commit -m "Add patient service configuration"
echo "Da tao kho Git cau hinh: $repo_dir"
