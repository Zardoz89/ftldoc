/*
 * Templates.java
 */
package freemarker.tools.ftldoc;

/**
 * List of templates to be procesed to generated output documentation
 */
enum Templates
{
    file("file"),
    index("index"),
    indexAllCat("index-all-cat"),
    indexAllAlpha("index-all-alpha");

    private final String fileName;

    private Templates(String fileName)
    {
        this.fileName = fileName;
    }

    public String fileName()
    {
        return this.fileName + FtlDoc.EXT_FTL;
    }
}
