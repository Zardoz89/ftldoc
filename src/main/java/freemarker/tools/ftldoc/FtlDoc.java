/*
 * FtlDoc.java
 */
package freemarker.tools.ftldoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.tree.TreeNode;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.Comment;
import freemarker.core.Macro;
import freemarker.core.TemplateElement;
import freemarker.core.TextBlock;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;

/**
 * Main ftldoc class (includes command line tool).
 * 
 * @author Stephan Mueller - stephan at chaquotay dot net
 */
public class FtlDoc
{
    static final String EXT_FTL = ".ftl";
    private static final String OUTPUT_ENCODING = "UTF-8";

    private static final Comparator<Map<String, Object>> MACRO_COMPARATOR = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> lhs, Map<String, Object> rhs)
        {
            return lhs.get("name").toString().toLowerCase()
                .compareTo(rhs.get("name").toString().toLowerCase());
        }
    };

    private static final Comparator<File> FILE_COMPARATOR = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs)
        {
            return lhs.getName().compareTo(rhs.getName());
        }
    };

    private SortedMap<String, List<Map<String, Object>>> allCategories = null;
    private SortedMap<String, List<Map<String, Object>>> categories = null;
    private List<Map<String, Object>> allMacros = null;
    private List<Map<String, Object>> macros = null;
    private File outputDir;
    private List<File> sourceFiles;
    private List<Map<String, Object>> parsedFiles;
    private Set<File> allDirectorioes;
    private File alternartiveTemplatesFolder;
    private File readmeFile;
    private String title;
    private Version freemarkerVersion;

    List<CategoryRegion> regions = new LinkedList<>();

    private Configuration cfg = null;

    private Logger log = new Logger();

    public FtlDoc(List<File> sourceFiles, File outputDir, File altTemplatesFolder, File readmeFile, String title,
        String freemarkerVersionString)
    {
        this.outputDir = outputDir;
        this.sourceFiles = sourceFiles;
        this.alternartiveTemplatesFolder = altTemplatesFolder;
        this.readmeFile = readmeFile;
        this.title = title;
        this.freemarkerVersion = new Version(freemarkerVersionString);

        this.cfg = new Configuration(this.freemarkerVersion);
        this.cfg.setWhitespaceStripping(false);
        this.cfg.setOutputEncoding(OUTPUT_ENCODING);

        // extracting parent directories of all files
        this.allDirectorioes = new HashSet<>();
        for (File sourceFile : this.sourceFiles) {
            this.allDirectorioes.add(sourceFile.getParentFile());
        }
    }

    /**
     * Starts the ftldoc generation.
     */
    public void run()
    {
        try {
            // init global collections
            this.allCategories = new TreeMap<>();
            this.allMacros = new ArrayList<>();
            this.parsedFiles = new ArrayList<>();

            List<TemplateLoader> loaders = new ArrayList<>(this.allDirectorioes.size() + 1);
            // Loads documantation generation templates
            loaders.add(this.loadDocumentationTemplates());

            // add loader for every directory
            loaders.addAll(this.loadSourceTemplates());

            TemplateLoader[] loadersArray = loaders.toArray(new TemplateLoader[0]);
            TemplateLoader loader = new MultiTemplateLoader(loadersArray);
            this.cfg.setTemplateLoader(loader);

            // = create template for file page
            // Sort files
            Collections.sort(this.sourceFiles, FILE_COMPARATOR);

            // create file pages
            for (File element : this.sourceFiles) {
                this.createFilePage(element);
            }

            // sort categories
            for (List<Map<String, Object>> l : this.allCategories.values()) {
                Collections.sort(l, MACRO_COMPARATOR);
            }

            // create the rest
            this.createIndexPage();
            this.createAllCatPage();
            this.createAllAlphaPage();
            this.copyCssFiles();

        } catch (Exception ex) {
            this.log.error(ex);
        }
    }

    private TemplateLoader loadDocumentationTemplates()
        throws IOException
    {
        if (this.alternartiveTemplatesFolder != null) {
            return new FileTemplateLoader(this.alternartiveTemplatesFolder);
        }
        return new ClassTemplateLoader(this.getClass(), "/default");
    }

    private List<TemplateLoader> loadSourceTemplates()
        throws IOException
    {
        List<TemplateLoader> loaders = new ArrayList<>();
        for (File file : this.allDirectorioes) {
            loaders.add(new FileTemplateLoader(file));
        }
        return loaders;
    }

    private void createFilePage(File file)
    {
        try {
            File htmlFile = new File(this.outputDir, file.getName() + ".html");
            this.log.info("Generating " + htmlFile.getCanonicalFile() + "...");

            Template t_out = this.cfg.getTemplate(Templates.file.fileName());
            this.categories = new TreeMap<>();
            TemplateElement te = null;
            Comment globalComment = null;
            Template t = this.cfg.getTemplate(file.getName());
            this.macros = new ArrayList<>();
            Set<Comment> comments = new HashSet<>();
            Map ms = t.getMacros();

            this.createCategoryRegions(t);

            Iterator macroIter = ms.values().iterator();
            while (macroIter.hasNext()) {
                Macro macro = (Macro)macroIter.next();
                int k = macro.getParent().getIndex(macro);
                for (int j = k - 1; j >= 0; j--) {
                    te = (TemplateElement)macro.getParent().getChildAt(j);
                    if (te instanceof TextBlock) {
                        if (((TextBlock)te).getSource().trim().length() == 0) {
                            continue;
                        } else {
                            this.addMacro(this.createCommentedMacro(macro, null, file));
                            break;
                        }
                    } else if (te instanceof Comment) {
                        Comment c = (Comment)te;
                        comments.add(c);
                        if (c.getText().startsWith("-")) {
                            this.addMacro(this.createCommentedMacro(macro, c, file));
                            break;
                        }
                    } else {
                        this.addMacro(this.createCommentedMacro(macro, null, file));
                        break;
                    }
                }
            }

            te = t.getRootTreeNode();
            if (te.getClass().getName().endsWith("MixedContent")) {
                Enumeration children = te.children();
                while (children.hasMoreElements()) {
                    Object element = children.nextElement();
                    if (element instanceof Comment) {
                        Comment candidate = (Comment)element;
                        if (candidate.getText().startsWith("-")) {
                            if (!comments.contains(candidate)) {
                                globalComment = candidate;
                            }
                            break;
                        }
                    }
                }
            }

            Collections.sort(this.macros, MACRO_COMPARATOR);
            for (List<Map<String, Object>> l : this.categories.values()) {
                Collections.sort(l, MACRO_COMPARATOR);
            }

            Map<String, Object> root = new HashMap<>();
            root.put("macros", this.macros);
            if (null != globalComment) {
                root.put("comment", this.parse(globalComment));
            } else {
                root.put("comment", new HashMap<>());
            }
            root.put("filename", t.getName());
            root.put("categories", this.categories);
            this.putGlobalVars(root);

            try (OutputStreamWriter outputStream = new OutputStreamWriter(
                new FileOutputStream(htmlFile), Charset.forName(OUTPUT_ENCODING).newEncoder())) {
                t_out.process(root, outputStream);
            }
            this.parsedFiles.add(root);
        } catch (Exception ex) {
            this.log.error(ex);
        }
    }

    private void addCategory(String name)
    {
        if (!this.categories.containsKey(name)) {
            this.categories.put(name, new ArrayList<Map<String, Object>>());
        }
        if (!this.allCategories.containsKey(name)) {
            this.allCategories.put(name, new ArrayList<Map<String, Object>>());
        }
    }

    private void createCategoryRegions(Template t)
    {
        this.regions = new LinkedList<>();

        TemplateElement te = t.getRootTreeNode();
        Map<String, Object> pc;
        Comment c;
        Comment regionStart = null;

        String name = null;
        int begincol = 0;
        int beginline = 0;

        Stack<TreeNode> nodes = new Stack<>();
        nodes.push(te);
        while (!nodes.isEmpty()) {
            te = (TemplateElement)nodes.pop();
            for (int i = te.getChildCount() - 1; i >= 0; i--) {
                nodes.push(te.getChildAt(i));
            }

            if (te instanceof Comment) {
                c = (Comment)te;
                pc = this.parse(c);

                if (pc.get("@begin") != null) {
                    if (regionStart != null) {
                        this.log.warn("WARNING: nested @begin-s");
                        CategoryRegion cc =
                            new CategoryRegion(name, begincol, beginline, c.getBeginColumn(), c.getBeginLine());
                        this.regions.add(cc);
                        this.addCategory(name);
                    }
                    name = pc.get("@begin").toString().trim();
                    begincol = c.getBeginColumn();
                    beginline = c.getBeginLine();

                    regionStart = c;
                }
                if (pc.get("@end") != null) {
                    if (regionStart == null) {
                        this.log.warn("WARNING: @end without @begin!");
                    } else {
                        CategoryRegion cc =
                            new CategoryRegion(name, begincol, beginline, c.getEndColumn(), c.getEndLine());
                        this.regions.add(cc);
                        this.addCategory(name);
                        regionStart = null;
                    }
                }

            }
        }
        if (regionStart != null) {
            this.log.warn("WARNING: missing @end (EOF)");
            CategoryRegion cc = new CategoryRegion(name, begincol, beginline, Integer.MAX_VALUE, Integer.MAX_VALUE);
            this.addCategory(name);
            this.regions.add(cc);
        }
    }

    private void addMacro(Map<String, Object> macro)
    {
        this.macros.add(macro);
        this.allMacros.add(macro);
        String key = (String)macro.get("category");
        if (key == null) {
            key = "";
        }
        List<Map<String, Object>> cat = this.categories.get(key);
        if (cat == null) {
            cat = new ArrayList<>();
            this.categories.put(key, cat);
        }
        cat.add(macro);
        List<Map<String, Object>> allCat = this.allCategories.get(key);
        if (allCat == null) {
            allCat = new ArrayList<>();
            this.allCategories.put(key, allCat);
        }
        allCat.add(macro);
    }

    private void putGlobalVars(Map<String, Object> root)
    {
        root.put("title", this.title);
        root.put("files", this.sourceFiles);
        root.put("fileSuffix", ".html");
    }

    private void createAllCatPage()
    {
        File categoryFile = new File(this.outputDir, "index-all-cat.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter(
            new FileOutputStream(categoryFile), Charset.forName(OUTPUT_ENCODING).newEncoder())) {
            Map<String, Object> root = new HashMap<>();
            root.put("categories", this.allCategories);
            this.putGlobalVars(root);
            Template template = this.cfg.getTemplate(Templates.indexAllCat.fileName());
            template.process(root, outputStream);
        } catch (java.io.IOException | freemarker.template.TemplateException ex) {
        }
    }

    private void createAllAlphaPage()
    {
        File allAlphaFile = new File(this.outputDir, "index-all-alpha.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter(
            new FileOutputStream(allAlphaFile), Charset.forName(OUTPUT_ENCODING).newEncoder())) {
            Map<String, Object> root = new HashMap<>();
            Collections.sort(this.allMacros, MACRO_COMPARATOR);
            root.put("macros", this.allMacros);
            this.putGlobalVars(root);
            Template template = this.cfg.getTemplate(Templates.indexAllAlpha.fileName());
            template.process(root, outputStream);
        } catch (java.io.IOException | freemarker.template.TemplateException ex) {
        }
    }

    private void createIndexPage()
    {
        File overviewFile = new File(this.outputDir, "index.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter(
            new FileOutputStream(overviewFile), Charset.forName(OUTPUT_ENCODING).newEncoder())) {
            Template template = this.cfg.getTemplate(Templates.index.fileName());
            Map<String, Object> root = new HashMap<>();
            this.putGlobalVars(root);
            
            if (this.readmeFile != null && this.readmeFile.exists() && this.readmeFile.canRead()) {
                try {
                    String readme = "";
                    readme = new String(Files.readAllBytes(this.readmeFile.toPath()));
                    root.put("readme", readme);
                } catch (java.io.IOException ex) {
                }
            }
            
            template.process(root, outputStream);
        } catch (java.io.IOException | freemarker.template.TemplateException ex) {
        }
    }

    private void copyCssFiles()
        throws IOException
    {
        if (this.alternartiveTemplatesFolder != null) {
            File[] cssfiles = this.alternartiveTemplatesFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name)
                {
                    return StringUtils.endsWithIgnoreCase(name, ".css");
                }
            });
            for (File cssFile : cssfiles) {
                FileUtils.copyFileToDirectory(cssFile, this.outputDir);
            }

        } else {
            InputStream in = this.getClass().getResourceAsStream("/default/ftldoc.css");
            File outputFile = new File(this.outputDir, "ftldoc.css");
            FileUtils.copyInputStreamToFile(in, outputFile);
        }
    }

    private Map<String, Object> createCommentedMacro(Macro macro, Comment comment, File file)
    {
        Map<String, Object> result = new HashMap<>();
        if (macro == null) {
            throw new IllegalArgumentException("macro == null");
        }

        CategoryRegion cc = this.findCategory(macro);
        String cat = null;
        if (cc != null) {
            cat = cc.toString();
        }

        result.putAll(this.parse(comment));
        result.put("category", cat);
        result.put("name", macro.getName());
        result.put("code", macro.getSource());
        result.put("isfunction", new Boolean(macro.isFunction()));
        result.put("type", macro.isFunction() ? "function" : "macro");
        result.put("arguments", macro.getArgumentNames());
        result.put("catchall", macro.getCatchAll());
        result.put("node", new TemplateElementModel(macro));
        result.put("filename", file.getName());
        return result;
    }

    private CategoryRegion findCategory(TemplateElement te)
    {
        Iterator<CategoryRegion> iter = this.regions.iterator();
        while (iter.hasNext()) {
            CategoryRegion cc = iter.next();
            if (cc.contains(te)) {
                return cc;
            }
        }
        return null;
    }

    private Map<String, Object> parse(Comment comment)
    {
        String commentText = null;
        if (comment != null) {
            commentText = comment.getText();
        }
        return ParseFtlDocComment.parse(commentText);
    }

    private class CategoryRegion
    {
        String name;
        int begincol;
        int beginline;
        int endcol;
        int endline;

        CategoryRegion(String name, int begincol, int beginline,
            int endcol, int endline)
        {
            this.name = name;
            this.begincol = begincol;
            this.beginline = beginline;
            this.endcol = endcol;
            this.endline = endline;
        }

        public boolean contains(TemplateElement te)
        {
            int bc = te.getBeginColumn();
            int bl = te.getBeginLine();
            int ec = te.getEndColumn();
            int el = te.getEndLine();
            boolean checkStart = ((bl > this.beginline) || (bl == this.beginline && bc > this.begincol));
            boolean checkEnd = ((el < this.endline) || (el == this.endline && ec < this.endcol));
            return (checkStart && checkEnd);
        }

        @Override
        public String toString()
        {
            return this.name;
        }
    }

    public void setLog(Log log)
    {
        this.log = new Logger(log);
    }
}
