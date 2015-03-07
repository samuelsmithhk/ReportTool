package managers;

import export.SheetGenerator;
import files.ExportFileManager;
import query.QueryResult;

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

    public void exportQuery(QueryResult qr) throws Exception {
        efm.writeExport(qr.queryName, SheetGenerator.generateSheet(qr), qr.hasTemplate, qr.outputTimestamp);
    }
}
