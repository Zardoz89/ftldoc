/**
 * ParseFtlDocCommentSpec.groovy
 */
package freemarker.tools.ftldoc

import freemarker.template.SimpleHash
import freemarker.template.SimpleSequence
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for ParseFtlDocComment class
 */
class ParseFtlDocCommentSpec extends Specification {

    def "Parsing a empty comment"() {
        given:
        def emptyString = ""

        when:
        def output = ParseFtlDocComment.parse(emptyString)

        then:
        output != null
        output.isEmpty()
    }

    @Unroll
    def "Parsing #comment"() {
        given:
        def fullComment = "-" + comment

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("short_comment") != null
        output.get("comment") != null
        def params = output.get("@param") as SimpleSequence
        def param = params.get(0) as SimpleHash
        param.get("name").toString() == name
        param.get("type").toList() == type
        param.get("description").toString() == description

        where:
        comment                                                             || name             | type          | description
        "@param arg Description"                                            || "arg"            | []            | "Description"
        "          @param arg Description"                                  || "arg"            | []            | "Description"
        "@param    arg Description"                                         || "arg"            | []            | "Description"
        "@param arg        Description"                                     || "arg"            | []            | "Description"
        "@param arg2 Ludo ergo sum"                                         || "arg2"           | []            | "Ludo ergo sum"
        "@param arg2 {String} Ludo ergo sum"                                || "arg2"           | ["String"]    | "Ludo ergo sum"
    }

    @Unroll
    def "Parsing jsdoc #jsdoccomment"() {
        given:
        def fullComment = "-" + jsdoccomment

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("short_comment") != null
        output.get("comment") != null
        def params = output.get("@param") as SimpleSequence
        def param = params.get(0) as SimpleHash
        param.get("name").toString() == name
        param.get("description").toString() == description
        param.get("type").toList() == type
        param.get("optional").getAsBoolean() == optional
        Objects.toString(param.get("def_val"), null) == defValue

        where:
        jsdoccomment                                    || name     | type                      | optional  | defValue  | description
        "@param {TypeExp} arg3 - Bla bla bla"           || "arg3"   | ["TypeExp"]               | false     | null      | "Bla bla bla"
        "@param {String|Number} arg3 Bla bla bla"       || "arg3"   | ["String", "Number"]      | false     | null      | "Bla bla bla"
        "@param {Number} [arg] Bla bla bla"             || "arg"    | ["Number"]                | true      | null      | "Bla bla bla"
        "@param {Number} [arg=1] Bla bla bla"           || "arg"    | ["Number"]                | true      | "1"       | "Bla bla bla"
        '@param {String} [arg=""] Bla bla bla'          || "arg"    | ["String"]                | true      | '""'      | "Bla bla bla"
        '@param {String} [arg=" "] Bla bla bla'         || "arg"    | ["String"]                | true      | '" "'     | "Bla bla bla"
        "@param [arg] Bla bla bla"                      || "arg"    | []                        | true      | null      | "Bla bla bla"
        "@param [arg=1] Bla bla bla"                    || "arg"    | []                        | true      | "1"       | "Bla bla bla"
        "@param {Hash<String,String>} map - Bla"        || "map"    | ["Hash<String,String>"]   | false     | null      | "Bla"
    }

    @Unroll
    def "Parsing #keywordComment"() {
        given:
        def fullComment = "-" + keywordComment

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("short_comment") != null
        output.get("comment") != null
        output.get("@" + keyword) == text

        where:
        keywordComment                                                      || keyword          | text
        "@deprecated Because yes"                                           || "deprecated"     | "Because yes"
        "   @return     Fulano"                                             || "return"         | "Fulano"
    }

    @Unroll
    def "Parsing repeating #keywordComment"() {
        given:
        def fullComment = "-" + keywordComment

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("short_comment") != null
        output.get("comment") != null
        output.get("@" + keyword) == [text]

        where:
        keywordComment                                                      || keyword          | text
        "@author Fulano"                                                    || "author"         | "Fulano"
        "   @author     Fulano"                                             || "author"         | "Fulano"
        "@copyright 2021 Mocosoft Inc."                                     || "copyright"      | "2021 Mocosoft Inc."
    }

    def "Multiple @author annotations are stored in a list"() {
        given:
        def fullComment = """-
-- This is a test macro.
--
-- @author Foo
-- @author Bar
-- @author Baz
"""

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("@author") instanceof List
        def authors = output.get("@author") as List
        authors.size() == 3
        authors == ["Foo", "Bar", "Baz"]
    }


    def "Mixed @param, @keyword and alone comments"() {
        given:
        def fullComment = """-
-- This is a function.
--
-- Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna
-- aliqua.
--
-- @author Someone
-- @author Other guy
--
-- @param arg Long description
--      that continues on another line
-- @return something
"""

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("short_comment").toString() == " This is a function."
        output.get("comment").toString() == " This is a function. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
        output.get("@author") == ["Someone", "Other guy"]
        def params = output.get("@param") as SimpleSequence
        def param = params.get(0) as SimpleHash
        param.get("name").toString() == "arg"
        param.get("description").toString() == "Long description      that continues on another line\n"
    }

    def "Parsing null comment returns empty map"() {
        when:
        def output = ParseFtlDocComment.parse(null)

        then:
        output != null
        output.isEmpty()
    }

    def "Parsing comment with only single dash returns empty map"() {
        when:
        def output = ParseFtlDocComment.parse("-")

        then:
        output != null
        output.isEmpty()
    }

    def "Parsing comment that does not start with dash"() {
        given:
        def comment = "This is a comment without dash prefix"

        when:
        def output = ParseFtlDocComment.parse(comment)

        then:
        output != null
        output.get("comment") != null
        output.get("comment").toString().contains("comment without dash prefix")
    }

    @Unroll
    def "Parsing complex types #typeExpression"() {
        given:
        def fullComment = "- @param {" + typeExpression + "} arg Description"

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        def params = output.get("@param") as SimpleSequence
        def param = params.get(0) as SimpleHash
        param.get("name").toString() == "arg"
        param.get("description").toString() == "Description"
        param.get("type").toList() == expectedTypes

        where:
        typeExpression                         || expectedTypes
        "Hash<String,List<Object>>"             || ["Hash<String,List<Object>>"]
        "Map<String,ArrayList<Integer>>"        || ["Map<String,ArrayList<Integer>>"]
        "List<HashMap<String,Object>>"         || ["List<HashMap<String,Object>>"]
    }

    def "Parsing @param with empty description"() {
        given:
        def fullComment = "- @param {String} arg"

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        def params = output.get("@param") as SimpleSequence
        def param = params.get(0) as SimpleHash
        param.get("name").toString() == "arg"
        param.get("type").toList() == ["String"]
        param.get("description").toString() == ""
    }

    def "Parsing comment with only dash returns empty map"() {
        when:
        def output = ParseFtlDocComment.parse("-")

        then:
        output != null
        output.isEmpty()
    }

    @Unroll
    def "Parsing @ftlvariable #comment"() {
        given:
        def fullComment = "-" + comment

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        def ftlvariables = output.get("@ftlvariable") as List
        ftlvariables.size() == 1
        ftlvariables[0].get("name") == name
        ftlvariables[0].get("type") == type
        ftlvariables[0].get("file") == file

        where:
        comment                                                              || name     | type              | file
        '@ftlvariable name="foo" type="java.lang.String"'                    || "foo"    | "java.lang.String" | null
        '@ftlvariable name="bar" type="int"'                                 || "bar"    | "int"             | null
        '@ftlvariable name="user" type="com.example.User" file="path.ftl"' || "user"   | "com.example.User" | "path.ftl"
    }

    def "Parsing multiple @ftlvariable annotations"() {
        given:
        def fullComment = """-
-- @ftlvariable name="foo" type="String"
-- @ftlvariable name="bar" type="int"
"""

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        def ftlvariables = output.get("@ftlvariable") as List
        ftlvariables.size() == 2
        ftlvariables[0].get("name") == "foo"
        ftlvariables[0].get("type") == "String"
        ftlvariables[1].get("name") == "bar"
        ftlvariables[1].get("type") == "int"
    }

    @Unroll
    def "Parsing @ftlroot #comment"() {
        given:
        def fullComment = "-" + comment

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("@ftlroot") == path

        where:
        comment                                     || path
        '@ftlroot "path/to/root"'                  || "path/to/root"
        '@ftlroot "path/to.jar!/path/inside/jar"'   || "path/to.jar!/path/inside/jar"
    }

    def "Parsing @ftlvariable combined with regular doc comment"() {
        given:
        def fullComment = """-
-- My function description.
-- @param x The input value
-- @ftlvariable name="user" type="com.example.User"
"""

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("comment").toString().contains("My function description.")
        def params = output.get("@param") as SimpleSequence
        params.size() == 1
        def ftlvariables = output.get("@ftlvariable") as List
        ftlvariables.size() == 1
        ftlvariables[0].get("name") == "user"
        ftlvariables[0].get("type") == "com.example.User"
    }
}
