#!/bin/bash
set -e

wget "https://gist.githubusercontent.com/fennifith/664f534ead7336adf420d5afa72628f9/raw/travis-github-release.sh" -O .scripts/github-release.sh
chmod +x .scripts/github-release.sh && ./.scripts/github-release.sh

exit 0