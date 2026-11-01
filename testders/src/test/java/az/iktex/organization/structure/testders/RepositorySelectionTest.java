package az.iktex.organization.structure.testders;

import az.iktex.organization.structure.testders.component.InMemoryUserRepository;
import az.iktex.organization.structure.testders.component.MockUserRepository;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class RepositorySelectionTest {
    @Test
    void twoImplementationsNeedExplicitSelection() {
        try (var context = new AnnotationConfigApplicationContext(RequestContext.class,
                InMemoryUserRepository.class, MockUserRepository.class)) {
            assertEquals(2, context.getBeansOfType(UserRepository.class).size());
            assertThrows(NoUniqueBeanDefinitionException.class,
                    () -> context.getBean(UserRepository.class));
        }
    }
}
