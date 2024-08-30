# Overview

Plexus is an IoC container, originally developed at [Codehaus][codehaus] and meanwhile migrated to [GitHub][plexus-containers].
It relies on an [XML Descriptor][plexus-component-descriptor] which declares the dependencies of each component.

This project provides a Plexus compatibility layer on top of Sisu which should be used instead when a project for some reasons still needs to rely on Plexus API. It support injection of both [legacy Plexus components](plexus-component-descriptor) as well as Sisu JSR 330 components.

Plexus itself is considered legacy nowadays so new projects should no longer rely on Plexus API/metadata but rather use [JSR 330 annotations only][conversion-to-jsr330] (which converts them from Plexus to plain Sisu components).

Even the projects listed in [Plexus Components][plexus-components] have been migrated meanwhile to JSR 330 annotations and do no longer rely on Plexus annotations/metadata (despite their name).

[plexus-containers]: https://codehaus-plexus.github.io/plexus-containers/
[plexus-component-descriptor]: https://codehaus-plexus.github.io/plexus-containers/plexus-container-default/plexus-components.html
[codehaus]: https://www.reddit.com/r/java/comments/2xciu8/codehaus_birthplace_of_many_java_oss_projects/
[plexus-components]: https://codehaus-plexus.github.io/ref/available-components.html
[conversion-to-jsr330]: conversion-to-jsr330.html