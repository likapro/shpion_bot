package tgBot.Shpion.entity;

public enum Roles {
    SPY("шпион"),
    PERSON("мирный житель");

    private String name;
    Roles(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
