/**
 * FtlDocSpec.groovy
 */
package freemarker.tools.ftldoc

import spock.lang.Specification

/**
 * Unit tests for FtlDoc class
 */
class FtlDocSpec extends Specification {

    final temporalFolderString = new File(System.getProperty("java.io.tmpdir"))
    final outputFolder = new File(temporalFolderString, "/FtlDocSpec/")

    def setup() {
        outputFolder.mkdirs()
    }

    def cleanup() {
        outputFolder.delete()
    }

    def "Generating output files"() {
        given: "A simple FTL file with FTLDoc markups"
        List<File> files = [
            this.getFileResource("test/simple_test.ftl"),
            this.getFileResource("test/lib/test_lib.ftl")
        ]
        def ftlDoc = new FtlDoc(files, outputFolder, null, this.getFileResource("test/readme.html"), "FtlDoc test", "2.3.26")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Generates the expected output files on the output folder"
        def outputFiles = outputFolder.list()
        outputFiles.contains("index.html")
        outputFiles.contains("simple_test.ftl.html")
        outputFiles.contains("test_lib.ftl.html")
        outputFiles.contains("index-all-alpha.html")
        outputFiles.contains("index-all-cat.html")

        (new File(outputFolder, "index.html")).text == getFileResource("expected/index.html").text
        (new File(outputFolder, "simple_test.ftl.html")).text == getFileResource("expected/simple_test.ftl.html").text
        (new File(outputFolder, "test_lib.ftl.html")).text == getFileResource("expected/test_lib.ftl.html").text
    }

    private File getFileResource(path) {
        return new File(getClass().getClassLoader().getResource(path).toURI())
    }
}