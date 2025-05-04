import CLEP.util.Queries;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertThrows;

public class QueriesNoDBTest {
    @Test
    public void testPrepareParams_switchCase() throws SQLException {
        PreparedStatement mockPstmt = mock(PreparedStatement.class);

        Object[] params = {
                42,
                "test",
                new BigDecimal("10.50"),
                new byte[]{1, 2, 3}
        };

        Queries.prepareParams(mockPstmt, params);

        verify(mockPstmt).setInt(1, 42);
        verify(mockPstmt).setString(2, "test");
        verify(mockPstmt).setBigDecimal(3, new BigDecimal("10.50"));
        verify(mockPstmt).setBytes(eq(4), eq(new byte[]{1, 2, 3}));
    }

    @Test
    public void testPrepareParams_switchCaseUnsupported() {
        PreparedStatement mockPstmt = mock(PreparedStatement.class);
        Object[] params = { new Object() };
        assertThrows(SQLException.class, () -> Queries.prepareParams(mockPstmt, params));
    }

}
