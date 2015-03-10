package files;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class InputFileManagerTest {

    @Test
    public void shouldGetAllInputFiles() throws Exception {
        InputFileManager ifm = new InputFileManager(null, null);
        File[] actual = ifm.getFilesForDirectory("src/test/resources/testInputDirectory/");

        if (actual == null) Assert.fail();
        if (actual.length != 3) Assert.fail();

        List<String> correctNames = Lists.newArrayList();
        correctNames.add("input1.xls");
        correctNames.add("input2.xlsx");
        correctNames.add("input4.xls");

        for (File f : actual) if (!correctNames.contains(f.getName())) Assert.fail();
    }

}
