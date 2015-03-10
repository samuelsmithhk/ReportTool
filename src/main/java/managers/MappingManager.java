package managers;


import files.MappingFileManager;
import mapping.CRGMapping;
import mapping.ICDateMapping;
import mapping.Mapping;

public class MappingManager {

    private static MappingManager mm;

    public static void initMappingManager(MappingFileManager mfm) {
        if (mm == null) mm = new MappingManager(mfm);
    }

    public static MappingManager getMappingManager() throws Exception {
        if (mm == null) throw new Exception("MappingManager needs to be initialised with an instance of MappingFileManager");
        return mm;
    }

    private final MappingFileManager mfm;

    private MappingManager(MappingFileManager mfm) {
        this.mfm = mfm;
    }

    public Mapping loadColumnMap(String mapName) throws Exception {
        return mfm.loadColumnMap(mapName);
    }

    public CRGMapping loadCagMap() throws Exception {
        return mfm.loadCagMap();
    }

    public ICDateMapping loadICDateMap() throws Exception {
        return mfm.loadICDateMap();
    }
}
