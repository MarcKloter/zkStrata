package zkstrata.utils;

import zkstrata.domain.gadgets.Gadget;

import java.util.*;
import java.util.stream.Collectors;

public class CombinatoricsUtils {
    private CombinatoricsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns a list containing all possible combinations of {@link Gadget} objects according to the order of the
     * provided list of types.
     *
     * <p>Example: Given a list of types [ONE, TWO] and a list of gadgets [A:ONE, B:ONE, C:TWO], this function will
     * return all possible lists starting with an object of type ONE followed by an object of type TWO. In this case:
     * [A, C], [B, C]. A combination will never contain a gadget twice. If the list of types contains the same type
     * multiple times, such as [ONE, ONE] and the list as before, the combinations will be: [A, B], [B, A].
     *
     * @param pattern list of gadget types describing the pattern a combination should conform
     * @param gadgets list of gadgets to draw objects from
     * @return all possible combinations of objects drawn from {@code gadgets} satisfying the given {@code pattern}
     */
    public static List<List<Gadget>> getCombinations(List<Class<? extends Gadget>> pattern, List<Gadget> gadgets) {
        if (pattern.isEmpty())
            return new ArrayList<>();

        int lastIndex = pattern.size() - 1;
        Class<? extends Gadget> type = pattern.get(lastIndex);
        List<Class<? extends Gadget>> remainingTypes = new ArrayList<>(pattern);
        remainingTypes.remove(lastIndex);

        return gadgets.stream()
                .filter(gadget -> gadget.getClass().equals(type))
                .map(gadget -> {
                    if (remainingTypes.isEmpty()) {
                        List<List<Gadget>> listOfLists = new ArrayList<>();
                        List<Gadget> list = new ArrayList<>();
                        list.add(gadget);
                        listOfLists.add(list);
                        return listOfLists;
                    } else {
                        List<Gadget> remainingGadgets = new ArrayList<>(gadgets);
                        remainingGadgets.remove(gadget);
                        List<List<Gadget>> combinations = getCombinations(remainingTypes, remainingGadgets);
                        combinations.forEach(list -> list.add(gadget));
                        return combinations;
                    }
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static List<List<Gadget>> getCombinations(List<Class<? extends Gadget>> pattern, Set<Gadget> inferences) {
        return getCombinations(pattern, new ArrayList<>(inferences));
    }
}
