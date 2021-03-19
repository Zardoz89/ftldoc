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
        output.get("@" + keyword).toString()== text

        where:
        keywordComment                                                      || keyword          | text
        "@author Fulano"                                                    || "author"         | "Fulano"
        "   @author     Fulano"                                             || "author"         | "Fulano"
        "@copyright 2021 Mocosoft Inc."                                     || "copyright"      | "2021 Mocosoft Inc."
    }

    def "Multiline @param"() {
        given:
        def fullComment = """-
    @param arg Long description
        that continues on another line
"""

        when:
        def output = ParseFtlDocComment.parse(fullComment)

        then:
        !(output.isEmpty())
        output.get("short_comment") != null
        output.get("comment") != null
        def params = output.get("@param") as SimpleSequence
        def param = params.get(0) as SimpleHash
        param.get("name").toString() == "arg"
        param.get("description").toString() == "Long description that continues on another line\n"
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
        output.get("@author").toString()== "Someone"
        def params = output.get("@param") as SimpleSequence
        def param = params.get(0) as SimpleHash
        param.get("name").toString() == "arg"
        param.get("description").toString() == "Long description      that continues on another line\n"
    }
}