package webservice;

import com.google.common.collect.Lists;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.util.List;

public class HttpServer {

    private static final String logPath = "logs/web-logs/yyyy_mm_dd.log";
    private static final String webXML = "src/main/resources/META-INF/webapp/WEB-INF/web.xml";
    private static final String onlyInIDE = "webservice.ide";
    private static final String projectPathRelativeWebApp = "src/main/resources/META-INF/webapp";

    public static interface WebContext {
        public File getWarPath();

        public String getContextPath();
    }

    private final Server server;
    private final int port;
    private final String bindInterface;

    public HttpServer(int port) {
        this(port, null);
    }

    public HttpServer(int port, String bindInterface) {
        this.port = port;
        this.bindInterface = bindInterface;

        server = new Server();
    }

    public void start() throws Exception {
        server.setThreadPool(createThreadPool());
        server.addConnector(createConnector());
        server.setHandler(createHandlers());
        server.setStopAtShutdown(true);
        server.start();
    }

    private Handler createHandlers() {
        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath("/");

        if(isRunningInShadedJar()) ctx.setWar(getShadedWarUrl());
        else ctx.setWar(projectPathRelativeWebApp);

        List<Handler> handlers = Lists.newArrayList();

        handlers.add(ctx);

        HandlerList contexts = new HandlerList();
        contexts.setHandlers(handlers.toArray(new Handler[0]));

        RequestLogHandler log = new RequestLogHandler();
        log.setRequestLog(createRequestLog());

        HandlerCollection _result = new HandlerCollection();
        _result.setHandlers(new Handler[] {contexts, log});

        return _result;
    }

    private RequestLog createRequestLog() {
        NCSARequestLog log = new NCSARequestLog();

        File path = new File(logPath);
        path.getParentFile().mkdirs();

        log.setFilename(path.getPath());
        log.setRetainDays(90);
        log.setExtended(false);
        log.setAppend(true);
        log.setLogTimeZone("HKT");
        log.setLogLatency(true);
        return log;
    }

    private String getShadedWarUrl() {
        String urlStr = Thread.currentThread().getContextClassLoader().getResource(webXML).toString();
        return urlStr.substring(0, urlStr.length() - 15);
    }

    private boolean isRunningInShadedJar() {
        try
        {
            Class.forName(onlyInIDE);
            return false;
        }
        catch(ClassNotFoundException anExc)
        {
            return true;
        }
    }

    private Connector createConnector() {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setHost(bindInterface);
        return connector;
    }

    private ThreadPool createThreadPool() {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(10);
        threadPool.setMaxThreads(100);
        return threadPool;
    }

}
