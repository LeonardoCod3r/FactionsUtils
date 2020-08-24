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
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KdrRanking implements Ranking{

    public List<Map.Entry<String, KdrInfo>> orderlyList;

    public List<Map.Entry<String, KdrInfo>> getOrderlyList() {
        return orderlyList;
    }

    public void setOrderlyList(List<Map.Entry<String, KdrInfo>> orderlyList) {
        this.orderlyList = orderlyList;
    }

    public Item getItem(Faction faction) {
        final KdrInfo supplier = getSupplier(faction);
        return new Item(Banners.getAlphabet(faction.getTag().substring(0, 1), DyeColor.BLACK, DyeColor.WHITE))
                .hideAttributes()
                .name("§f" + getPosition(faction) + "º. §8[" + faction.getTag() + "] " + faction.getName())
                .lore(
                        "§fKDR: §7" + supplier.getKDRRounded(),
                        "§fAbates: §7" + supplier.getKills(),
                        "§fMortes: §7" + supplier.getDeaths(),
                        ""
                ).addLines(supplier.getLore());
    }

    public KdrInfo getSupplier(Faction faction) {
        return get(faction.getTag()).getValue();
    }

    public Map.Entry<String, KdrInfo> get(String factionName) {
        return getOrderlyList().stream().filter(entr -> entr.getKey().equalsIgnoreCase(factionName)).findFirst().get();
    }

    @Override
    public void update() {
        final List<Faction> sorted = FactionColl.get().getAll(faction -> faction.getMPlayers().size() > 0 && !Arrays.asList("ZNP", "ZNL", "ZNG", "BPD").contains(faction.getTag()));
        final HashMap<String, KdrInfo> mapSorted = Maps.newHashMap();
        for (Faction faction : sorted) {
            final KdrInfo kdrS = new KdrInfo(faction.getKdr(), faction.getKills(), faction.getDeaths(), (HashMap<String, Double>) faction.getMPlayers().stream().collect(Collectors.toMap(MPlayer::getName, MPlayer::getKdr)));
            mapSorted.put(faction.getTag(), kdrS);
        }
        final List<Map.Entry<String, KdrInfo>> entries = Lists.newArrayList(mapSorted.entrySet());
        entries.sort((o1, o2) -> o2.getValue().getKdr().compareTo(o1.getValue().getKdr()));
        setOrderlyList(entries);
    }

    public void remove(Faction faction) {
        if (has(faction)) {
            final List<Map.Entry<String, KdrInfo>> map = Lists.newArrayList(getOrderlyList());
            map.remove(get(faction.getTag()));
            setOrderlyList(map);
        }
    }

    public boolean has(Faction faction) {
        return getOrderlyList().stream().anyMatch(faction1 -> faction1.getKey().equalsIgnoreCase(faction.getTag()));
    }

    @Override
    public void openInventory(Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        final InventoryBuilder builder = new InventoryBuilder(Main.get(), 6, "§8Ranking Geral - KDR");
        final Faction faction = mPlayer.getFaction();
        builder.clear();
        builder.setCancellable(true);
        if (mPlayer.hasFaction() && has(faction)) builder.setItem(7, getItem(faction));
        else {
            builder.setItem(7, new Item(Material.PAPER).name("§cSem facção.").lore("§7Ranking da sua facção não foi encontrado."));
        }
        builder.setItem(1, Item.gray().name("§fValor").lore("§7Clique para acessar o ranking por valor.").onClick(event -> Main.getWorthRanking().openInventory(p)));
        builder.setItem(2, Item.green().name("§fKDR").lore("§7Você está vendo esse ranking.").onClick(event -> Main.getKdrRanking().openInventory(p)));
        builder.setItem(3, Item.gray().name("§fCoins").lore("§7Clique para acessar o ranking por coins.").onClick(event -> Main.getBalanceRanking().openInventory(p)));
        builder.setItem(4, Item.gray().name("§fGeradores").lore("§7Clique para acessar o ranking por geradores.").onClick(event -> Main.getSpawnerRanking().openInventory(p)));
        builder.setItem(5, Item.gray().name("§fPoder").lore("§7Clique para acessar o ranking por poder.").onClick(event -> Main.getPowerRanking().openInventory(p)));
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


    @Data
    @RequiredArgsConstructor
    public static class KdrInfo implements Comparable<KdrInfo> {

        private Double kdr;
        private Integer kills;
        private Integer deaths;
        private HashMap<String, Double> nameAndKDR;

        public KdrInfo(Double kdr, Integer kills, Integer deaths, HashMap<String, Double> nameAndKDR) {
            this.kdr = kdr;
            this.kills = kills;
            this.deaths = deaths;
            this.nameAndKDR = nameAndKDR;
        }

        public String getKDRRounded() {
            return round(kdr);
        }

        public HashMap<String, Double> getNameAndKDR() {
            return nameAndKDR;
        }

        public String round(Double value) {
            return String.format("%.2f", value);
        }

        public List<String> getLore() {
            return getNameAndKDR().keySet().stream().map(s -> "§f" + s + ": §7" + round(getNameAndKDR().get(s))).collect(Collectors.toList());
        }

        @Override
        public int compareTo(KdrInfo o) {
            return o.getKdr().compareTo(kdr);
        }
    }
}
