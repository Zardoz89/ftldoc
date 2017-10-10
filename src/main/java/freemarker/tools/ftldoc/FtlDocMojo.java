package freemarker.tools.ftldoc;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate-documentation")
public class FtlDocMojo extends AbstractMojo {

    @Parameter( property = "outputDirectory", required=true)
    private File outputDirectory;

    @Parameter( property = "templateDirectory")
    private File templateDirectory;

    @Parameter( property = "freemarkerFiles", required=true)
    private File[] freemarkerFiles;

    @Parameter( property = "freemarkerFileExtesion")
    private String freemarkerFileExtension;

    public void execute() throws MojoExecutionException {
        if (this.freemarkerFiles.length == 0) {
            getLog().error("Required parameter 'freemarkerFiles' is empty. Please fill it.");
            return;
        }
        List<File> ftlFiles = this.expandFiles(Arrays.asList(freemarkerFiles));
        
        getLog().info( "Will generate doc into " + outputDirectory);
        if (this.templateDirectory != null) {
            getLog().info("With templates from " + templateDirectory );
        }
        getLog().info( "Files to process in: " + Arrays.asList(freemarkerFiles));
        if (this.freemarkerFileExtension != null) {
            getLog().info("Files filtered by extesion : " + this.freemarkerFileExtension);
        }
        outputDirectory.mkdirs();
        FtlDoc ftl = new FtlDoc(ftlFiles, outputDirectory, templateDirectory);
        ftl.run();
        getLog().info( "Finished generating doc" );
    }

    
    private List<File>  expandFiles (List<File> paramFiles) {
        List<File> realFreemarkerFiles = new ArrayList<File>();
        for (File f : paramFiles) {
            if (f.isFile()) {
                if (this.freemarkerFileExtension == null 
                        || f.getName().endsWith(this.freemarkerFileExtension)) {
                    realFreemarkerFiles.add (f);
                }
            } else if (f.isDirectory()) {
                realFreemarkerFiles.addAll(this.expandFiles( Arrays.asList(f.listFiles())));
            }
        }
        return realFreemarkerFiles;
    }
}
