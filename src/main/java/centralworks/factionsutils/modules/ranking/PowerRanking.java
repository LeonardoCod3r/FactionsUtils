package centralworks.factionsutils.modules.ranking;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.lib.InventoryBuilder;
import centralworks.factionsutils.lib.Item;
import centralworks.factionsutils.modules.commons.banners.Banners;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PowerRanking implements Ranking{

    public List<Map.Entry<String, PowerInfo>> orderlyList;

    public List<Map.Entry<String, PowerInfo>> getOrderlyList() {
        return orderlyList;
    }

    public void setOrderlyList(List<Map.Entry<String, PowerInfo>> orderlyList) {
        this.orderlyList = orderlyList;
    }

    @Override
    public void update() {
        final List<Faction> sorted = FactionColl.get().getAll(faction -> !Arrays.asList("ZNP", "ZNL", "ZNG", "BPD").contains(faction.getTag()));
        final HashMap<String, PowerInfo> mapSorted = Maps.newHashMap();
        for (Faction faction : sorted) {
            final PowerInfo poderS = new PowerInfo(faction.getPowerMax(), faction.getPower(), (HashMap<String, Map.Entry<Double, Double>>) faction.getMPlayers().stream().collect(Collectors.toMap(MPlayer::getName, mPlayer -> {
                final HashMap<Double, Double> map = Maps.newHashMap();
                map.put(mPlayer.getPower(), mPlayer.getPowerMax());
                return map.entrySet().stream().findFirst().get();
            })));
            mapSorted.put(faction.getTag(), poderS);
        }
        final List<Map.Entry<String, PowerInfo>> entries = Lists.newArrayList(mapSorted.entrySet());
        entries.sort((o1, o2) -> o2.getValue().getPowerMax().compareTo(o1.getValue().getPowerMax()));
        setOrderlyList(entries);
    }

    public PowerInfo getSupplier(Faction faction) {
        return get(faction.getTag()).getValue();
    }

    public Map.Entry<String, PowerInfo> get(String factionName) {
        return getOrderlyList().stream().filter(entr -> entr.getKey().equalsIgnoreCase(factionName)).findFirst().get();
    }

    public void remove(Faction faction) {
        if (has(faction)) {
            final List<Map.Entry<String, PowerInfo>> map = Lists.newArrayList(getOrderlyList());
            map.remove(get(faction.getTag()));
            setOrderlyList(map);
        }
    }

    public boolean has(Faction faction) {
        return getOrderlyList().stream().anyMatch(faction1 -> faction1.getKey().equalsIgnoreCase(faction.getTag()));
    }

    public Item getItem(Faction faction) {
        final PowerInfo supplier = getSupplier(faction);
        return new Item(Banners.getAlphabet(faction.getTag().substring(0, 1), DyeColor.BLACK, DyeColor.WHITE))
                .hideAttributes()
                .name("§f" + getPosition(faction) + "º. §8[" + faction.getTag() + "] " + faction.getName())
                .lore(
                        "§fPoder da facção: §7" + supplier.getPowerRounded() + "/" + supplier.getPowerMaxRounded(),
                        ""
                ).addLines(supplier.getLore());
    }

    @Override
    public void openInventory(Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        final InventoryBuilder builder = new InventoryBuilder(Main.get(), 6, "§8Ranking Geral - Poder");
        final Faction faction = mPlayer.getFaction();
        builder.clear();
        builder.setCancellable(true);
        if (mPlayer.hasFaction() && has(faction)) builder.setItem(7, getItem(faction));
        else {
            builder.setItem(7, new Item(Material.PAPER).name("§cSem facção.").lore("§7Ranking da sua facção não foi encontrado."));
        }
        builder.setItem(1, Item.gray().name("§fValor").lore("§7Clique para acessar o ranking por valor.").onClick(event -> Main.getWorthRanking().openInventory(p)));
        builder.setItem(2, Item.gray().name("§fKDR").lore("§7Clique para acessar o ranking por KDR.").onClick(event -> Main.getKdrRanking().openInventory(p)));
        builder.setItem(3, Item.gray().name("§fCoins").lore("§7Clique para acessar o ranking por coins.").onClick(event -> Main.getBalanceRanking().openInventory(p)));
        builder.setItem(4, Item.gray().name("§fGeradores").lore("§7Clique para acessar o ranking por geradores.").onClick(event -> Main.getSpawnerRanking().openInventory(p)));
        builder.setItem(5, Item.green().name("§fPoder").lore("§7Você está vendo esse ranking.").onClick(event -> Main.getPowerRanking().openInventory(p)));
        if (getOrderlyList().isEmpty())
            builder.setItem(31, new Item(Material.WEB).name("§cSem resultados").lore("§7Não foi encontrado nenhuma facção.").onClick(event -> p.closeInventory()));
        else {
            final List<Integer> slots = Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
            int count = 0;
            final int tamanho = Math.min(21, getOrderlyList().size());
            while (count < tamanho) {
                final Faction faction1 = FactionColl.get().getByTag(getOrderlyList().get(count).getKey());
                builder.setItem(slots.get(count), getItem(faction1));
                count++;
            }
        }
        builder.open(p);
    }

    @Override
    public Integer getPosition(Faction faction) {
        return getOrderlyList().indexOf(get(faction.getTag())) + 1;
    }

    public static class PowerInfo implements Comparable<PowerInfo> {
        private final Double powerMax;
        private final Double power;
        private final HashMap<String, Map.Entry<Double, Double>> powerAndPowerMax;


        public PowerInfo(Double powerMax, Double power, HashMap<String, Map.Entry<Double, Double>> powerAndPowerMax) {
            this.powerMax = powerMax;
            this.power = power;
            this.powerAndPowerMax = powerAndPowerMax;
        }

        public Integer round(Double value) {
            return Integer.valueOf("" + Math.round(value));
        }

        public String getPowerMaxRounded() {
            return "" + round(getPowerMax());
        }

        public String getPowerRounded() {
            return "" + round(getPower());
        }

        public List<String> getLore() {
            return getPowerAndPowerMax().keySet().stream().map(s -> "§f" + s + ": §7" + round(getPowerAndPowerMax().get(s).getKey()) + "/" + round(getPowerAndPowerMax().get(s).getValue())).collect(Collectors.toList());
        }

        public HashMap<String, Map.Entry<Double, Double>> getPowerAndPowerMax() {
            return powerAndPowerMax;
        }

        public Double getPower() {
            return power;
        }

        public Double getPowerMax() {
            return powerMax;
        }

        @Override
        public int compareTo(PowerInfo o) {
            return o.getPowerMax().compareTo(getPowerMax());
        }
    }

}
