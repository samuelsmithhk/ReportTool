package managers;

import export.SheetGenerator;
import files.ExportFileManager;
import query.Query;
import query.QueryResult;

import java.io.IOException;

public class ExportManager {

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
        Runtime.getRuntime().exec( "wscript runmacro.vbs " + query.name);
    }
}
