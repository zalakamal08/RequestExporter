package com.burpext.requestexporter.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Returns selection positions (0-based) sorted by ascending request index. */
final class SelectionOrdering {

    private SelectionOrdering() {
    }

    static List<Integer> byAscendingIndex(List<Integer> requestIndices) {
        List<Integer> order = new ArrayList<>(requestIndices.size());
        for (int i = 0; i < requestIndices.size(); i++) {
            order.add(i);
        }
        order.sort(Comparator.comparingInt(requestIndices::get));
        return order;
    }
}
