package az.iktex.organization.structure.testders;

import az.iktex.organization.structure.testders.component.InitializationLogProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class InitializationLogProcessorTest {
    @Test
    void logsOnlyApplicationBeansAndReturnsTheSameObject(CapturedOutput output) {
        InitializationLogProcessor processor = new InitializationLogProcessor();
        Object applicationBean = new SampleBean();
        Object frameworkBean = new Object();

        assertSame(applicationBean, processor.postProcessBeforeInitialization(applicationBean, "sample"));
        assertSame(applicationBean, processor.postProcessAfterInitialization(applicationBean, "sample"));
        assertSame(frameworkBean, processor.postProcessBeforeInitialization(frameworkBean, "outside"));
        processor.postProcessAfterInitialization(frameworkBean, "outside");

        assertTrue(output.getOut().contains("[Bean before] sample"));
        assertTrue(output.getOut().contains("[Bean after] sample"));
        assertFalse(output.getOut().contains("outside"));
    }

    private static class SampleBean {
    }
}
