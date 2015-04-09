package export;

import managers.ExportManager;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import query.Query;

import java.util.List;

public class Email {

    private static Email email;

    public static Email getEmail() throws Exception {
        if (email == null) throw new Exception("Needs to be instantiated with properties");
        return email;
    }

    public static void initEmail(String host, int port, boolean ssl, String from) {
        initEmail(host, port, ssl, from, null, null);
    }

    public static void initEmail(String host, int port, boolean ssl, String from, String username, String password) {
        email = new Email(host, port, ssl, from, username, password);
    }

    private final String host, from, username, password;
    private final int port;
    private final boolean ssl;

    private Email(String host, int port, boolean ssl, String from, String username, String password) {
        this.host = host;
        this.from = from;
        this.username = username;
        this.password = password;
        this.port = port;
        this.ssl = ssl;
    }

    public void sendEmail(List<Query> queries, List<String> addresses, String subject, String contents) throws Exception {
        MultiPartEmail email = new MultiPartEmail();
        ExportManager em = ExportManager.getExportManager();

        for (Query q : queries) {
            List<String> paths = em.getLatestExportPathForQuery(q);
            List<String> names = em.getLatestExportNameForQuery(q);

            for (int i = 0; i < paths.size(); i++) {
                EmailAttachment at = new EmailAttachment();
                at.setPath(paths.get(i));
                at.setDisposition(EmailAttachment.ATTACHMENT);
                at.setDescription(q.name);
                at.setName(names.get(i));

                email.attach(at);
            }
        }

        for (String address : addresses) email.addTo(address);

        email.setHostName(host);
        email.setSmtpPort(port);
        if (username != null && password != null) email.setAuthenticator(new DefaultAuthenticator(username, password));
        email.setSSLOnConnect(ssl);
        email.setFrom(from);
        email.setSubject(subject);
        email.setMsg(contents);

        email.send();
    }
}
