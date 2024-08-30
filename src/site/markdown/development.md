<!-- MACRO{toc} -->

# How to Contribute

See [Contributing](https://github.com/eclipse-sisu/sisu-project/blob/main/CONTRIBUTING.md)

# How to Release

See [Releasing Sisu](https://github.com/eclipse-sisu/sisu-project/blob/main/RELEASE.md).

# Management of GitHub Repositories

Both the GitHub organisation as well as the repositories of Sisu are managed via [OtterDog](https://github.com/eclipse-csi/otterdog) configured in <https://github.com/eclipse-sisu/.eclipsefdn>.

# Site Publishing

The site is built with [maven-site-plugin](https://maven.apache.org/plugins/maven-site-plugin/) and then 

1. published via [maven-scm-publish-plugin](https://maven.apache.org/plugins/maven-scm-publish-plugin/) to the dedicated Git repository [`sisu-website`][sisu-website] in branch `gh-pages` and ...
2. polled (every 5 minutes) and synced to the Eclipse Web Server in order to serve it from <https://eclipse.dev/sisu/>.

*Despite the branch naming `gh-pages` GitHub Pages is currently not used for serving the site, further details in [Eclipse Helpdesk Issue 4998](https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/issues/4989)*

In order to publish it run the following commands from the root of the clone of [`sisu-project`](https://github.com/eclipse-sisu/sisu-project)

```
mvn site site:stage
mvn scm-publish:publish-scm
```

## Authentication

The site publishing leverages [GitHub SSH authentication](https://docs.github.com/en/authentication/connecting-to-github-with-ssh). Make sure to have Git authentication configured accordingly for [`sisu-website`][sisu-website].

## Eclipse Hosting References

- <https://www.eclipse.org/projects/handbook/#resources-website>
- <https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/wikis/IT_Infrastructure_Doc#website>

[sisu-website]: https://github.com/eclipse-sisu/sisu-website
