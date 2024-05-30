# Releasing Sisu

## Maven

It should be the "usual" Maven release:
* `mvn release:prepare`
* `mvn release:perform`
* project uses https://oss.sonatype.org/ to stage (manual step: close and release staging repository)

## Site

TBD