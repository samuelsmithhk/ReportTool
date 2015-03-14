package managers;

import files.TemplateFileManager;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class TemplateManager {

    private final TemplateFileManager tfm;
    private static TemplateManager tm;

    public static void initTemplateManager(TemplateFileManager tfm) {
        if (tm == null) tm = new TemplateManager(tfm);
    }

    public static TemplateManager getTemplateManager() throws Exception {
        if (tm == null)
            throw new Exception("TemplateManager needs to be instantiated with instance of TemplateFileManager");
        return tm;
    }

    private TemplateManager(TemplateFileManager tfm) {
        this.tfm = tfm;
    }

    public synchronized Workbook getTemplate(String templateName) {
        return tfm.getTemplate(templateName);
    }

    public List<String> getTemplateList() {
        return tfm.getAllTemplates();
    }
}
