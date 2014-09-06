package ems.mib.compiler.tools.smi;

import org.apache.log4j.Logger;
import org.jsmiparser.parser.SmiDefaultParser;
import org.jsmiparser.parser.SmiParser;
import org.jsmiparser.smi.SmiMib;
import org.jsmiparser.smi.SmiVersion;
import org.jsmiparser.util.url.ClassPathURLListFactory;
import org.jsmiparser.util.url.CompositeURLListFactory;
import org.jsmiparser.util.url.FileURLListFactory;
import org.jsmiparser.util.url.URLListFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: thebaz
 * Date: 2/24/12
 * Time: 8:58 PM
 */
public class SmiParserHelper {
    private final Logger log = Logger.getLogger(getClass());
    private static SmiParserHelper instance;
    private SmiVersion version;
    private String[] resources;
    private static ThreadLocal<SmiMib> smiMib = new ThreadLocal<SmiMib>();

    private SmiParserHelper() {
    }

    public static SmiParserHelper getInstance() {
        if (instance == null) {
            instance = new SmiParserHelper();
        }
        return instance;
    }

    public final String[] getResources() {
        return resources;
    }

    public SmiMib loadSmiMib(String mibName, int version, String... resources) {
        if (version == 1) {
            this.version = SmiVersion.V1;
        } else if (version == 2) {
            this.version = SmiVersion.V2;
        }
        this.resources = resources;
        // this is a rather ugly hack to mimic JUnit4 @BeforeClass, without
        // having to annotate all test methods:
        try {
            SmiParser parser = createParser(mibName);
            /*StopWatch stopWatch = new StopWatch();
            stopWatch.start();*/
            SmiMib mib = parser.parse();
            /*stopWatch.stop();
            log.info("Parsing time: " + stopWatch.getTotalTimeSeconds()
                    + " s");*/

            smiMib.set(mib);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return smiMib.get();
    }

    private SmiParser createParser(String mibName) throws Exception {
        List<URL> urls = loadRepository(mibName);

        SmiDefaultParser parser = new SmiDefaultParser();
        parser.getFileParserPhase().setInputUrls(urls);
        return parser;
    }

    private List<URL> loadRepository(String mibName) throws Exception {
        List<URLListFactory> listFactories = new ArrayList<URLListFactory>();
        ClassPathURLListFactory defaultListFactory = new ClassPathURLListFactory("mibs/ietf/");

        if (version == null || version == SmiVersion.V1) {
            defaultListFactory.add("RFC1155-SMI");
        }
        if (version == null || version == SmiVersion.V2) {
            defaultListFactory.add("SNMPv2-SMI");
            defaultListFactory.add("SNMPv2-TC");
            defaultListFactory.add("SNMPv2-CONF");
            defaultListFactory.add("SNMPv2-MIB");
        }
        if (getResources().length == 0) {
            defaultListFactory.add(mibName);
        }
        listFactories.add(defaultListFactory);
        ClassPathURLListFactory ianaListFactory = new ClassPathURLListFactory("mibs/iana/");
        ianaListFactory.add("IANA-ADDRESS-FAMILY-NUMBERS-MIB");
        ianaListFactory.add("IANA-CHARSET-MIB");
        ianaListFactory.add("IANA-FINISHER-MIB");
        ianaListFactory.add("IANA-LANGUAGE-MIB");
        ianaListFactory.add("IANA-MALLOC-MIB");
        ianaListFactory.add("IANA-PRINTER-MIB");
        ianaListFactory.add("IANA-RTPROTO-MIB");
        ianaListFactory.add("IANAifType-MIB");
        ianaListFactory.add("IANATn3270eTC-MIB");
        listFactories.add(ianaListFactory);
        for (String resource : getResources()) {
            FileURLListFactory fileURLListFactory = new FileURLListFactory(resource);
            fileURLListFactory.add(mibName);
            try {
                fileURLListFactory.create();
                listFactories.add(fileURLListFactory);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find MIB in repository");
                }
            }
        }
        CompositeURLListFactory repositoryFactory = new CompositeURLListFactory(listFactories);
        return repositoryFactory.create();
    }
}
