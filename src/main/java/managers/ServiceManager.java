package managers;

public class ServiceManager {
    public static void initServiceManager() {
        if (sm == null) sm = new ServiceManager();
    }
    
    public static ServiceManager getServiceManager() throws Exception {
        if (sm == null) throw new Exception("Service manager needs to be initialized");
        return sm;
    }

    private static ServiceManager sm;

    private boolean isReady;
    private String status;

    private ServiceManager() {
        isReady = false;
        status = "Not ready";
    }

    public boolean isReady() {
        return isReady;
    }

    public String getStatus() {
        return status;
    }

    public void isReady(boolean ready) {
        isReady = ready;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
