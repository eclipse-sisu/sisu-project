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

## Before release

**Prerequisites:**
* deploy snapshot: `mvn deploy -P sisu-release` for testing
* make sure source code does not have `@since TBD`; of have, search/replace it with upcoming version
* perform the release

## Using Njord

This build publishes to Maven Central using [Sonatype Central Portal](https://central.sonatype.com/) service. To accomplish this, build
uses [Maveniverse Njord](https://github.com/maveniverse/njord) extension. Check out [Njord plugin](https://maveniverse.eu/docs/njord/) documentation as well.

Follow these steps to set up yourself to publish to Central Portal:

1. Using https://central.sonatype.com/account (while logged in) generate tokens for your account. Add those tokens
   **to the service/server we use to publish**, the Sonatype Central Portal as this in `settings.xml`:
   ```xml
    <server>
      <id>sonatype-cp</id>
      <!-- Create TOKEN1/TOKEN2 with Portal Service -->
      <username>$TOKEN1</username>
      <password>$TOKEN2</password>
    </server>
   ```
2. Sisu POM distribution management servers setup, add to your `settings.xml` following server entry:
   ```xml
    <server>
      <id>sonatype-cp-service</id>
      <configuration>
        <!-- Using Sonatype Central Portal publisher -->
        <njord.publisher>sonatype-cp</njord.publisher>
        <!-- Releases are staged locally (if omitted, would go directly to URL as per POM) -->
        <!-- Snapshots goes directly to URL as per POM -->
        <njord.releaseUrl>njord:template:release-sca</njord.releaseUrl>
      </configuration>
    </server>
   ```
   while here, another good change for settings is this:
   ```xml
   <pluginGroups>
     <pluginGroup>eu.maveniverse.maven.plugins</pluginGroup>
   </pluginGroups>
   ```
   The POM `project/distributionManagement/repository/id` named server is now "redirected" to `sonatype-cp` server 
   (and publishing service).
3. Perform release "as usual" (execute `mvn release:prepare` followed by `mvn release:perform`)
4. If build ended OK, you will have locally staged release, use `mvn njord:list` to check store name.
5. If needed, verify staging content using `mvn njord:list-content -Dstore=$storeName`
6. If all okay, publish the staged store to Sonatype Central Portal `mvn publish` (by default will publish to `sonatype-cp` and will figure out store. If override needed, use `-Dstore=$storeName -Dtarget=sonatype-cp`)

## Site

Look at https://eclipse.dev/sisu/development.html#Site_Publishing
