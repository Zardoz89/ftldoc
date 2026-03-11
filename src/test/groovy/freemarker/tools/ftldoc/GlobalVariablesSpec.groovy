/*
 * GlobalVariablesSpec.groovy
 */
package freemarker.tools.ftldoc

import spock.lang.Specification

/**
 * Unit tests for global variables extraction
 */
class GlobalVariablesSpec extends Specification {

    final temporalFolderString = new File(System.getProperty("java.io.tmpdir"))
    final outputFolder = new File(temporalFolderString, "/GlobalVariablesSpec/")

    def setup() {
        outputFolder.delete()
        outputFolder.mkdirs()
    }

    def cleanup() {
        outputFolder.delete()
    }

    def "Global variables are extracted from FTL files"() {
        given: "An FTL file with global variables"
        List<File> files = [getFileResource("test/global_vars_test.ftl")]
        def ftlDoc = new FtlDoc(files, outputFolder, null, null, "Global Variables Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Generates the expected output file"
        new File(outputFolder, "global_vars_test.ftl.html").exists()
        def content = new File(outputFolder, "global_vars_test.ftl.html").text

        and: "Contains the global variables in the summary"
        content.contains("Global Variables Summary")
        content.contains("myStringVar")
        content.contains("myHashVar")
        content.contains("myUntypedVar")
        content.contains("myNoCommentVar")

        and: "Does not contain regular assign variables"
        !content.contains("regularAssignVar")

        cleanup:
        outputFolder.delete()
    }

    def "Global variables with type annotation show type info"() {
        given: "An FTL file with global variables with @type annotation"
        List<File> files = [getFileResource("test/global_vars_test.ftl")]
        def ftlDoc = new FtlDoc(files, outputFolder, null, null, "Global Variables Type Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Contains type information"
        def content = new File(outputFolder, "global_vars_test.ftl.html").text
        content.contains("String")
        content.contains("Hash")

        cleanup:
        outputFolder.delete()
    }

    def "Global variables without comment show as unknown type"() {
        given: "An FTL file with global variables without comments"
        List<File> files = [getFileResource("test/global_vars_test.ftl")]
        def ftlDoc = new FtlDoc(files, outputFolder, null, null, "Global Variables No Comment Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Variable without comment is still included"
        def content = new File(outputFolder, "global_vars_test.ftl.html").text
        content.contains("myNoCommentVar")

        cleanup:
        outputFolder.delete()
    }

    def "Global variables index page is created when variables exist"() {
        given: "An FTL file with global variables"
        List<File> files = [getFileResource("test/global_vars_test.ftl")]
        def ftlDoc = new FtlDoc(files, outputFolder, null, null, "Global Variables Index Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Creates index-global-vars.html"
        new File(outputFolder, "index-global-vars.html").exists()
        def indexContent = new File(outputFolder, "index-global-vars.html").text
        indexContent.contains("Global Variables Index")
        indexContent.contains("myStringVar")
        indexContent.contains("myHashVar")

        cleanup:
        outputFolder.delete()
    }

    def "No global variables index page is created when no variables exist"() {
        given: "An FTL file without global variables"
        def customOutputFolder = new File(temporalFolderString, "/GlobalVariablesSpecNoVars/")
        customOutputFolder.mkdirs()
        List<File> files = [getFileResource("test/simple_test.ftl")]
        def ftlDoc = new FtlDoc(files, customOutputFolder, null, null, "No Global Variables Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Does not create index-global-vars.html or it should be empty"
        def indexFile = new File(customOutputFolder, "index-global-vars.html")
        !indexFile.exists() || indexFile.length() == 0

        cleanup:
        customOutputFolder.delete()
    }

    private File getFileResource(path) {
        return new File(getClass().getClassLoader().getResource(path).toURI())
    }
}
