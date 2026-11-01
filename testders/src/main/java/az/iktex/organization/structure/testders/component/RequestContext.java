package az.iktex.organization.structure.testders.component;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Getter
@Component
@RequestScope
public class RequestContext {
    private final String requestId = UUID.randomUUID().toString().substring(0, 8);
    private final long startTime = System.currentTimeMillis();
}
