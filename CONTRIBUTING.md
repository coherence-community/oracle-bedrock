Contributing to Oracle Bedrock
==============================

The following sections outline the requirements for making contributions to
the Oracle Bedrock.

<h4>Overview</h4>

Developing code and documentation for the Oracle Bedrock is a **big deal**.

While being open may seemingly make it a place to develop and possibly experiment with
patterns or solutions, the fact of the matter is that the Oracle and the wider Java Community
frequently adopt the styles, approaches and implementations provided by Oracle Bedrock,
whether in total or in part, for use in production scenarios.

Consequently every effort has to be made to ensure that projects are somewhat
stable, not too experimental and offer a high-degree of reliability, scalability
and stability, something that people can trust if they decide to branch or 
become inspired by said projects on their own.

Oracle Bedrock tends not to be a "holding zone" for individually or corporately
developed utilities that developers would like to share.   While these may be of
interest to individuals, we carefully investigate utility of these solutions
before considering them for inclusion.  In most cases we instead encourage
developers to define their own "common" projects.

<h4>Becoming a Contributor</h4>

Contributing to Oracle Bedrock can be done in various ways, each of which is
valuable to the Oracle and Java Communities as a whole.  Contributions may
include helping out by answering questions on the forums, isolating and submitting 
defects resolution requests, proposing or submitting defect resolutions (fixes), 
suggesting or building enhancements, introducing new features and improving tests 
and documentation.

For all contributions that involve making a change to the source tree and thus
releasing a new build of Oracle Bedrock, those primarily being a defect fix,
enhancement, new feature or documentation improvement, all non-Oracle contributors
must complete and sign the [Oracle Contributors Agreement](http://oss.oracle.com/oca.pdf).

To do this, simply print out the form, fill in the necessary details, scan it 
in and return via email to: oracle-ca_us [at] oracle [dot] com. 

**Note 1:** For the "Project Name:" please write "Oracle Tools/Bedrock".

**Note 2:** This is the same agreement used for making contributions to GlassFish,
The Oracle Coherence Incubator and Java itself.  If you already have executed this
agreement then you're ready to contribute to Oracle Bedrock.

Should you have any questions regarding this agreement, you should consult the
[Oracle Contributors Agreement FAQ](http://www.oracle.com/technetwork/oca-faq-405384.pdf)

<h4>Roles and Responsibilities</h4>

As mentioned above, there are quite a few ways to participate on projects that
are part of Oracle Bedrock, and not all of them involve contributing source code!
Simply using the software, participating on mailing lists or forums, filing bug reports
or enhancement requests are an incredibly valuable form of participation.

If we were to break down the forms of participation for Oracle Bedrock projects
into a set of roles, the result would look something like this: 

    Users, Contributors, Committers, Maintainers, and Project Leads.

**Users:**

Users are the people who use the software. Users are using the software, reporting 
bugs, making feature requests and suggestions. This is by far the most important 
category of people. Without users, there is no reason for the project.

*How to become one:* Download or use Maven et al to add the software as a dependency
for your application and start to use it.

**Contributors:**

Contributors are individuals who contribute to an Oracle Bedrock project,
but do not have write access to the source tree. Contributions can be in the form of 
pull-requests, source code patches, new code, or bug reports, but could also include
distributed documentation like examples, tutorials, javadoc, articles, FAQs, or
screenshots.

A contributor who has provided in solid, useful content/source code on a project
can be elevated to Committer status by a Maintainer.

Integration of a Contributor submissions done at the discretion of a Maintainer, 
but this is an iterative, communicative process. Note that for code to be 
integrated, a completed Oracle Contribution Agreement is required from 
each contributor.

*How to become one:* Create a pull-request for content as mentioned above.

**Committers:**

Committers are Contributors that have had contributions (pull-requests) accepted
for inclusion in the project.

*How to become one:* Participate in a review with a Maintainer for a contribution
you've made and have that contribution included in the project.

**Maintainers:**

Each module has one or more Maintainers, who have commit/pull-request accepting
permissions (either for that module or globally), and "manages" a group of Committers.

They are responsible for reviewing and merging contributions.  Essentially Maintainers
are responsible for quality and making sure that these contributions do not break the build.

Maintainers are also responsible for checking that everyone who contributes
code has submitted an Oracle Contribution Agreement.

*How to become one:* 

- Start a module (you need to have written some working code on your project 
  to do this, you'll also need to talk to the Project Lead).

- Have the module reviewed and accepted as a contribution.

- Have responsibility for that module handed over to you from the current Maintainer.

- Alternatively take over an abandoned project--sometimes someone starts something, but for
  one reason or another can't continue to work on it. If it's interesting to you, 
  volunteer!

**Project Lead:**

Each project in Oracle Bedrock has an overall Project Lead.

The Project Leads are currently appointed by Oracle. They are responsible for 
managing the entire project, helping to create policies by consensus that 
ensure global quality.

<h4>Making a Contribution</h4>

Contributions are only accepted by Committers, including for documentation.

To become a Committer you must first clearly demonstrate both 
skill as a developer and be capable of strictly adhering to the quality
and architectural requirements of Oracle Bedrock.

In order to demonstrate these abilities, it's best to get started by contributing patches
or improvements via pull-requests and then asking the Maintainer(s) or Project Lead(s) to
review said changes, after which they may be accepted and committed.

Like all large projects, Oracle Bedrock employs strict coding
guidelines.  For the most part these are easily automated using the preferred 
tool called [JIndent](http://www.jindent.com).  A JIndent style-template for
Oracle Bedrock is located in the documents folder of the source tree.

**Note:** Oracle does not supply licenses for JIndent.