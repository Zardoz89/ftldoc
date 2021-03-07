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
        param.get("description").toString() == description

        where:
        comment                                                             || name             | description
        "@param arg Description"                                            || "arg"            | "Description"
        "          @param arg Description"                                  || "arg"            | "Description"
        "@param    arg Description"                                         || "arg"            | "Description"
        "@param arg        Description"                                     || "arg"            | "Description"
        "@param arg2 Ludo ergo sum"                                         || "arg2"           | "Ludo ergo sum"
        "@param {TypeExp} arg3 - Bla bla bla"                               || "arg3"           | "Bla bla bla"
        "@param {String|Number} arg3 Bla bla bla"                           || "arg3"           | "Bla bla bla"
        "@param {Number} [arg=1] Bla bla bla"                               || "arg"            | "Bla bla bla"
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