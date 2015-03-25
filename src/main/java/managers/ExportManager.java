package managers;

import export.SheetGenerator;
import files.ExportFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.Query;
import query.QueryResult;

import java.io.IOException;
import java.net.URLDecoder;

public class ExportManager {

    private final Logger logger = LoggerFactory.getLogger(ExportManager.class);

    private static ExportManager em;

    public static void initExportManager(ExportFileManager efm) {
        if (em == null) em = new ExportManager(efm);
    }

    public static ExportManager getExportManager() throws Exception {
        if (em == null)
            throw new Exception("ExportManager needs to be initialised with an instance of ExportFileManager");
        return em;
    }

    private final ExportFileManager efm;

    private ExportManager(ExportFileManager efm) {
        this.efm = efm;
    }

    public synchronized void exportQuery(QueryResult qr) throws Exception {
        efm.writeExport(qr.queryName, SheetGenerator.generateSheet(qr), qr.hasTemplate, qr.outputTimestamp);
    }

    public synchronized String getLatestExportNameForQuery(Query query) {
        return efm.getLatestExportForQuery(query).getName();
    }

    public synchronized String getLatestExportPathForQuery(Query query) {
        return efm.getLatestExportForQuery(query).getAbsolutePath();
    }

    public synchronized void runMacroOnQuery(Query query) throws IOException {

        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            String path = ExportManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            decodedPath = decodedPath.replace("bin/reportTool.jar", "");

            StringBuilder sb = new StringBuilder(query.name);

            if (query.outputTimestamp)
                sb.append(" - ").append(efm.getFileTimestamp(efm.getLatestExportForQuery(query)));

            Runtime.getRuntime().exec( "wscript runmacro.vbs \"" + decodedPath + "\" " + sb.toString());
        }
        else logger.warn("Warning, cannot run vbscript for macro as not on Windows operating system");
    }
}
