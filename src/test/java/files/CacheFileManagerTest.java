package files;

import cache.Cache;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import sun.jvm.hotspot.utilities.AssertionFailure;

import java.io.File;
import java.util.List;

/**
 * Created by samuelsmith on 08/11/2014.
 */
public class CacheFileManagerTest {

    @Test
    public void shouldReturnTimestampForFile() {
        CacheFileManager cfm = new CacheFileManager("", 4);

        String test = "/Users/samuelsmith/Desktop/Dev/kkr/20141208194555.cache";

        DateTime actual = cfm.getFileTimestamp(test);
        DateTime expected = new DateTime(2014, 12, 8, 19, 45, 55);

        Assert.assertTrue(actual.equals(expected));

    }

    @Test
    public void shouldGetAllTestCacheFiles() {
        CacheFileManager cfm = new CacheFileManager("src/test/resources/testCacheDirectory/", 1);
        File[] actual = cfm.getAllCaches();

        if (actual == null) Assert.fail();
        if (actual.length != 3) Assert.fail();

        List<String> correctNames = Lists.newArrayList();
        correctNames.add("20150101000000.cache");
        correctNames.add("20150101000001.cache");
        correctNames.add("20150303121312.cache");

        for (File f : actual) if (!correctNames.contains(f.getName())) Assert.fail();
    }

    @Test
    public void shouldGetLatestCacheFile() {
        CacheFileManager cfm = new CacheFileManager("src/test/resources/testCacheDirectory/", 1);
        String actual = cfm.getLatestCache();
        actual = actual.substring(actual.length() - 20);
        String expected = "20150303121312.cache";

        Assert.assertTrue(actual.equals(expected));
    }

    @Test
    public void shouldGetEmptyCache() {
        CacheFileManager cfm = new CacheFileManager("does/not/exist", 1);
        Cache actual = cfm.getCache();

        if (actual.getDeals().size() != 0) Assert.fail();
        if (actual.getCols().size() != 0) Assert.fail();
        if (actual.getLastUpdated() != null) Assert.fail();
    }
}