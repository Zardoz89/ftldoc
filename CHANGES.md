0.1.0-SNAPSHOT
- Adopting semantic versioning
- Using Maven logger
- Maven profile to launch tests using Groovy 3
- Minor changes to make more clean the code
- Simple categorizing of files, on the file list
- Support of repeated @ annotations like @author or @copyright

0.0.3

- Updated Freemaker to 2.3.31
- Added to @param an optional entry for type.
- Added support of JsDoc @param style annotations
- Added some basic tests using JUnit 5 + Spock 2
- Added maven configuration parameters to change :
    - Freemarker compatibility version
    - Title
    - Embed readme/welcome text
- Strong refactor of templates to adopt a modern style, without using
    framesets.

0.0.2

- Updated Freemarker from 2.3.17 to 2.3.26-incubating
- Added support for a @deprecated tag (with include reason/description)
- Fix encoding bug . Now outputs UTF-8 files always, and HTML files are set to the correct value.
- Default value for "outputDirectory" parameter
- "freemarkerFiles" marked as required
- "freemarkerFiles" now accepts directory paths and now can search freemarker files on these directory and his descendants.
- "freemarkerFileExtesion" with default value "ftl" to control the search of freemaker files on directories.
- More consistent indenting of default template files.
- Fixed @param with multiline description.
