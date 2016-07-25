package freemarker.tools.ftldoc;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate-documentation")
public class FtlDocMojo extends AbstractMojo {

    @Parameter( property = "outputDirectory")
    private File outputDirectory;

    @Parameter( property = "templateDirectory")
    private File templateDirectory;

    @Parameter( property = "freemarkerFiles")
    private File[] freemarkerFiles;

    public void execute() throws MojoExecutionException {
        getLog().info( "Will generate doc into " + outputDirectory + " based on templates from " + templateDirectory );
        getLog().info( "Files to process: " + Arrays.asList(freemarkerFiles));
        outputDirectory.mkdirs();
        FtlDoc ftl = new FtlDoc(Arrays.asList(freemarkerFiles), outputDirectory, templateDirectory);
        ftl.run();
        getLog().info( "Finished generating doc" );
    }

}
