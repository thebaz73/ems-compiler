package ems.mib.compiler.tools;

import ems.mib.compiler.tools.generator.TemplateHelper;
import ems.mib.compiler.tools.generator.domain.DataModel;
import ems.mib.compiler.tools.smi.SmiParserHelper;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.jsmiparser.smi.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by IntelliJ IDEA.
 * User: thebaz
 * Date: 2/24/12
 * Time: 8:13 PM
 */
public class Mibc {
    private static Log log = LogFactory.getLog(Mibc.class);

    public static void main(String[] args) {
        Options opt = addOptions();
        try {
            BasicParser parser = new BasicParser();
            CommandLine cl = parser.parse(opt, args);

            if (cl.hasOption('h')) {
                printUsage(opt);
            } else {
                String className = cl.getOptionValue("n");
                String packageName = cl.getOptionValue("p");
                String mibName = cl.getOptionValue("mib");
                String version = cl.getOptionValue("v");
                String repository = cl.getOptionValue("d");
                String libDir = cl.getOptionValue("l");
                boolean keep = cl.hasOption("k");
                if (className == null || packageName == null || mibName == null || version == null)
                    printUsage(opt);
                else if (!version.equals("1") && !version.equals("2")) {
                    printUsage(opt);
                } else {
                    String output = cl.getOptionValue("o");
                    if(output == null) {
                        output = System.getProperty("java.io.tmpdir");
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(className);
                        log.debug(packageName);
                        log.debug(mibName);
                        log.debug(version);
                        log.debug(repository);
                    }
                    Mibc compiler = new Mibc();
                    File folder = new File(output, "work");
                    boolean mkdirs = folder.mkdirs();
                    if(mkdirs) {
                        log.info(String.format("Folder %s created.", folder.getAbsolutePath()));
                    }
                    compiler.doCompile(className, packageName, mibName, version, repository, folder, new File(libDir), keep);
                }
            }
        } catch (ParseException e) {
            log.error("Error parsing command line", e);
            printUsage(opt);
        } catch (Exception e) {
            log.error("Error ", e);
            //printUsage(opt);
        }
    }

    private static Options addOptions() {
        Options opt = new Options();
        opt.addOption("h", "help", false, "Print help for this application");
        opt.addOption("n", "name", true, "The ManagedObject class name");
        opt.addOption("p", "package", true, "The ManagedObject package name");
        opt.addOption("m", "mib", true, "The MIB file name");
        opt.addOption("v", "version", true, "The MIB file version [1|2]");
        opt.addOption("d", "dir", true, "A pipe (|) separated list of MIB file's repositories");
        opt.addOption("l", "lib", true, "Library directory");
        opt.addOption("o", "output", true, "Output directory");
        opt.addOption("k", "keep", false, "Keep sources");
        return opt;
    }

    private static void printUsage(Options opt) {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("mibc ", opt);
    }

    private void doCompile(String className, String packageName, String mibName, String version, String repository, File output, File libDir, boolean keep) throws IOException {
        SmiMib smiMib = SmiParserHelper.getInstance().loadSmiMib(mibName, Integer.parseInt(version), repository == null ? new String[0] : repository.split("|"));
        SmiModule smiMibModule = smiMib.findModule(mibName);
        if (log.isDebugEnabled()) {
            log.debug(String.format("SmiMib: %s", smiMibModule.getId()));
        }
        if(smiMibModule != null) {
            Collection<SmiObjectType> objectTypes = smiMibModule.getObjectTypes();
            Collection<SmiTable> tables = smiMibModule.getTables();
            Collection<SmiRow> rows = smiMibModule.getRows();
            Collection<SmiVariable> columns = smiMibModule.getColumns();
            Collection<SmiVariable> scalars = smiMibModule.getScalars();
            if (log.isDebugEnabled()) {
                for (SmiObjectType objectType : objectTypes) {
                    log.debug(String.format("ObjectType %s", objectType));
                }
            }
            log.debug(String.format("Table\t#: %d", tables.size()));
            log.debug(String.format("Row\t\t#: %d", rows.size()));
            log.debug(String.format("Column\t#: %d", columns.size()));
            log.debug(String.format("Scalar\t#: %d", scalars.size()));
            DataModel model = new DataModel();
            model.setClassName(className);
            model.setPackageName(packageName);
            model.setObjectTypes(objectTypes);
            model.setTables(tables);
            model.setRows(rows);
            model.setColumns(columns);
            model.setScalars(scalars);

            List<File> files = TemplateHelper.getInstance().generateSource(model, output);
            StringBuilder builder = new StringBuilder();
            for (String jar : libDir.list()) {
                if (!jar.endsWith("sources.jar") && jar.endsWith("jar")) {
                    builder.append(libDir.getAbsolutePath()).append("/").append(jar).append(":");
                }
            }
            String compilationPath = builder.toString().substring(0, builder.toString().lastIndexOf(":"));
            Main compiler = new Main();
            MessageHandler m = new MessageHandler();
            List<String> args = new ArrayList<String>(Arrays.asList("-1.5", "-aspectpath", compilationPath));
            for (File file : files) {
                args.add(file.getAbsolutePath());
            }
            compiler.run(args.toArray(new String[args.size()]), m);
            IMessage[] ms = m.getMessages(null, true);
            for (IMessage iMessage : ms) {
                if (log.isDebugEnabled()) {
                    log.debug(iMessage);
                }
            }

            if(!keep) {
                for (File javaFile : files) {
                    boolean delete = javaFile.delete();
                    if (log.isDebugEnabled() && delete) {
                        log.debug(String.format("Java file %s deleted", javaFile.getAbsolutePath()));
                    }
                }
            }

            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(new Attributes.Name("MIB-Name"), mibName);
            manifest.getMainAttributes().put(new Attributes.Name("Created-By"), "EMS MibCompiler");
            manifest.getMainAttributes().put(new Attributes.Name("Bean-Name"), packageName + "." + className);
            String jarName = className + ".jar";
            File jarFile = new File(output.getParent(), jarName);
            JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile), manifest);
            add(output, output.getAbsolutePath(), target);
            target.close();
        }
    }

    private void add(File source, String workingDirPath, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                /*String name = source.getPath().replace("\\", "/").substring(workingDirPath.length());
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }*/
                for (File nestedFile : source.listFiles())
                    add(nestedFile, workingDirPath, target);
                return;
            }

            JarEntry entry = new JarEntry(source.getPath().substring(workingDirPath.length() + 1).replace("\\", "/"));
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        }
        finally {
            if (in != null)
                in.close();
        }
    }

}
