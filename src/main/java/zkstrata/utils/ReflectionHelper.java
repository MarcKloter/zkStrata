package zkstrata.utils;

import org.reflections.Reflections;
import zkstrata.domain.gadgets.Gadget;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionHelper {
    /**
     * Returns all non-abstract classes implementing the {@link Gadget} interface.
     *
     * @return {@link Set} of classes implementing {@link Gadget}
     */
    public static Set<Class<? extends Gadget>> getAllGadgets() {
        Reflections reflections = new Reflections("zkstrata.domain.gadgets.impl");

        Set<Class<? extends Gadget>> gadgets = reflections.getSubTypesOf(Gadget.class);

        return  gadgets
                .stream()
                .filter(gadget -> !Modifier.isAbstract(gadget.getModifiers()))
                .collect(Collectors.toSet());
    }
}
