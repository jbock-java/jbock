### RELEASING

1. Export these environment variables: `OSS_USER`, `OSS_PASS` (login for http://oss.sonatype.org/)
1. Export these environment variables: `ORG_GRADLE_PROJECT_signingKey`, `ORG_GRADLE_PROJECT_signingPassword`
   (`signingKey` is an ascii-armored encrypted gpg private key, so this variable contains a multiline string)
1. Run `./release core ${MY_VERSION}` or `./release annotations ${MY_VERSION}`

