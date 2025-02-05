package Aufgabe8;

import java.util.Comparator;
import java.util.List;

public class RecursiveAlgorithm {

    public static List<CubeManipulation> searchArrangements (int n, int m, int k, RatingFunction ratingFunction) {
        if (n < 1) {
            throw new IllegalArgumentException("Number of cubes n has to be at least 1");
        } else if (n == 1) {
            return List.of(CubeManipulation.singleCube());
        } else {
            // gehe rekursiv weiter vor

            List<CubeManipulation> smallerSolutions = searchArrangements(n-1, m, k, ratingFunction);

            List<CubeManipulation> allNewSolutions = smallerSolutions.stream()
                            .flatMap(arr -> arr.addOneCubeAllPossible(m).stream())
                            .toList();



            return selectTopK(allNewSolutions, ratingFunction, k);

        }

        //return null;
    }

    public static List<CubeManipulation> selectTopK(
            List<CubeManipulation> arrangements,
            RatingFunction ratingFunction,
            int k
    ) {
        return arrangements.stream()
                .sorted(Comparator.comparing(ratingFunction::rate).reversed())
                .limit(k)
                .toList();
    }
}
