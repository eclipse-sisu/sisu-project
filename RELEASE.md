# Releasing Sisu

## Maven

Note: Build uses latest `maven-gpg-plugin` and is getting rid "old bad practices" of storing sensitive information in
any Maven configuration file. Hence, on Workstations, users are recommended to have GPG Agent set up and running,
as plugin will make use of it to get the sensitive information. On unattended releases, the use of
BouncyCastle signer is recommended, and use environment variables `MAVEN_GPG_KEY` and `MAVEN_GPG_PASSPHRASE` 
to pass over the key material and the passphrase to `maven-gpg-plugin`.
See [maven-gpg-plugin site](https://maven.apache.org/plugins/maven-gpg-plugin/usage.html) for more information.

### Release steps

Prerequsites:
* deploy snapshot: `mvn deploy -P sisu-release` for testing
* make sure source code does not have `@since TBD`; of have, search/replace it with upcoming version
* perform the release

The "usual" Maven release:
* `mvn release:prepare`
* `mvn release:perform`
* project uses https://oss.sonatype.org/ to stage (manual step: close and release staging repository)

## Site

TBD
