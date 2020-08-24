package centralworks.factionsutils.lib;

public enum Movement {

    STOCK("Armazenar"),
    REMOVE("Remover"),
    STOCK_ALL("Armazenar Todos"),
    COLLECT_ALL("Coletar todos");

    private final String name;

    Movement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
