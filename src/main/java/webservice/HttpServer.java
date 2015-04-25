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
import java.net.URL;
import java.util.List;

public class HttpServer {

    private static final String logPath = "logs/web-logs/yyyy_mm_dd.log";
    private static final String webXML = "webapp/WEB-INF/web.xml";

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


        URL warURL = Thread.currentThread().getContextClassLoader().getResource(webXML);
        assert warURL != null;
        String war = warURL.toString();
        war = war.substring(0, war.length() - 15);
        ctx.setWar(war);

        List<Handler> handlers = Lists.newArrayList();

        handlers.add(ctx);

        HandlerList contexts = new HandlerList();
        contexts.setHandlers(handlers.toArray(new Handler[handlers.size()]));

        RequestLogHandler log = new RequestLogHandler();
        log.setRequestLog(createRequestLog());

        HandlerCollection result = new HandlerCollection();
        result.setHandlers(new Handler[] {contexts, log});

        return result;
    }

    private RequestLog createRequestLog() {
        NCSARequestLog log = new NCSARequestLog();

        File path = new File(logPath);
        //noinspection ResultOfMethodCallIgnored
        path.getParentFile().mkdirs();

        log.setFilename(path.getPath());
        log.setRetainDays(90);
        log.setExtended(false);
        log.setAppend(true);
        log.setLogTimeZone("HKT");
        log.setLogLatency(true);
        return log;
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
