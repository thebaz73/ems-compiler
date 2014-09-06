package ems.mib.compiler.tools.generator;

import ems.mib.compiler.tools.generator.domain.DataModel;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jsmiparser.smi.SmiTable;
import org.jsmiparser.smi.SmiUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: thebaz
 * Date: 2/25/12
 * Time: 6:36 PM
 */
public class TemplateHelper {
    private final Logger log = Logger.getLogger(getClass());
    private static TemplateHelper instance;

    private TemplateHelper() {
    }

    public static TemplateHelper getInstance() {
        if (instance == null) {
            instance = new TemplateHelper();
        }
        return instance;
    }

    public List<File> generateSource(DataModel model, File output) {
        List<File> files = new ArrayList<File>();
        VelocityContext context = new VelocityContext();
        context.put("className", model.getClassName());
        context.put("packageName", model.getPackageName());
        context.put("objectTypes", model.getObjectTypes());
        context.put("tables", model.getTables());
        context.put("rows", model.getRows());
        context.put("columns", model.getColumns());
        context.put("scalars", model.getScalars());
        context.put("TypeConverter", TypeConverter.class);
        context.put("SmiUtil", SmiUtil.class);

        try {
            Properties p = new Properties();
            p.setProperty("resource.loader", "class");
            p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
            p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            p.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogChute");
            Velocity.init(p);

            StringWriter sw;
            Template managedObject = Velocity.getTemplate("managed_object.vm");
            sw = new StringWriter();
            managedObject.merge(context, sw);
            if (log.isDebugEnabled()) {
                log.debug(String.format("\n%s", sw.toString()));
            }
            File moFile = writeJavaFile(output, model.getPackageName(), model.getClassName(), sw.toString());
            if(moFile != null) {
                files.add(moFile);
            }
            for (SmiTable smiTable : model.getTables()) {
                Template managedRow = Velocity.getTemplate("row_object.vm");
                context.put("currentTable", smiTable);
                sw = new StringWriter();
                managedRow.merge(context, sw);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("\n%s", sw.toString()));
                }
                File rowFile = writeJavaFile(output, model.getPackageName(), SmiUtil.ucFirst(smiTable.getRow().getId()), sw.toString());
                if(rowFile != null) {
                    files.add(rowFile);
                }
            }
            return files;
        } catch (ResourceNotFoundException rnfe) {
            log.error("Resource error: couldn't find the template", rnfe);
        } catch (ParseErrorException pee) {
            log.error("Syntax error: problem parsing the template", pee);
        } catch (MethodInvocationException mie) {
            log.error("Template error: something invoked in the template threw an exception", mie);
        } catch (Exception e) {
            log.error("Generic error: " + e.getMessage(), e);
        }

        return files;
    }

    private File writeJavaFile(File output, String packageName, String className, String content) {
        try {
            File javaFile = new File(createTree(output, packageName), String.format("%s.java", className));
            if (javaFile.exists()) {
                boolean delete = javaFile.delete();
                if (log.isDebugEnabled() && delete) {
                    log.debug("Previous java file deleted");
                }
            }
            boolean newFile = javaFile.createNewFile();
            if(newFile) {
                FileWriter writer = new FileWriter(javaFile);
                writer.write(content);
                writer.flush();
                writer.close();
            }
            else {
                log.error("Cannot create java file");
            }
            return javaFile;
        } catch (IOException e) {
            log.error("IO exception", e);
        }
        return null;
    }

    private File createTree(File output, String packageName) {
        String[] dirs = packageName.split("\\.");
        File tree = output;
        for (String dir : dirs) {
            tree = new File(tree, dir);
            if(tree.mkdirs()) {
                log.info(String.format("Folder %s created", tree.getAbsolutePath()));
            }
        }
        return tree;
    }

}
