# FTLDoc

Generates HTML documentation for FTL templates and macros.

* Original author: [chaquotay](https://github.com/chaquotay/ftldoc)
* Improvements: [nguillaumin](https://github.com/nguillaumin/ftldoc)
* Changed to a maven plugin: [msheppard](https://github.com/msheppard/ftldoc)
* Improvements, redo templates, add some test code and support for types and default values: [zardoz](https://github.com/Zardoz89)

License : [MIT](LICENSE.md)

[Changelog](CHANGES)

Examples :
* [Simple test](examples/simple_test/index.html)
* [Own default templates](examples/default_templates/index.html)

## Usage

You'll need to run `mvn install` on this project to install the plugin locally (or deploy it to your local nexus), and then add something like the following to the pom.xml for the project in which you want it to generate documentation.

```xml
...
    <build>
        <plugins>
...
            <plugin>
                <groupId>io.github.zardoz89</groupId>
                <artifactId>ftldoc-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>generate-documentation</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>generate-documentation</goal>
                        </goals>
                        <configuration>
                            <freemarkerFiles>
                                <freemarkerFile>${project.build.directory}/src/main/resources/example1.ftl</freemarkerFile>
                                <freemarkerFile>${project.build.directory}/src/main/resources/example2.ftl</freemarkerFile>
                            </freemarkerFiles>
                            <outputDirectory>${project.build.directory}/documentation</outputDirectory>
                            <title>FtlDoc</title>
                            <readmeFile>${project.build.directory}/src/main/resources/readme.html</readmeFile>
                            <freemarkerVersion>2.3.31</freemarkerVersion>
                            <templateDirectory>${project.basedir}/src/main/resources/templates</templateDirectory>
                            <freemarkerFileExtesion>ftl</freemarkerFileExtesion>
                         </configuration>
                     </execution>
                 </executions>
             </plugin>
...
        </plugins>
    </build>
...

```

Where :

* freemarkerFiles : A list of paths to FTL files or directories containing FTL files that will be parsed to generate the documentation.
* outputDirectory : Output directoty. By default it's : ${project.build.directory}/ftldocs
* title : Title of the documentation generated. By default it's FtlDoc
* readmeFile : A path to a HTML file to be embed inside of the output index.html
* freemarkerVersion : Version of [freemarker compatibility](https://freemarker.apache.org/docs/pgui_config_incompatible_improvements.html). By default it's 2.3.31
* templateDirectory : A path where to use custom freemarker templates to generate the documentation.
* freemarkerFileExtesion : Freemarker files extensión. By default it's "ftl"

### Usage as CLI

Example:
```bash
mvn io.github.zardoz89:ftldoc-maven-plugin:0.1.0-SNAPSHOT:generate-documentation -DoutputDirectory=./outputdir/  -DfreemarkerFiles=./src/main/webapp/templates/webftl/lib/auxiliar_functions.ftl
```

## Comment syntax

The comments to process must start with a `<#---` tag (3 dashes). This is to mimic the Javadoc behaviour where a `/*` is a standard comment, but `/**` is a Javadoc comment.

### Macro comments

The first sentence of the comment (until the first dot) will be used a short description in the summary table.

Macro parameters should be indicated using :

Old style :
 * `@param <name> <description>`.
 * `@param <name> {Type} <description>`.

JsDoc style :
 * `@param {Type} <name> <description>`.
 * `@param {Type} [<name>] <description>`.
 * `@param {Type} [<name>=DefaultValue] <description>`.

HTML is permitted within comments.

Example:

```xml
<#---
    Does fancy stuff.

    <p>And does it well !</p>

    @param fist The first parameter.
    @param {Boolean} [second=false] The second parameter, a <code>boolean</code>.
-->
<#macro MyMacro first="" second=false>
    ...
<#/macro>
```

You can use any `@` tags you want such as `@author`, or `@mytag`. These tags will be parsed and available in the template on the `macro` or `comment` objects (i.e. `macro.@author` ...).

### Global comment

A global comment for a given `.ftl` file can be written at the top of the file. The first comment found that isn't followed by a `<#macro />` is considered the global comment.

### Categories

Macro can be put in categories. To embed a group of macros in a category, use the `@begin` and `@end` tags.

```
<#-- @begin Menu handling -->

<#---
    ...
-->
<#macro MainMenu> ... </#macro>

<#---
    ...
-->
<#macro SubMenu> ... </#macro>

<#-- @end -->
```

## Custom templates

The generated documentation is based on FreeMarker templates. There is a default set of templates provided but you can use your own.
To do so, use the `-DtemplateDirectory </path/to/tpl/folder` option. The folder must contains the following files:

* `file.ftl` : Used for a single `.ftl` file documentation.
* `index.ftl` : Index/Welcome page
* `index-all-cat.ftl` : Index of categories.
* `index-all-alpha.ftl` : Alphabetical index.
