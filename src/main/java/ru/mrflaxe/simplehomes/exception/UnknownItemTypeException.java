package ru.mrflaxe.simplehomes.exception;

import lombok.Getter;

@Getter
public class UnknownItemTypeException extends ItemStackParsingException {

    private static final String FORMAT = "unknown item type '%s'";
    
    private final String itemType;
    
    public UnknownItemTypeException(String itemType) {
        this(itemType, null);
    }
    
    public UnknownItemTypeException(String itemType, Throwable throwable) {
        super(String.format(FORMAT, itemType), throwable, false, false);
        this.itemType = itemType;
    }

}
