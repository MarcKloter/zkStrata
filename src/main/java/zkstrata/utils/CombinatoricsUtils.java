package zkstrata.utils;

import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;

import java.util.*;
import java.util.stream.Collectors;

public class CombinatoricsUtils {
    private CombinatoricsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns a set containing all possible combinations (lists) of {@link Gadget} objects according to the order of
     * the provided list of types.
     * <p>
     * Example: Given a list of types [ONE, TWO] and a list of gadgets [A:ONE, B:ONE, C:TWO], this function will
     * return all possible lists starting with an object of type ONE followed by an object of type TWO. In this case:
     * [A, C], [B, C]. A combination will never contain a gadget twice or duplicate combinations (even if differently
     * arranged). If the list of types contains the same type multiple times, such as [ONE, ONE] and the list as before,
     * the only combinations will be: [A, B].
     *
     * @param pattern list of gadget types describing the pattern a combination should conform
     * @param gadgets set of gadgets to draw objects from
     * @return all possible combinations of objects drawn from {@code gadgets} satisfying the given {@code pattern}
     */
    public static Set<List<Gadget>> getCombinations(List<Class<? extends Gadget>> pattern, Set<Gadget> gadgets) {
        if (pattern.isEmpty())
            return Collections.emptySet();

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
                        Set<Gadget> remainingGadgets = new HashSet<>(gadgets);
                        remainingGadgets.remove(gadget);
                        Set<List<Gadget>> combinations = getCombinations(remainingTypes, remainingGadgets);
                        combinations.forEach(list -> list.add(gadget));
                        return combinations;
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Calculates the cartesian product of the given list of lists {@code lists}.
     * <p>
     * Taken from: https://gist.github.com/max333/3cfafa146aeee29bee2a
     *
     * @param lists {@link List} of {@link List} to calculate the cartesian product for
     * @return {@link List} of {@link List} containing the cartesian product of {@code lists}.
     */
    public static <T> List<List<T>> computeCartesianProduct(List<List<T>> lists) {
        List<List<T>> currentCombinations = Arrays.asList(Arrays.asList());
        for (List<T> list : lists) {
            currentCombinations = appendElements(currentCombinations, list);
        }
        return currentCombinations;
    }

    /**
     * Appends the given {@code extraElements} to every {@link List} in the provided {@code combinations}.
     * <p>
     * Taken from: https://gist.github.com/max333/3cfafa146aeee29bee2a
     *
     * @param combinations  {@link List} of {@link List} to append elements to
     * @param extraElements {@link List} of elements to append to each {@link List} in {@code combinations}
     * @return {@link List} of {@link List} containing the {@code combinations} with {@code extraElements} appended.
     */
    private static <T> List<List<T>> appendElements(List<List<T>> combinations, List<T> extraElements) {
        return combinations.stream().flatMap(oldCombination
                -> extraElements.stream().map(extra -> {
            List<T> combinationWithExtra = new ArrayList<>(oldCombination);
            combinationWithExtra.add(extra);
            return combinationWithExtra;
        })).collect(Collectors.toList());
    }

    /**
     * Checks whether two-sided gadgets have a common witness variable and returns the other two variables.
     *
     * @param left1  left side of first gadget
     * @param right1 right side of first gadget
     * @param left2  left side of second gadget
     * @param right2 right side of second gadget
     * @return {@link List} containing two variables if the given gadgets have a common witness variable,
     * empty list otherwise.
     */
    public static List<Variable> getParity(Variable left1, Variable right1, Variable left2, Variable right2) {
        if (left1 instanceof WitnessVariable) {
            if (left2 instanceof WitnessVariable && left1.equals(left2))
                return List.of(right1, right2);
            if (right2 instanceof WitnessVariable && left1.equals(right2))
                return List.of(right1, left2);
        }

        if (right1 instanceof WitnessVariable) {
            if (left2 instanceof WitnessVariable && right1.equals(left2))
                return List.of(left1, right2);
            if (right2 instanceof WitnessVariable && right1.equals(right2))
                return List.of(left1, left2);
        }

        return Collections.emptyList();
    }

    /**
     * Returns all elements that occur in each of the provided list of lists (intersection of all lists).
     *
     * @param listOfLists {@link List} of {@link List} to intersect
     * @return intersection of elements occurring in each of the lists within the provided list
     */
    public static <T> Set<T> computeIntersection(List<List<T>> listOfLists) {
        if(listOfLists.isEmpty())
            return Collections.emptySet();

        Set<T> commonElements = new HashSet<>(listOfLists.get(0));

        for (int i = 1; i < listOfLists.size(); i++) {
            List<T> list = listOfLists.get(i);
            Set<T> common = new HashSet<>();
            for (T element : list) {
                if (commonElements.contains(element))
                    common.add(element);
            }
            commonElements = common;
        }

        return commonElements;
    }
}
