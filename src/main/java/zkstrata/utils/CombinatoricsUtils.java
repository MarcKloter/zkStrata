package zkstrata.utils;

import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;

import java.util.*;
import java.util.stream.Collectors;

public class CombinatoricsUtils {
    private CombinatoricsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns a set containing all possible combinations (lists) of objects according to the order of the provided list
     * of types.
     * <p>
     * Example: Given a list of types [ONE, TWO] and a list of objects [A:ONE, B:ONE, C:TWO], this function will
     * return all possible lists starting with an object of type ONE followed by an object of type TWO. In this case:
     * [A, C], [B, C]. A combination will never contain a objects twice or duplicate combinations (even if differently
     * arranged). If the list of types contains the same type multiple times, such as [ONE, ONE] and the list as before,
     * the only combinations will be: [A, B].
     *
     * @param pattern list of types describing the pattern a combination should conform
     * @param objects list of objects to draw from
     * @return all possible combinations of objects drawn from {@code objects} satisfying the given {@code pattern}
     */
    public static <T> Set<List<T>> getCombinations(List<Class<? extends T>> pattern, List<T> objects) {
        if (pattern.isEmpty())
            return Collections.emptySet();

        int lastIndex = pattern.size() - 1;
        Class<? extends T> type = pattern.get(lastIndex);
        List<Class<? extends T>> remainingTypes = new ArrayList<>(pattern);
        remainingTypes.remove(lastIndex);

        return objects.stream()
                .filter(object -> object.getClass().equals(type))
                .map(object -> {
                    if (remainingTypes.isEmpty()) {
                        List<List<T>> listOfLists = new ArrayList<>();
                        List<T> list = new ArrayList<>();
                        list.add(object);
                        listOfLists.add(list);
                        return listOfLists;
                    } else {
                        List<T> remainingObjects = new ArrayList<>(objects);
                        remainingObjects.remove(object);
                        Set<List<T>> combinations = getCombinations(remainingTypes, remainingObjects);
                        combinations.forEach(list -> list.add(object));
                        return combinations;
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
    
    public static <T> Set<List<T>> getCombinations(List<Class<? extends T>> pattern, Set<T> objects) {
        return getCombinations(pattern, new ArrayList<>(objects));
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
