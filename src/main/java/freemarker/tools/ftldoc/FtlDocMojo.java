package freemarker.tools.ftldoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate-documentation")
public class FtlDocMojo extends AbstractMojo {

    @Parameter( property = "outputDirectory", defaultValue ="${project.build.directory}/ftldocs")
    private File outputDirectory;

    @Parameter( property = "templateDirectory")
    private File templateDirectory;

    @Parameter( property = "freemarkerFiles", required=true)
    private File[] freemarkerFiles;

    @Parameter( property = "freemarkerFileExtesion", defaultValue ="ftl")
    private String freemarkerFileExtension;

    @Parameter(property = "readmeFile", defaultValue = "readme.html")
    private File readmeFile;

    @Parameter(property = "title", defaultValue = "FtlDoc")
    private String title;

    @Parameter(property = "freemarkerVersion", defaultValue = "2.3.31")
    private String freemarkerVersion;

    @Override
    public void execute() throws MojoExecutionException {

        if (this.freemarkerFiles.length == 0) {
            this.getLog().error("Required parameter 'freemarkerFiles' is empty. Please fill it.");
            return;
        }
        List<File> ftlFiles = this.expandFiles(Arrays.asList(this.freemarkerFiles));
        
        this.getLog().info( "Will generate doc into " + this.outputDirectory);
        if (this.templateDirectory != null) {
            this.getLog().info("With templates from " + this.templateDirectory );
        }
        this.getLog().info("Readme files to process : " + this.readmeFile);
        this.getLog().info( "Files to process in: " + Arrays.asList(this.freemarkerFiles));
        if (this.freemarkerFileExtension != null) {
            this.getLog().info("Files filtered by extesion : " + this.freemarkerFileExtension);
        }
        this.outputDirectory.mkdirs();
        FtlDoc ftl = new FtlDoc(ftlFiles, this.outputDirectory, this.templateDirectory, this.readmeFile, this.title,
            this.freemarkerVersion);
        ftl.setLog(this.getLog());
        ftl.run();
        this.getLog().info( "Finished generating doc" );
    }
    
    private List<File>  expandFiles (List<File> paramFiles) {
        List<File> realFreemarkerFiles = new ArrayList<>();
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
