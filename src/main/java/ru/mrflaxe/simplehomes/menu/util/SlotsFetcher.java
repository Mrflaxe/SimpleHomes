package ru.mrflaxe.simplehomes.menu.util;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

public class SlotsFetcher {

    public static int[] fetchSlotsArray(ConfigurationSection section, String rangeDelimiter) {
        return fetchSlots(section, rangeDelimiter).stream()
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();
    }
    
    public static Set<Integer> fetchSlots(ConfigurationSection section, String rangeDelimiter) {
        Set<Integer> slots = new LinkedHashSet<>();
        
        // fetch single slot
        if(section.isInt("slot"))
            slots.add(section.getInt("slot"));
        
        // fetch rangeable slots
        if(section.isString("slots")) {
            slots.addAll(fetchSlotsFromRanges(section.getString("slots"), rangeDelimiter));
        } else if(section.isList("slots")) {
            // fetch slots integers list
            List<Integer> slotsIntsList = section.getIntegerList("slots");
            if(slotsIntsList != null && !slotsIntsList.isEmpty())
                slots.addAll(slotsIntsList);

            // fetch slots ranges string list
            List<String> slotsRangesList = section.getStringList("slots");
            if(slotsRangesList != null && !slotsRangesList.isEmpty())
                slotsRangesList.forEach(l -> slots.addAll(fetchSlotsFromRanges(l, rangeDelimiter)));
        }
        
        return slots.stream()
                .filter(i -> i >= 0)
                .collect(Collectors.toSet());
    }
    
    public static Set<Integer> fetchSlotsFromRanges(String rangeString, String rangeDelimiter) {
        String[] ranges = rangeString.split(rangeDelimiter);
        if(ranges == null || ranges.length == 0)
            return Collections.emptySet();
        
        Set<Integer> output = new LinkedHashSet<>();
        
        Arrays.stream(ranges)
                .filter(s -> s != null && !s.isEmpty())
                .forEach(range -> {
                    Integer single = getAsInteger(range);
                    if(single != null) {
                        output.add(single);
                        return;
                    }
                    
                    if(!range.contains("-")) return;
                    
                    String[] rangeParts = range.split("-");
                    if(rangeParts == null || rangeParts.length != 2) return;
                    
                    Integer first = getAsInteger(rangeParts[0]);
                    Integer second = getAsInteger(rangeParts[1]);
                    if(first == null || second == null) return;
                    
                    IntRange intRange = new IntRange(first, second);
                    output.addAll(intRange.itemsSet());
                });
        
        return output.stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private static Integer getAsInteger(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    
}
