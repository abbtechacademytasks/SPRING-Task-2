package az.iktex.organization.structure.testders.component;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

@Component
public class InitializationLogProcessor implements BeanPostProcessor, PriorityOrdered {
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        log("before", bean, beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        log("after", bean, beanName);
        return bean;
    }

    private void log(String stage, Object bean, String beanName) {
        if (ClassUtils.getUserClass(bean).getPackageName().startsWith("az.iktex.")) {
            System.out.println("[Bean " + stage + "] " + beanName);
        }
    }
}
