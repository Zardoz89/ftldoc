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
import java.util.LinkedHashMap;
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
 *
 * @deprecated This class uses deprecated FreeMarker internal APIs (freemarker.core package)
 *             like TemplateElement, Comment, Macro, and Template.getRootTreeNode().
 *             These APIs are marked as internal and may be removed in future versions.
 *             See: https://freemarker.apache.org/docs/api/deprecated-list.html
 */
@SuppressWarnings("unchecked")
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
    private List<Map<String, Object>> allVariables = null;
    private List<Map<String, Object>> variables = null;
    private List<Map<String, Object>> externalVariables = null;
    private File outputDir;
    private List<File> sourceFiles;
    private List<Map<String, Object>> parsedFiles;
    private Map<File, List<File>> categorizedFiles;
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
        Collections.sort(this.sourceFiles, FILE_COMPARATOR);

        this.categorizedFiles = new LinkedHashMap<>();
        for (File sourceFile : this.sourceFiles) {
            File parentFile = sourceFile.getParentFile();
            List<File> filesOnCategory = this.categorizedFiles.getOrDefault(parentFile, new ArrayList<>());
            filesOnCategory.add(sourceFile);
            this.categorizedFiles.put(parentFile, filesOnCategory);
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
            this.allVariables = new ArrayList<>();
            this.parsedFiles = new ArrayList<>();
            this.externalVariables = new ArrayList<>();

            List<TemplateLoader> loaders = new ArrayList<>(this.categorizedFiles.size() + 1);
            // Loads documantation generation templates
            loaders.add(this.loadDocumentationTemplates());

            // add loader for every directory
            loaders.addAll(this.loadSourceTemplates());

            TemplateLoader[] loadersArray = loaders.toArray(new TemplateLoader[0]);
            TemplateLoader loader = new MultiTemplateLoader(loadersArray);
            this.cfg.setTemplateLoader(loader);

            // = create template for file page

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
            this.createGlobalVarsIndexPage();
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
        for (File file : this.categorizedFiles.keySet()) {
            loaders.add(new FileTemplateLoader(file));
        }
        return loaders;
    }

    private void createFilePage(File file)
    {
        this.categories = new TreeMap<>();
        this.macros = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.externalVariables = new ArrayList<>();
        try {
            File htmlFile = new File(this.outputDir, file.getName() + ".html");
            this.log.info("Generating " + htmlFile.getCanonicalFile() + "...");

            Template outputTemplate = this.cfg.getTemplate(Templates.file.fileName());
            Template template = this.cfg.getTemplate(file.getName());
            Set<Comment> comments = new HashSet<>();
            Map<String, Macro> templateMacros = template.getMacros();

            this.createCategoryRegions(template);

            this.extractCommentedMacros(file, comments, templateMacros);

            this.extractGlobalVariables(template, comments, file);

            Comment globalComment = this.getGlobalCommant(template, comments);

            Map<String, Object> globalCommentData = new HashMap<>();
            if (null != globalComment) {
                globalCommentData = this.parse(globalComment);
                Object ftlvariableObj = globalCommentData.get("@ftlvariable");
                if (ftlvariableObj instanceof List) {
                    List<Map<String, String>> ftlvariables = (List<Map<String, String>>) ftlvariableObj;
                    for (Map<String, String> fv : ftlvariables) {
                        Map<String, Object> extVar = new HashMap<>();
                        extVar.put("name", fv.get("name"));
                        extVar.put("type", fv.get("type"));
                        if (fv.get("file") != null) {
                            extVar.put("file", fv.get("file"));
                        }
                        this.externalVariables.add(extVar);
                    }
                }
            }

            Collections.sort(this.macros, MACRO_COMPARATOR);
            for (List<Map<String, Object>> l : this.categories.values()) {
                Collections.sort(l, MACRO_COMPARATOR);
            }

            Map<String, Object> root = new HashMap<>();
            root.put("macros", this.macros);
            root.put("variables", this.variables);
            root.put("externalVariables", this.externalVariables);
            root.put("comment", globalCommentData);
            root.put("filename", template.getName());
            root.put("categories", this.categories);
            this.putGlobalVars(root);

            try (OutputStreamWriter outputStream = new OutputStreamWriter(
                new FileOutputStream(htmlFile), Charset.forName(OUTPUT_ENCODING).newEncoder())) {
                outputTemplate.process(root, outputStream);
            }
            this.parsedFiles.add(root);
        } catch (Exception ex) {
            this.log.error(ex);
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

    private void addCategory(String name)
    {
        if (!this.categories.containsKey(name)) {
            this.categories.put(name, new ArrayList<Map<String, Object>>());
        }
        if (!this.allCategories.containsKey(name)) {
            this.allCategories.put(name, new ArrayList<Map<String, Object>>());
        }
    }

    private void extractCommentedMacros(File file, Set<Comment> comments, Map<String, Macro> ms)
    {
        TemplateElement te;
        for (Macro macro : ms.values()) {
            int k = macro.getParent().getIndex(macro);
            for (int j = k - 1; j >= 0; j--) {
                te = (TemplateElement)macro.getParent().getChildAt(j);
                if (te instanceof TextBlock) {
                    if (((TextBlock)te).getSource().trim().length() == 0) {
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
    }

    private void extractGlobalVariables(Template template, Set<Comment> comments, File file)
    {
        TemplateElement root = template.getRootTreeNode();
        Stack<TreeNode> nodes = new Stack<>();
        nodes.push(root);

        while (!nodes.isEmpty()) {
            TemplateElement te = (TemplateElement)nodes.pop();
            for (int i = te.getChildCount() - 1; i >= 0; i--) {
                nodes.push(te.getChildAt(i));
            }

            if (te.getClass().getName().endsWith("Assignment")) {
                boolean isGlobal = this.isGlobalAssignment(te);
                if (isGlobal) {
                    String varName = this.getAssignmentVariableName(te);
                    if (varName != null) {
                        Comment associatedComment = this.findPreviousComment(te, comments);
                        this.addVariable(this.createCommentedVariable(te, associatedComment, file, varName));
                    }
                }
            }
        }
    }

    private boolean isGlobalAssignment(TemplateElement te)
    {
        try {
            java.lang.Class<?> assignmentClass = te.getClass();
            java.lang.reflect.Field scopeField = assignmentClass.getDeclaredField("scope");
            scopeField.setAccessible(true);
            int scope = scopeField.getInt(te);
            
            java.lang.Class<?> freemarkerCore = java.lang.Class.forName("freemarker.core.Assignment");
            java.lang.reflect.Field globalField = freemarkerCore.getDeclaredField("GLOBAL");
            globalField.setAccessible(true);
            int globalValue = globalField.getInt(null);
            return scope == globalValue;
        } catch (Exception e) {
            return false;
        }
    }

    private String getAssignmentVariableName(TemplateElement te)
    {
        try {
            java.lang.Class<?> teClass = te.getClass();
            java.lang.reflect.Method getParameterCountMethod = null;
            
            for (java.lang.Class<?> c = teClass; c != null; c = c.getSuperclass()) {
                try {
                    getParameterCountMethod = c.getDeclaredMethod("getParameterCount");
                    break;
                } catch (NoSuchMethodException e) {
                }
            }
            
            if (getParameterCountMethod == null) {
                return null;
            }
            
            getParameterCountMethod.setAccessible(true);
            Integer paramCount = (Integer) getParameterCountMethod.invoke(te);
            
            for (int i = 0; i < paramCount; i++) {
                java.lang.reflect.Method getParameterValueMethod = null;
                java.lang.reflect.Method getParameterRoleMethod = null;
                
                for (java.lang.Class<?> c = teClass; c != null; c = c.getSuperclass()) {
                    try {
                        getParameterValueMethod = c.getDeclaredMethod("getParameterValue", int.class);
                        getParameterRoleMethod = c.getDeclaredMethod("getParameterRole", int.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Continue to parent class
                    }
                }
                
                if (getParameterValueMethod == null || getParameterRoleMethod == null) {
                    continue;
                }
                
                getParameterValueMethod.setAccessible(true);
                getParameterRoleMethod.setAccessible(true);
                
                Object paramValue = getParameterValueMethod.invoke(te, i);
                Object roleObj = getParameterRoleMethod.invoke(te, i);
                
                String roleName = roleObj.toString();
                if (roleName.contains("assignment target") && paramValue != null) {
                    return paramValue.toString();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private Comment findPreviousComment(TemplateElement te, Set<Comment> comments)
    {
        TreeNode parentNode = te.getParent();
        if (parentNode == null) {
            return null;
        }
        TemplateElement parent = (TemplateElement) parentNode;
        int idx = parent.getIndex(te);
        for (int j = idx - 1; j >= 0; j--) {
            TemplateElement sibling = (TemplateElement) parent.getChildAt(j);
            if (sibling instanceof TextBlock) {
                if (((TextBlock)sibling).getSource().trim().length() == 0) {
                    continue;
                }
                return null;
            } else if (sibling instanceof Comment) {
                Comment c = (Comment)sibling;
                comments.add(c);
                if (c.getText().startsWith("-")) {
                    return c;
                }
                return null;
            } else {
                return null;
            }
        }
        return null;
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

    private Comment getGlobalCommant(Template template, Set<Comment> comments)
    {
        Comment globalComment = null;
        TemplateElement templateElement = template.getRootTreeNode();
        if (templateElement.getClass().getName().endsWith("MixedContent")) {
            Enumeration<?> children = templateElement.children();
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
        return globalComment;
    }

    private void putGlobalVars(Map<String, Object> root)
    {
        root.put("title", this.title);
        root.put("files", this.sourceFiles);
        root.put("categorizedFiles", this.categorizedFiles);
        root.put("fileSuffix", ".html");
        root.put("hasGlobalVariables", this.allVariables != null && !this.allVariables.isEmpty());
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

    private void createGlobalVarsIndexPage()
    {
        if (this.allVariables.isEmpty()) {
            return;
        }
        File globalVarsFile = new File(this.outputDir, "index-global-vars.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter(
            new FileOutputStream(globalVarsFile), Charset.forName(OUTPUT_ENCODING).newEncoder())) {
            Map<String, Object> root = new HashMap<>();
            Collections.sort(this.allVariables, MACRO_COMPARATOR);
            root.put("variables", this.allVariables);
            this.putGlobalVars(root);
            Template template = this.cfg.getTemplate(Templates.indexGlobalVars.fileName());
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
        result.put("isfunction", Boolean.valueOf(macro.isFunction()));
        result.put("type", macro.isFunction() ? "function" : "macro");
        result.put("arguments", macro.getArgumentNames());
        result.put("catchall", macro.getCatchAll());
        result.put("node", new TemplateElementModel(macro));
        result.put("filename", file.getName());
        return result;
    }

    private Map<String, Object> createCommentedVariable(TemplateElement variable, Comment comment, File file, String name)
    {
        Map<String, Object> result = new HashMap<>();
        if (variable == null) {
            throw new IllegalArgumentException("variable == null");
        }

        result.putAll(this.parse(comment));
        result.put("name", name);
        result.put("type", "global");
        result.put("node", new TemplateElementModel(variable));
        result.put("filename", file.getName());
        return result;
    }

    private void addVariable(Map<String, Object> variable)
    {
        this.variables.add(variable);
        this.allVariables.add(variable);
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
