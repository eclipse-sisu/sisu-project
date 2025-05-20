# Releasing Sisu

## Eclipse Foundation Release Policy

<https://www.eclipse.org/projects/handbook/#release>

## Maven

Build uses Maven Wrapper with latest Maven 3.9.9.

Note: Build uses latest `maven-gpg-plugin` and is getting rid "old bad practices" of storing sensitive information in
any Maven configuration file. Hence, on Workstations, users are recommended to have GPG Agent set up and running,
as plugin will make use of it to get the sensitive information. On unattended releases, the use of
BouncyCastle signer is recommended, and use environment variables `MAVEN_GPG_KEY` and `MAVEN_GPG_PASSPHRASE` 
to pass over the key material and the passphrase to `maven-gpg-plugin`.
See [maven-gpg-plugin site](https://maven.apache.org/plugins/maven-gpg-plugin/usage.html) for more information.

### Prepare for release

Release uses [Maveniverse Njord](https://github.com/maveniverse/njord) to publish to [Maven Central](https://repo.maven.apache.org/maven2/) 
using the [Sonatype Central Portal](https://central.sonatype.com/) service.

Your user environment need some extra steps to prepare for publishing. 

For start, you need to have an account on Sonatype Central Portal and your account needs to have access to 
`org.eclipse.sisu` namespace on Sonatype Central Portal.

Next, you need to obtain user tokens for publishing from Sonatype Central Portal. To generate them, log in and
browse to https://central.sonatype.com/account page and use Generate User Token.

The generated user tokens need to be added to your user Maven settings (by default the `~/.m2/settings.xml` file) as
following server entry:

```xml
    <server>
      <id>sonatype-central-portal</id>
      <username>$TOKEN1</username>
      <password>$TOKEN2</password>
      <configuration>
        <njord.publisher>sonatype-cp</njord.publisher>
        <njord.releaseUrl>njord:template:release-sca</njord.releaseUrl>
      </configuration>
    </server>
```

while there, another good change for settings is this:

```xml
   <pluginGroups>
     <pluginGroup>eu.maveniverse.maven.plugins</pluginGroup>
   </pluginGroups>
```

Use command `mvn njord:status` to review publishing configuration.

### Release steps

**Prerequisites:**
* deploy snapshot: `mvn deploy -P sisu-release` for testing
* make sure source code does not have `@since TBD`; of have, search/replace it with upcoming version
* perform the release

To perform Maven release invoke:
* `mvn release:prepare`
* `mvn release:perform`
* project will be staged to [Sonatype Central Portal](https://central.sonatype.com/publishing) (manual step: once validation passes, publish it)
* **don't forget to push git changes** once all done (`maven-release-plugin` is configured to not push them): `git push origin main --tags`.

## Site

Look at https://eclipse.dev/sisu/development.html#Site_Publishing
