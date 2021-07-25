package ru.mrflaxe.simplehomes.menu.util;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class IntRange {

    private final int min;
    private final int max;
    
    public IntRange(int a, int b) {
        this.min = Math.min(a, b);
        this.max = Math.max(a, b);
    }
    
    public int[] items() {
        return itemsStream().toArray();
    }
    
    public Set<Integer> itemsSet() {
        return itemsStream().boxed().collect(Collectors.toSet());
    }
    
    public IntStream itemsStream() {
        return IntStream.range(min, max + 1);
    }
            
    public boolean contains(int i) {
        return min <= i && i <= max;
    }
    
    public boolean contains(IntRange range) {
        return min <= range.min && range.max <= max;
    }
    
    public boolean between(int i) {
        return min < i && i < max;
    }
    
}
