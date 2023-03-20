Contributor License
-------------------

If this is your first time contributing to an Eclipse Foundation project, you'll need to sign the [Eclipse Contributor Agreement][ECA].

- [Create an account](https://dev.eclipse.org/site_login/createaccount.php) on dev.eclipse.org
- Open your [Account Settings tab](https://dev.eclipse.org/site_login/myaccount.php#open_tab_accountsettings), enter your GitHub ID and click Update Account
- Read and [sign the ECA](https://dev.eclipse.org/site_login/myaccount.php#open_tab_cla)
- Your git commits must be [signed off](https://wiki.eclipse.org/Development_Resources/Contributing_via_Git#Signing_off_on_a_commit)
- Use the exact same email address for your Eclipse account, your commit author, and your commit sign-off.

Issues
------

Search the [issue tracker][issue-tracker] for a relevant issue or create a new one.

Making changes
--------------

Fork the repository in GitHub and make changes in your fork, add a description of your changes, and submit a pull request.

Contact us
----------

[Join the mailing list][mailing-list] and email the community at sisu-dev@eclipse.org to discuss your ideas and get help.

Build
-----

The Sisu build requires Java 11 and Maven 3.6.3 (or higher), while the resulting jars work with Java 8 and above.

Coding Style
------------

Sisu follows the same [code style and code conventions][style-guide] as Maven.

Avoid changing whitespace on lines that are unrelated to your pull request. This helps preserve the accuracy of the git blame view, and makes code reviews easier.

You can use the spotless maven plugin to automatically format code to the accepted code style

```bash
$ mvn spotless:apply
```

Commit messages
---------------

- [Mention the GitHub issue][github-issue] when relevant
- It's a good idea to follow the [advice in Pro Git](https://git-scm.com/book/ch5-2.html)
- Sign-off your commits using `git commit --signoff` or `git commit -s` for short

Pull requests
-------------

Excessive branching and merging can make git history confusing. With that in mind

- Squash your commits down to a few commits, or one commit, before submitting a pull request
- [Rebase your pull request changes on top of the current master][rebase]. Pull requests shouldn't include merge commits.

Submit your pull request when ready. Two checks will be kicked off automatically

- IP Validation: checks that all committers signed the Eclipse CLA and signed their commits.
- The standard GitHub check that the pull request has no conflicts with the base branch.

One of the committers will take a look and provide feedback or merge your contribution.

That's it, thanks for contributing to Sisu!

[ECA]:             https://www.eclipse.org/legal/ECA.php
[issue-tracker]:   https://github.com/eclipse/sisu.plexus/issues
[style-guide]:     https://maven.apache.org/developers/conventions/code.html
[rebase]:          https://github.com/edx/edx-platform/wiki/How-to-Rebase-a-Pull-Request
[github-issue]:    https://help.github.com/articles/closing-issues-via-commit-messages/
[mailing-list]:    https://dev.eclipse.org/mailman/listinfo/sisu-dev
