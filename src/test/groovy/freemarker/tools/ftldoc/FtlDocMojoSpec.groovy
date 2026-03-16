/**
 * FtlDocMojoSpec.groovy
 */
package freemarker.tools.ftldoc

import org.apache.maven.plugin.logging.Log
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for FtlDocMojo class
 */
class FtlDocMojoSpec extends Specification {

    def "Empty freemarkerFiles logs error and returns"() {
        given:
        def mojo = new FtlDocMojo()
        mojo.freemarkerFiles = []
        def mockLog = Mock(Log)
        
        // Use Groovy metaproxy to override the getLog method on AbstractMojo
        def mojoClass = mojo.getClass()
        def logField = mojoClass.getSuperclass().getDeclaredField("log")
        logField.setAccessible(true)
        logField.set(mojo, mockLog)

        when:
        mojo.execute()

        then:
        1 * mockLog.error("Required parameter 'freemarkerFiles' is empty. Please fill it.")
        0 * mockLog.info(_)
    }

    def "Expand files with extension #extension filters correctly"() {
        given:
        def mojo = new FtlDocMojo()
        def testDir = new File(System.getProperty("java.io.tmpdir"), "FtlDocMojoSpec-${System.nanoTime()}")
        testDir.mkdirs()
        
        def ftlFile = new File(testDir, "test.ftl")
        ftlFile.write("<#--- test -->")
        
        def txtFile = new File(testDir, "test.txt")
        txtFile.write("text")
        
        def otherFile = new File(testDir, "test.other")
        otherFile.write("other")
        
        mojo.freemarkerFiles = [testDir]
        mojo.freemarkerFileExtension = extension

        when:
        def result = mojo.expandFiles(mojo.freemarkerFiles.toList())

        then:
        result.size() == expectedCount

        cleanup:
        testDir.deleteDir()

        where:
        extension | expectedCount
        "ftl"    | 1
        "txt"    | 1
        "other"  | 1
        null     | 3
        "ftlx"   | 0
    }

    def "Expand files recursively from nested directories"() {
        given:
        def mojo = new FtlDocMojo()
        def testDir = new File(System.getProperty("java.io.tmpdir"), "FtlDocMojoSpecRec-${System.nanoTime()}")
        testDir.mkdirs()
        
        def rootFile = new File(testDir, "root.ftl")
        rootFile.write("<#--- root -->")
        
        def subdir = new File(testDir, "subdir")
        subdir.mkdirs()
        def subFile = new File(subdir, "sub.ftl")
        subFile.write("<#--- sub -->")
        
        def nested = new File(subdir, "nested")
        nested.mkdirs()
        def nestedFile = new File(nested, "nested.ftl")
        nestedFile.write("<#--- nested -->")
        
        def txtFile = new File(nested, "should_filter.txt")
        txtFile.write("text")
        
        mojo.freemarkerFiles = [testDir]
        mojo.freemarkerFileExtension = "ftl"

        when:
        def result = mojo.expandFiles(mojo.freemarkerFiles.toList())

        then:
        result.size() == 3
        result.any { it.name == "root.ftl" }
        result.any { it.name == "sub.ftl" }
        result.any { it.name == "nested.ftl" }

        cleanup:
        testDir.deleteDir()
    }

    def "Expand files from directory without subdirectories"() {
        given:
        def mojo = new FtlDocMojo()
        def testDir = new File(System.getProperty("java.io.tmpdir"), "FtlDocMojoSpecSimple-${System.nanoTime()}")
        testDir.mkdirs()
        
        def file1 = new File(testDir, "file1.ftl")
        file1.write("<#--- test 1 -->")
        
        def file2 = new File(testDir, "file2.ftl")
        file2.write("<#--- test 2 -->")
        
        mojo.freemarkerFiles = [testDir]
        mojo.freemarkerFileExtension = "ftl"

        when:
        def result = mojo.expandFiles(mojo.freemarkerFiles.toList())

        then:
        result.size() == 2

        cleanup:
        testDir.deleteDir()
    }

    def "Expand files with single file input"() {
        given:
        def mojo = new FtlDocMojo()
        def testFile = new File(System.getProperty("java.io.tmpdir"), "single-${System.nanoTime()}.ftl")
        testFile.write("<#--- single file -->")
        
        mojo.freemarkerFiles = [testFile]
        mojo.freemarkerFileExtension = "ftl"

        when:
        def result = mojo.expandFiles(mojo.freemarkerFiles.toList())

        then:
        result.size() == 1
        result[0].name == testFile.name

        cleanup:
        testFile.delete()
    }

    def "Expand files excludes non-matching extensions"() {
        given:
        def mojo = new FtlDocMojo()
        def testDir = new File(System.getProperty("java.io.tmpdir"), "FtlDocMojoSpecFilter-${System.nanoTime()}")
        testDir.mkdirs()
        
        def ftlFile = new File(testDir, "test.ftl")
        ftlFile.write("<#--- ftl -->")
        
        def htmlFile = new File(testDir, "test.html")
        htmlFile.write("<html></html>")
        
        def xmlFile = new File(testDir, "test.xml")
        xmlFile.write("<xml/>")
        
        mojo.freemarkerFiles = [testDir]
        mojo.freemarkerFileExtension = "ftl"

        when:
        def result = mojo.expandFiles(mojo.freemarkerFiles.toList())

        then:
        result.size() == 1
        result[0].name == "test.ftl"

        cleanup:
        testDir.deleteDir()
    }
    
    def "Duplicate files are filtered out in FtlDocMojo"() {
        given: "A FtlDocMojo instance"
        def mojo = new FtlDocMojo()
        
        and: "A list with duplicate files"
        def testFile = getFileResource("test/simple_test.ftl")
        def duplicateFiles = [testFile, testFile, testFile]
        
        when: "We call expandFiles method via reflection"
        def method = FtlDocMojo.class.getDeclaredMethod("expandFiles", List.class)
        method.setAccessible(true)
        def result = method.invoke(mojo, duplicateFiles)
        
        then: "Duplicates are removed"
        result.size() == 1
        result[0] == testFile
    }

    private File getFileResource(path) {
        return new File(getClass().getClassLoader().getResource(path).toURI())
    }
}
