#!/bin/sh

if [ $(gpg --list-keys F44DF2DB332C52EF87557C990B5F2FF38F52623D 2>/dev/null | wc -l) -eq 0 ]; then
  gpg --quiet --batch --yes --decrypt --passphrase="${GPG_PASSPHRASE}" --output ./.mvn/maven-gpg-private.key ./.mvn/maven-gpg-private.key.gpg
  gpg --batch --import ./.mvn/maven-gpg-private.key
  rm ./.mvn/maven-gpg-private.key
fi
