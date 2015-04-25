package export;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SheetGeneratorTest {

    @Test
    public void shouldCalculateColumnWidth() {
        Sheet mockedSheet = mock(Sheet.class);

        when(mockedSheet.getColumnWidth(anyInt())).thenReturn(200);

        int actual = SheetGenerator.GeneratorUtils.calculateColumnWidth(mockedSheet, 0, "Test");
        int expected = 1536;

        Assert.assertEquals(actual, expected);
    }
}
