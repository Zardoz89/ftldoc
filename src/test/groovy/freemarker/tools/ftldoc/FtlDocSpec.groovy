/**
 * FtlDocSpec.groovy
 */
package freemarker.tools.ftldoc

import spock.lang.Specification

/**
 * Unit tests for FtlDoc class
 */
class FtlDocSpec extends Specification {

    def "test"() {
        given:
        List<File> files = []
        def ftlDoc = new FtlDoc(files, new File("/tmp/"), null)

        when:
        System.out.println()

        then:
        ftlDoc != null
    }
}