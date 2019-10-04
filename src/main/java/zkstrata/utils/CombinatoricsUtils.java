package zkstrata.utils;

import zkstrata.domain.gadgets.Gadget;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class CombinatoricsUtils {
    /**
     * Returns a list containing all possible combinations of {@link Gadget} objects according to the order of the
     * provided list of types.
     *
     * <p>Example: Given a list of types [ONE, TWO] and a list of gadgets [A:ONE, B:ONE, C:TWO], this function will
     * return all possible deques starting with an object of type ONE followed by an object of type TWO. In this case:
     * [A, C], [B, C]. A combination will never contain a gadget twice. If the list of types contains the same type
     * multiple times, such as [ONE, ONE] and the list as before, the combinations will be: [A, B], [B, A].
     *
     * @param pattern list of gadget types describing the pattern a combination should conform
     * @param gadgets list of gadgets to draw objects from
     * @return all possible combinations of objects drawn from {@code gadgets} satisfying the given {@code pattern}
     */
    public static List<Deque<Gadget>> getCombinations(List<Class<? extends Gadget>> pattern, List<Gadget> gadgets) {
        if(pattern.isEmpty())
            return new ArrayList<>();

        Class<? extends Gadget> type = pattern.get(0);
        List<Class<? extends Gadget>> remainingTypes = new ArrayList<>(pattern);
        remainingTypes.remove(type);

        return gadgets.stream()
                .filter(gadget -> gadget.getClass().equals(type))
                .map(gadget -> {
                    if (remainingTypes.isEmpty()) {
                        List<Deque<Gadget>> list = new ArrayList<>();
                        Deque<Gadget> deque = new ArrayDeque<>();
                        deque.add(gadget);
                        list.add(deque);
                        return list;
                    } else {
                        List<Gadget> remainingGadgets = new ArrayList<>(gadgets);
                        remainingGadgets.remove(gadget);
                        List<Deque<Gadget>> combinations = getCombinations(remainingTypes, remainingGadgets);
                        combinations.forEach(list -> list.addFirst(gadget));
                        return combinations;
                    }
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
