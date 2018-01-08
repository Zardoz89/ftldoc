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
