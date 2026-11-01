package az.iktex.organization.structure.testders;

import az.iktex.organization.structure.testders.component.UserAuditLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class PrototypeLifecycleTest {

    @Test
    void prototypeIsNewEachTimeAndIsNotDestroyedOnContextClose(CapturedOutput output) {
        try (var context = new AnnotationConfigApplicationContext(UserAuditLog.class)) {
            UserAuditLog first = context.getBean(UserAuditLog.class);
            UserAuditLog second = context.getBean(UserAuditLog.class);

            assertNotSame(first, second);
            assertNotEquals(first.getAuditId(), second.getAuditId());
            first.setAction("CREATE_USER");
            assertNull(second.getAction());
        }

        assertFalse(output.getOut().contains("UserAuditLog baglandi:"));
    }
}
