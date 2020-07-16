package net.ivanhjc.utility.model.enums;

public enum Season {
    SPRING("Warm"), SUMMER("Sunny"), AUTUMN("Cool"), WINTER("Cold");

    private String desc;

    Season(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
