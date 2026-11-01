package az.iktex.organization.structure.testders;

import az.iktex.organization.structure.testders.component.RecentlyViewedUsers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecentlyViewedUsersTest {

    @Test
    void historyKeepsFiveUniqueIdsNewestFirstAndReturnsSnapshot() {
        var recent = new RecentlyViewedUsers();
        for (long id = 1; id <= 6; id++) {
            recent.addUser(id);
        }
        List<Long> snapshot = recent.getUsers();
        recent.addUser(3L);

        assertEquals(List.of(6L, 5L, 4L, 3L, 2L), snapshot);
        assertEquals(List.of(3L, 6L, 5L, 4L, 2L), recent.getUsers());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(7L));
    }
}
