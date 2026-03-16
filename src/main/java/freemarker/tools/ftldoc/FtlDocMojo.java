package freemarker.tools.ftldoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate-documentation", threadSafe = true)
@SuppressWarnings("deprecation")
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

    @Parameter(property = "hidePrivateMacrosAndFunctions", defaultValue = "false")
    private boolean hidePrivateMacrosAndFunctions;

    @Parameter(property = "privatePrefix", defaultValue = "_")
    private String privatePrefix;

    @Override
    public void execute() throws MojoExecutionException {

        if (this.freemarkerFiles.length == 0) {
            this.getLog().error("Required parameter 'freemarkerFiles' is empty. Please fill it.");
            return;
        }
        List<File> ftlFiles = new ArrayList<>(this.expandFiles(Arrays.asList(this.freemarkerFiles)));
        
        this.getLog().info( "Will generate doc into " + this.outputDirectory);
        if (this.templateDirectory != null) {
            this.getLog().info("With templates from " + this.templateDirectory );
        }
        this.getLog().info("Readme files to process : " + this.readmeFile);
        this.getLog().info( "Files to process in: " + Arrays.asList(this.freemarkerFiles));
        if (this.freemarkerFileExtension != null) {
            this.getLog().info("Files filtered by extension : " + this.freemarkerFileExtension);
        }
        this.outputDirectory.mkdirs();
        FtlDoc ftl = new FtlDoc(ftlFiles, this.outputDirectory, this.templateDirectory, this.readmeFile, this.title,
            this.freemarkerVersion, this.hidePrivateMacrosAndFunctions, this.privatePrefix);
        ftl.setLog(this.getLog());
        ftl.run();
        this.getLog().info( "Finished generating doc" );
    }
    
    private Collection<File> expandFiles(List<File> paramFiles) {
        List<File> files = new ArrayList<>();
        
        for (File f : paramFiles) {
            if (f.isFile()) {
                if (this.freemarkerFileExtension == null || f.getName().endsWith(this.freemarkerFileExtension)) {
                    files.add(f);
                }
            } else if (f.isDirectory()) {
                files.addAll(this.expandFiles(Arrays.asList(f.listFiles())));
            }
        }
        
        Set<File> uniqueFiles = new LinkedHashSet<>(files);
        if (uniqueFiles.size() < files.size()) {
            this.getLog().warn("Duplicate file(s) found");
        }
        
        return uniqueFiles;
    }
}
