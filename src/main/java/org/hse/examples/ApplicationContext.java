package org.hse.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Создаёт объекты, необходимые для работы приложения, и обеспечивает к ним доступ
 */
public interface ApplicationContext {

    /** Возвращает контекст приложения */
    static ApplicationContext getContext() {
        return ApplicationContextImpl.INSTANCE;
    }

    /**
     * Возвращает объект из контекста приложения
     *
     * @param name  имя объекта
     * @param clazz ссылка на тип объекта
     * @param <T>   тип объекта
     * @return объект заданного типа, обёрнутый в {@link Optional}, либо пустой {@link Optional},
     *         если объекта с таким именем нет или он другого типа
     */
    <T> Optional<T> getInstance(String name, Class<T> clazz);
}

class ApplicationContextImpl implements ApplicationContext {

    static final ApplicationContextImpl INSTANCE = new ApplicationContextImpl();

    private final Map<String, Object> context = new HashMap<>();

    ApplicationContextImpl() {
        CommissionPolicy commissionPolicy = new RateCommissionPolicy();

        context.put("commissionPolicy", commissionPolicy);
        context.put("orderReportService", new OrderReportService(commissionPolicy));
        context.put("orderFormatter", new OrderFormatter());
    }

    @Override
    public <T> Optional<T> getInstance(String name, Class<T> clazz) {
        Object instance = context.get(name);

        return clazz.isInstance(instance) ? Optional.of(clazz.cast(instance)) : Optional.empty();
    }
}
