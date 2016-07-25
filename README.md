# FTLDoc

Generates HTML documentation for FTL templates and macros.

* Original author: [chaquotay](https://github.com/chaquotay/ftldoc)
* Improvements: [nguillaumin](https://github.com/nguillaumin/ftldoc)
* Changed to a maven plugin: [msheppard](https://github.com/msheppard/ftldoc)

## Usage

You'll need to run `mvn install` on this project to install the plugin locally (or deploy it to your local nexus), and then add something like the following to the pom.xml for the project in which you want it to generate documentation.

```
...
    <build>
        <plugins>
...
            <plugin>
                <groupId>freemarker</groupId>
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
                            <templateDirectory>${project.basedir}/src/main/resources/templates</templateDirectory>
                            <outputDirectory>${project.build.directory}/documentation</outputDirectory>
                         </configuration>
                     </execution>
                 </executions>
             </plugin>
...
        </plugins>
    </build>
...

```

## Comment syntax

The comments to process must start with a `<#---` tag (3 dashes). This is to mimic the Javadoc behaviour where a `/*` is a standard comment, but `/**` is a Javadoc comment.

### Macro comments

The first sentence of the comment (until the first dot) will be used a short description in the summary table.

Macro parameters should be indicated using `@param <name> <description>`.

HTML is permitted within comments.

Example:

```
<#---
	Does fancy stuff.
	
	<p>And does it well !</p>
	
	@param fist The first parameter.
	@param second The second parameter, a <code>boolean</code>.
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

The generated doco is based on FreeMarker templates. There is a default set of templates provided but you can use your own.
To do so, use the `-tpl </path/to/tpl/folder` option. The folder must contains the following files:

* `file.ftl` : Used for a single `.ftl` file documentation.
* `index.ftl` : Index page (frameset).
* `index-all-cat.ftl` : Index of categories.
* `index-all-alpha.ftl` : Alphabetical index.
* `overview.ftl` : Overview (list of documented `.ftl` libraries).
* `filelist.ftl` : List of documented `.ftl` files (Left side of the frameset). 
