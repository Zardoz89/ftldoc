/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */ 

package freemarker.tools.ftldoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.TreeNode;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.Comment;
import freemarker.core.Macro;
import freemarker.core.TemplateElement;
import freemarker.core.TextBlock;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.Version;


/**
 * Main ftldoc class (includes command line tool).
 * 
 * @author Stephan Mueller <stephan at chaquotay dot net>
 */
public class FtlDoc {
    
    private static final String EXT_FTL = ".ftl";
    private static final String OUTPUT_ENCODING = "UTF-8";
    
    private static final Comparator<Map<String, Object>> MACRO_COMPARATOR = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
            return lhs.get("name").toString().toLowerCase()
                    .compareTo(rhs.get("name").toString().toLowerCase());
        }
    };

    private static final Pattern LINESPLIT_PATTERN = Pattern.compile("(\\r\\n)|(\\r)|(\\n)");
    private static final Pattern PARAM_PATTERN = Pattern.compile("^\\s*(?:--)?\\s*@param\\s*(\\w*)\\s*(.*)$");
    private static final Pattern AT_PATTERN = Pattern.compile("^\\s*(?:--)?\\s*(@\\w+)\\s*(.*)$");
    private static final Pattern TEXT_PATTERN = Pattern.compile("^\\s*(?:--)?(.*)$");

    private static enum Templates {
        file("file"),index("index"),indexAllCat("index-all-cat"),
        indexAllAlpha("index-all-alpha"), overview("overview"), filelist("filelist");
        
        private final String fileName;
        private Templates(String fileName) {
            this.fileName = fileName;
        }
         
        public String fileName() {
            return fileName + EXT_FTL;
        }
    }

    private SortedMap<String, List<Map<String, Object>>> allCategories = null;
    private SortedMap<String, List<Map<String, Object>>> categories = null;
    private List<Map<String, Object>> allMacros = null;
    private List<Map<String, Object>> macros = null;
    private File fOutDir;
    private List<File> fFiles;
    private List<SimpleHash> fParsedFiles;
    private Set<File> fAllDirectories;
    private File fAltTemplatesFolder;
    
    List<CategoryRegion> regions = new LinkedList<CategoryRegion>();
    
    private Configuration cfg = null;
    
    
    public FtlDoc(List<File> files, File outputDir, File altTemplatesFolder) {
        cfg = new Configuration(Configuration.VERSION_2_3_26); // TODO parametrice version compatibility
        cfg.setWhitespaceStripping(false);
        cfg.setOutputEncoding(OUTPUT_ENCODING);
        
        fOutDir = outputDir;
        fFiles = files;
        fAltTemplatesFolder = altTemplatesFolder;
        
        // extracting parent directories of all files
        fAllDirectories = new HashSet<File>();
        for (File f : files) {
            fAllDirectories.add(f.getParentFile());
        }
        
    }
    
    private void addCategory(String name) {
        if(!categories.containsKey(name)) {
            categories.put(name,new ArrayList<Map<String, Object>>());
        }
        if(!allCategories.containsKey(name)) {
            allCategories.put(name,new ArrayList<Map<String, Object>>());
        }
    }
    
    private void createCategoryRegions(Template t) {
        regions = new LinkedList<CategoryRegion>();
        
        TemplateElement te = t.getRootTreeNode();
        Map<String, Serializable> pc;
        Comment c;
        Comment regionStart = null;
        
        String name = null;
        int begincol = 0;
        int beginline = 0;
        
        Stack<TreeNode> nodes = new Stack<TreeNode>();
        nodes.push(te);
        while(!nodes.isEmpty()) {
            te=(TemplateElement)nodes.pop();
            for(int i = te.getChildCount()-1;i>=0;i--) {
                nodes.push(te.getChildAt(i));
            }
            
            if(te instanceof Comment) {
                c = (Comment)te;
                pc = parse(c);
                
                if(pc.get("@begin")!=null) {
                    if(regionStart!=null) {
                        System.err.println("WARNING: nested @begin-s");
                        CategoryRegion cc = new CategoryRegion(name, begincol,beginline,c.getBeginColumn(),c.getBeginLine());
                        regions.add(cc);
                        addCategory(name);
                    }
                    name = pc.get("@begin").toString().trim();
                    begincol = c.getBeginColumn();
                    beginline = c.getBeginLine();
                    
                    
                    regionStart = c;
                }
                if(pc.get("@end")!=null) {
                    if(regionStart==null) {
                        System.err.println("WARNING: @end without @begin!");
                    } else {
                        CategoryRegion cc = new CategoryRegion(name, begincol,beginline,c.getEndColumn(),c.getEndLine());
                        regions.add(cc);
                        addCategory(name);
                        regionStart = null;
                    }
                }
                
                
            }
        }
        if(regionStart!=null) {
            System.err.println("WARNING: missing @end (EOF)");
            CategoryRegion cc = new CategoryRegion(name, begincol,beginline,Integer.MAX_VALUE,Integer.MAX_VALUE);
            addCategory(name);
            regions.add(cc);
        }
    }
    
    private void addMacro(Map<String, Object> macro) {
        macros.add(macro);
        allMacros.add(macro);
        String key = (String)macro.get("category");
        if(key==null) key = "";
        List<Map<String, Object>> cat = categories.get(key);
        if(cat==null) {
            cat = new ArrayList<Map<String, Object>>();
            categories.put(key,cat);
        }
        cat.add(macro);
        List<Map<String, Object>> allCat = allCategories.get(key);
        if(allCat==null) {
            allCat = new ArrayList<Map<String, Object>>();
            allCategories.put(key,allCat);
        }
        allCat.add(macro);
    }
    
    private void createFilePage(File file) {
        try {
            File htmlFile = new File(fOutDir,file.getName()+".html");
            System.out.println("Generating " + htmlFile.getCanonicalFile() + "...");
            
            Template t_out = cfg.getTemplate(Templates.file.fileName());
            categories = new TreeMap<String, List<Map<String, Object>>>();
            TemplateElement te = null;
            Comment globalComment = null;
            Template t = cfg.getTemplate(file.getName());
            macros = new ArrayList<Map<String, Object>>();
            Set<Comment> comments = new HashSet<Comment>();
            Map ms = t.getMacros();
            
            createCategoryRegions(t);
            
            Iterator macroIter = ms.values().iterator();
            while(macroIter.hasNext()) {
                Macro macro = (Macro)macroIter.next();
                int k = macro.getParent().getIndex(macro);
                for(int j=k-1;j>=0;j--) {
                    te = (TemplateElement)macro.getParent().getChildAt(j);
                    if(te instanceof TextBlock) {
                        if(((TextBlock)te).getSource().trim().length()==0) {
                            continue;
                        } else {
                            addMacro(createCommentedMacro(macro,null,file));
                            break;
                        }
                    } else if(te instanceof Comment) {
                        Comment c = (Comment)te;
                        comments.add(c);
                        if(c.getText().startsWith("-")) {
                            addMacro(createCommentedMacro(macro,c,file));
                            break;
                        }
                    } else {
                        addMacro(createCommentedMacro(macro,null,file));
                        break;
                    }
                }
            }
            
            
            te = t.getRootTreeNode();
            if(te.getClass().getName().endsWith("MixedContent")) {
                Enumeration children = te.children();
                while (children.hasMoreElements())
                {
                    Object element = children.nextElement();
                    if(element instanceof Comment) {
                        Comment candidate = (Comment)element;
                        if(candidate.getText().startsWith("-")) {
                            if(!comments.contains(candidate)) {
                                globalComment = candidate;
                            }
                            break;
                        }
                    }
                }
            }
            
            Collections.sort(macros, MACRO_COMPARATOR);
            for (List<Map<String, Object>> l : categories.values()) {
                Collections.sort(l, MACRO_COMPARATOR);
            }
            
            SimpleHash root = new SimpleHash();
            root.put("macros",macros);
            if(null!=globalComment) {
                root.put("comment",parse(globalComment));
            } else {
                root.put("comment",new SimpleHash());
            }
            root.put("filename",t.getName());
            root.put("categories",categories);
            
            try (OutputStreamWriter outputStream = new OutputStreamWriter (
                    new FileOutputStream (htmlFile), Charset.forName(OUTPUT_ENCODING).newEncoder()))
            {
                t_out.process(root, outputStream);
            }
            fParsedFiles.add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Starts the ftldoc generation.
     *
     */
    public void run() {
     
        try {
            // init global collections
            allCategories = new TreeMap<String, List<Map<String, Object>>>();
            allMacros = new ArrayList<Map<String, Object>>();
            fParsedFiles = new ArrayList<SimpleHash>();
            
            
            TemplateLoader[] loaders = new TemplateLoader[fAllDirectories.size()+1];

            // loader for ftldoc templates
            if (fAltTemplatesFolder != null) {
                loaders[0] = new FileTemplateLoader(fAltTemplatesFolder);
            } else {
                loaders[0] = new ClassTemplateLoader(this.getClass(), "/default");
            }
            
            
            // add loader for every directory
            int i = 1;
            for (Iterator<File> it = fAllDirectories.iterator(); it.hasNext(); i++) {
                loaders[i] = new FileTemplateLoader(it.next());
            }
            
            TemplateLoader loader = new MultiTemplateLoader(loaders);
            cfg.setTemplateLoader(loader);
            
            // create template for file page
            
            
            // create file pages
            for(int n=0;n<fFiles.size();n++) {
                createFilePage((File)fFiles.get(n));
            }
            
            // sort categories
            for (List<Map<String, Object>> l : allCategories.values()) {
                Collections.sort(l, MACRO_COMPARATOR);
            }
            
            // create the rest
            createFileListPage(".html");
            createIndexPage();
            createAllCatPage();
            createAllAlphaPage();
            createOverviewPage();
        }
        
        catch (Exception e) {e.printStackTrace();}
    }
    
    private void createIndexPage() {
        File indexFile = new File(fOutDir,"index.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter (
                    new FileOutputStream (indexFile), Charset.forName(OUTPUT_ENCODING).newEncoder()))
        {
            Template template = cfg.getTemplate(Templates.index.fileName());
            template.process(null, outputStream);
        } catch (java.io.IOException | freemarker.template.TemplateException ex) {}
    }
    
    private void createAllCatPage() {
        File categoryFile = new File(fOutDir,"index-all-cat.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter (
                    new FileOutputStream (categoryFile), Charset.forName(OUTPUT_ENCODING).newEncoder()))
        {
            SimpleHash root = new SimpleHash();
            root.put("categories", allCategories);
            Template template = cfg.getTemplate(Templates.indexAllCat.fileName());
            template.process(root, outputStream);
        } catch (java.io.IOException | freemarker.template.TemplateException ex) {}
    }
    
    private void createAllAlphaPage() {
        File allAlphaFile = new File(fOutDir,"index-all-alpha.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter (
                    new FileOutputStream (allAlphaFile), Charset.forName(OUTPUT_ENCODING).newEncoder()))
        {
            SimpleHash root = new SimpleHash();
            Collections.sort(allMacros, MACRO_COMPARATOR);
            root.put("macros", allMacros);
            Template template = cfg.getTemplate(Templates.indexAllAlpha.fileName());
            template.process(root, outputStream);
        } catch (java.io.IOException | freemarker.template.TemplateException ex) {}
    }
    
    private void createOverviewPage() {
        File overviewFile = new File(fOutDir,"overview.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter (
                    new FileOutputStream (overviewFile), Charset.forName(OUTPUT_ENCODING).newEncoder()))
        {
            Template template = cfg.getTemplate(Templates.overview.fileName());
            Map<String, List> root = new HashMap<String, List>();
            root.put("files",fParsedFiles);
            template.process(root, outputStream);
        } catch (java.io.IOException | freemarker.template.TemplateException ex) {}
    }
    
    private void createFileListPage(String suffix) {
        Collections.sort(fFiles, new Comparator<File>() {
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        
        File filelistFile = new File(fOutDir,"files.html");
        try (OutputStreamWriter outputStream = new OutputStreamWriter (
                    new FileOutputStream (filelistFile), Charset.forName(OUTPUT_ENCODING).newEncoder()))
        {
            SimpleHash root = new SimpleHash();
            root.put("suffix",suffix);
            root.put("files",fFiles);
            Template template = cfg.getTemplate(Templates.filelist.fileName());
            template.process(root, outputStream);
        } catch (java.io.IOException | freemarker.template.TemplateException ex) {}
    }
    
    private Map<String, Object> createCommentedMacro(Macro macro, Comment comment, File file) {
        Map<String, Object> result = new HashMap<String, Object>();
        if( macro == null ) {
            throw new IllegalArgumentException("macro == null");
        }
        
        CategoryRegion cc = findCategory(macro);
        String cat = null;
        if(cc!=null) {
            cat = cc.toString();
        }
        
        result.putAll(parse(comment));
        result.put("category",cat);
        result.put("name",macro.getName());
        result.put("code",macro.getSource());
        result.put("isfunction", new Boolean(macro.isFunction()));
        result.put("type",macro.isFunction()?"function":"macro");
        result.put("arguments",macro.getArgumentNames());
        result.put("catchall", macro.getCatchAll());
        result.put("node",new TemplateElementModel(macro));
        result.put("filename", file.getName());
        return result;
    }
    
    private Map<String, Serializable> parse(Comment comment) {
        Map<String, Serializable> result = new HashMap<String, Serializable>();
        
        
        // always return a hash, even if doesn't have any content
        if(null==comment) {
            return result;
        }
        
        SimpleSequence params = new SimpleSequence();
        
        Matcher m;
        // remove leading hyphen (last hyphen of '<#---')
        String fixedComment = comment.getText().substring(1);
        StringBuffer bufText = new StringBuffer();

        String[] lines = LINESPLIT_PATTERN.split(fixedComment);
        String line;

        for(int i = 0;i<lines.length;i++) {
            line = lines[i];
            if ((m = PARAM_PATTERN.matcher(line)).matches()) {
                SimpleHash param = new SimpleHash();
                param.put("name",m.group(1));
                param.put("description",m.group(2));
                params.add(param);
            } else if((m = AT_PATTERN.matcher(line)).matches()) {
                result.put(m.group(1),m.group(2));
            } else if ((m = TEXT_PATTERN.matcher(line)).matches()) {
                if (line.matches("^\\s+.*$")) {
                    // Line started with spaces, collapse them
                    // in a single one
                    bufText.append(" " + m.group(1));
                } else {
                    bufText.append(m.group(1));
                }
                bufText.append("\n");
            } else {
                // one can prove (with some automat theory) that the
                // TEXT_PATTERN regex matches *every* string. Under normal
                // circumstances this else block can never be reached.
                System.err.println("WARNING: reached unreachable point: " + line);
            }
        }
        String text = bufText.toString().replaceAll("\n","");
        
        result.put("@param",params);
        result.put("comment",text);
        // extract first sentence for "Macro and Function Summary" table
        int endOfSentence = text.indexOf(".");
        if(endOfSentence>0) {
            result.put("short_comment",text.substring(0,endOfSentence+1));
        } else {
            result.put("short_comment",text);
        }
        
        return result;
    }
    
    private CategoryRegion findCategory(TemplateElement te) {
        Iterator<CategoryRegion> iter = regions.iterator();
        while (iter.hasNext())
        {
            CategoryRegion cc = iter.next();
            if(cc.contains(te)) return cc;
        }
        return null;
    }
    
    private class CategoryRegion{
        
        String name;
        int begincol;
        int beginline;
        int endcol;
        int endline;
        
        CategoryRegion (String name, int begincol, int beginline,
                        int endcol, int endline) {
            this.name=name;
            this.begincol = begincol;
            this.beginline = beginline;
            this.endcol = endcol;
            this.endline = endline;
        }
        
        public boolean contains(TemplateElement te) {
            int bc = te.getBeginColumn();
            int bl = te.getBeginLine();
            int ec = te.getEndColumn();
            int el = te.getEndLine();
            boolean checkStart = ((bl>beginline) || (bl==beginline && bc>begincol));
            boolean checkEnd = ((el<endline) || (el==endline && ec < endcol));
            return  (checkStart && checkEnd);
        }
        
        public String toString() {
            return name;
        }
    }
    
}
