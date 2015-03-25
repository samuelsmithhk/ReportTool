package export;

import managers.ExportManager;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import query.Query;

import java.util.List;

public class Email {

    public static void sendEmail(List<Query> queries, List<String> addresses, String subject, String contents) throws Exception {
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

        email.setHostName("smtp.googlemail.com");
        email.setSmtpPort(587);
        email.setAuthenticator(new DefaultAuthenticator("reporttooltest@gmail.com", "kkrkkrkkr"));
        email.setSSLOnConnect(true);
        email.setFrom("reporttooltest@gmail.com");
        email.setSubject(subject);
        email.setMsg(contents);

        email.send();
    }
}
