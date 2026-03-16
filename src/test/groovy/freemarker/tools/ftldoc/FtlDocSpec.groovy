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

    def "Categories are correctly parsed from @begin/@end tags"() {
        given: "An FTL file with category tags"
        List<File> files = [getFileResource("test/categories/macros.ftl")]
        def outputDir = new File(temporalFolderString, "/FtlDocSpecCategories/")
        outputDir.mkdirs()
        
        def ftlDoc = new FtlDoc(files, outputDir, null, null, "Categories Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Generates category index file"
        new File(outputDir, "index-all-cat.html").exists()
        def catContent = new File(outputDir, "index-all-cat.html").text
        catContent.contains("Menu macros")
        catContent.contains("Form macros")

        cleanup:
        outputDir.delete()
    }

    def "Global file comment is correctly parsed"() {
        given: "An FTL file with global comment"
        List<File> files = [getFileResource("test/global_comment_test.ftl")]
        def outputDir = new File(temporalFolderString, "/FtlDocSpecGlobal/")
        outputDir.mkdirs()
        
        def ftlDoc = new FtlDoc(files, outputDir, null, null, "Global Comment Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Generates file with global comment"
        new File(outputDir, "global_comment_test.ftl.html").exists()
        def content = new File(outputDir, "global_comment_test.ftl.html").text
        content.contains("global_comment_test.ftl")
        content.contains("Global Author")
        content.contains("1.0")

        cleanup:
        outputDir.delete()
    }

    def "Multiple directories are correctly processed"() {
        given: "Files from multiple directories"
        List<File> files = [
            getFileResource("test/dir1/lib1.ftl"),
            getFileResource("test/dir2/lib2.ftl")
        ]
        def outputDir = new File(temporalFolderString, "/FtlDocSpecMultiDir/")
        outputDir.mkdirs()
        
        def ftlDoc = new FtlDoc(files, outputDir, null, null, "Multi Dir Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Generates output for all files"
        new File(outputDir, "lib1.ftl.html").exists()
        new File(outputDir, "lib2.ftl.html").exists()
        new File(outputDir, "index.html").exists()

        cleanup:
        outputDir.delete()
    }

    def "Custom template directory is used when provided"() {
        given: "Files and custom template directory"
        List<File> files = [getFileResource("test/simple_test.ftl")]
        def outputDir = new File(temporalFolderString, "/FtlDocSpecCustom/")
        outputDir.mkdirs()
        
        def templateDir = getFileResource("test/custom_templates")
        def ftlDoc = new FtlDoc(files, outputDir, templateDir, null, "Custom Template Test", "2.3.31")

        when: "We run FtlDoc parsing"
        ftlDoc.run()

        then: "Uses custom templates and generates custom output"
        new File(outputDir, "custom.css").exists()
        new File(outputDir, "simple_test.ftl.html").exists()
        new File(outputDir, "index.html").exists()
        def indexContent = new File(outputDir, "index.html").text
        indexContent.contains("Custom index template")

        cleanup:
        outputDir.delete()
    }

    def "Exception handling in createIndexPage does not throw when file is read-only"() {
        given: "An output directory with a read-only index.html"
        List<File> files = [getFileResource("test/simple_test.ftl")]
        def outputDir = new File(temporalFolderString, "/FtlDocSpecException/")
        outputDir.mkdirs()
        
        // Create a file that will cause issues when trying to overwrite
        def indexFile = new File(outputDir, "index.html")
        indexFile.createNewFile()
        indexFile.setReadOnly()
        
        def ftlDoc = new FtlDoc(files, outputDir, null, null, "Exception Test", "2.3.31")
        
        // Run to generate files first
        ftlDoc.run()
        
        when: "We try to generate index page on read-only file"
        // Use reflection to call private method
        def method = FtlDoc.class.getDeclaredMethod("createIndexPage")
        method.setAccessible(true)
        method.invoke(ftlDoc)

        then: "No exception is thrown - exceptions are silently caught (this is the current bug)"
        noExceptionThrown()
        // The file should exist from the first run
        indexFile.exists()

        cleanup:
        indexFile.setWritable(true)
        outputDir.delete()
    }

    def "Exception handling in createAllCatPage does not throw with invalid template"() {
        given: "An FtlDoc with no categories"
        def outputDir = new File(temporalFolderString, "/FtlDocSpecException2/")
        outputDir.mkdirs()
        
        def ftlDoc = new FtlDoc([], outputDir, null, null, "Exception Test", "2.3.31")
        // Set empty categories
        def categoriesField = FtlDoc.class.getDeclaredField("allCategories")
        categoriesField.setAccessible(true)
        categoriesField.set(ftlDoc, new java.util.TreeMap())

        when: "We call createAllCatPage"
        def method = FtlDoc.class.getDeclaredMethod("createAllCatPage")
        method.setAccessible(true)
        method.invoke(ftlDoc)

        then: "No exception is thrown - exceptions are silently caught (this is the current bug)"
        noExceptionThrown()

        cleanup:
        outputDir.delete()
    }

    private File getFileResource(path) {
        return new File(getClass().getClassLoader().getResource(path).toURI())
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
}