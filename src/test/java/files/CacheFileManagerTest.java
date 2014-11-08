package files;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by samuelsmith on 08/11/2014.
 */
public class CacheFileManagerTest {

    @Test
    public void shouldReturnTimestampForFile() {
        CacheFileManager cfm = new CacheFileManager("", 4);

        String test = "/Users/samuelsmith/Desktop/Dev/kkr/20141010.cache";

        DateTime actual = cfm.getFileTimestamp(test);
        DateTime expected = new DateTime(2014, 10, 10, 0, 0);

        Assert.assertTrue(actual.equals(expected));

    }
}