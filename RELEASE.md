# Releasing Sisu

## Eclipse Foundation Release Policy

<https://www.eclipse.org/projects/handbook/#release>

## Maven

Note: Build uses latest `maven-gpg-plugin` and is getting rid "old bad practices" of storing sensitive information in
any Maven configuration file. Hence, on Workstations, users are recommended to have GPG Agent set up and running,
as plugin will make use of it to get the sensitive information. On unattended releases, the use of
BouncyCastle signer is recommended, and use environment variables `MAVEN_GPG_KEY` and `MAVEN_GPG_PASSPHRASE` 
to pass over the key material and the passphrase to `maven-gpg-plugin`.
See [maven-gpg-plugin site](https://maven.apache.org/plugins/maven-gpg-plugin/usage.html) for more information.

### Release steps

**Prerequisites:**
* have Sonatype Central Repository Portal User token in `settings.xml` (https://central.sonatype.org/publish/generate-portal-token/) for server with id `central`
* deploy snapshot: `mvn deploy -P sisu-release` for testing
* make sure source code does not have `@since TBD`; of have, search/replace it with upcoming version
* perform the release

The "usual" Maven release:
* `mvn release:prepare`
* `mvn release:perform`
* project uses <https://central.sonatype.com/publishing> to stage, afterwards you need to [manually publish via the web UI](https://central.sonatype.org/publish/publish-portal-guide/#publishing-your-components)

## Site

Look at <https://eclipse.dev/sisu/development.html#Site_Publishing>
