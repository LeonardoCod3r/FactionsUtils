package centralworks.factionsutils.modules.ranking;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.lib.EntityName;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class RankingSupplier implements Comparable<RankingSupplier> {

    private HashMap<EntityType, Integer> placed;
    private HashMap<EntityType, Integer> storage;

    public RankingSupplier(HashMap<EntityType, Integer> placed, HashMap<EntityType, Integer> storage) {
        this.placed = placed;
        this.storage = storage;
    }

    public Integer getColocado(EntityType entityType) {
        return getPlaced().getOrDefault(entityType, 0);
    }

    public Integer getArmazenado(EntityType entityType) {
        return getStorage().getOrDefault(entityType, 0);
    }

    public Double getColocadosPrice() {
        final Double[] value = {0D};
        getPlaced().forEach((entityType, integer) -> value[0] += Main.getSpawnersDetails().get(entityType).getPrice() * integer);
        return value[0];
    }

    public Double getArmazenadosPrice() {
        final Double[] value = {0D};
        getStorage().forEach((entityType, integer) -> value[0] += Main.getSpawnersDetails().get(entityType).getPrice() * integer);
        return value[0];
    }

    public Double getTotal() {
        return getArmazenadosPrice() + getColocadosPrice();
    }

    public HashMap<EntityType, Integer> getAllGeradores() {
        final HashMap<EntityType, Integer> map = Maps.newHashMap(getStorage());
        final HashMap<EntityType, Integer> sps = Maps.newHashMap(getPlaced());
        sps.forEach((entityType, integer) -> {
            if (map.containsKey(entityType)) map.replace(entityType, map.get(entityType) + integer);
            else map.put(entityType, integer);
        });
        return map;
    }

    public boolean hasSpawners() {
        return getSpawnersAmount() > 0;
    }

    public Integer getSpawnersAmount() {
        return getAllGeradores().values().stream().reduce(Integer::sum).orElse(0);
    }

    public Integer getColocadosAmount() {
        return getPlaced().values().stream().reduce(Integer::sum).orElse(0);
    }

    public Integer getArmazenadosAmount() {
        return getStorage().values().stream().reduce(Integer::sum).orElse(0);
    }

    public List<String> getLore() {
        final HashMap<EntityType, Integer> allGeradores = getAllGeradores();
        return allGeradores.keySet().stream().map(entityType -> "§f" + EntityName.valueOf(entityType).getName() + ": §7" + allGeradores.get(entityType)).collect(Collectors.toList());
    }

    public List<String> getLoreColocados() {
        final HashMap<EntityType, Integer> allGeradores = getPlaced();
        return allGeradores.keySet().stream().map(entityType -> "§f" + EntityName.valueOf(entityType).getName() + ": §7" + allGeradores.get(entityType)).collect(Collectors.toList());
    }

    public List<String> getLoreArmazenados() {
        final HashMap<EntityType, Integer> allGeradores = getStorage();
        return allGeradores.keySet().stream().map(entityType -> "§f" + EntityName.valueOf(entityType).getName() + ": §7" + allGeradores.get(entityType)).collect(Collectors.toList());
    }

    @Override
    public int compareTo(RankingSupplier o) {
        return o.getTotal().compareTo(getTotal());
    }
}
