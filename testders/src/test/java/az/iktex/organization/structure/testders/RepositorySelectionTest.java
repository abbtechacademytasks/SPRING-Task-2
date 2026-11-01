package az.iktex.organization.structure.testders;

import az.iktex.organization.structure.testders.component.InMemoryUserRepository;
import az.iktex.organization.structure.testders.component.MockUserRepository;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class RepositorySelectionTest {
    @Test
    void twoImplementationsNeedExplicitSelection() {
        // Bu sınaqda property seçimi olmadan iki bean birbaşa qeyd olunur.
        try (var context = new GenericApplicationContext()) {
            context.registerBean("memory", InMemoryUserRepository.class,
                    () -> new InMemoryUserRepository(new RequestContext()));
            context.registerBean("mock", MockUserRepository.class,
                    () -> new MockUserRepository(new RequestContext()));
            context.refresh();
            assertEquals(2, context.getBeansOfType(UserRepository.class).size());
            assertThrows(NoUniqueBeanDefinitionException.class,
                    () -> context.getBean(UserRepository.class));
        }
    }

    @Test
    void propertySelectsMemoryRepository() {
        repositoryContext().withPropertyValues("app.repository-type=memory").run(context -> {
            assertEquals(1, context.getBeansOfType(UserRepository.class).size());
            UserRepository repository = context.getBean(UserRepository.class);
            assertInstanceOf(InMemoryUserRepository.class, repository);
            assertEquals(3, repository.getAllUsers().size());
        });
    }

    @Test
    void propertySelectsMockRepository() {
        repositoryContext().withPropertyValues("app.repository-type=mock").run(context -> {
            assertEquals(1, context.getBeansOfType(UserRepository.class).size());
            UserRepository repository = context.getBean(UserRepository.class);
            assertInstanceOf(MockUserRepository.class, repository);
            assertTrue(repository.getAllUsers().stream().allMatch(user -> user.getFullName().equals("Mock User")));
        });
    }

    private ApplicationContextRunner repositoryContext() {
        return new ApplicationContextRunner()
                .withUserConfiguration(InMemoryUserRepository.class, MockUserRepository.class)
                .withBean(RequestContext.class, RequestContext::new);
    }
}
