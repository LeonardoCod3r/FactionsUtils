package centralworks.factionsutils.modules.ranking;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.lib.Configuration;
import centralworks.factionsutils.lib.InventoryBuilder;
import centralworks.factionsutils.lib.Item;
import centralworks.factionsutils.lib.NPCManager;
import centralworks.factionsutils.modules.commons.banners.Banners;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.massivecraft.factions.entity.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WorthRanking implements Ranking {

    private String first = "";
    private String second = "";
    private String third = "";

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getThird() {
        return third;
    }

    public void setThird(String third) {
        this.third = third;
    }

    public List<Map.Entry<String, Worth>> orderlyList;

    public List<Map.Entry<String, Worth>> getOrderlyList() {
        return orderlyList;
    }

    public void setOrderlyList(List<Map.Entry<String, Worth>> orderlyList) {
        this.orderlyList = orderlyList;
    }

    public Worth getSupplier(Faction faction) {
        return get(faction.getTag()).getValue();
    }

    public Map.Entry<String, Worth> get(String factionName) {
        return getOrderlyList().stream().filter(entr -> entr.getKey().equalsIgnoreCase(factionName)).findFirst().get();
    }

    public Integer getPosition(Faction faction) {
        return getOrderlyList().indexOf(get(faction.getTag())) + 1;
    }

    @Override
    public void update() {
        final Economy eco = Main.getEconomy();
        final SpawnerRanking rg = Main.getSpawnerRanking();
        final List<Faction> list = FactionColl.get().getAll(faction -> !Arrays.asList("ZNP", "ZNL", "ZNG", "BPD").contains(faction.getTag()) && faction.getMPlayers() != null && rg.has(faction));
        final HashMap<String, Worth> map = Maps.newHashMap();
        for (Faction f : list) {
            final Double g1 = rg.getSupplier(f).getColocadosPrice();
            final Double g2 = rg.getSupplier(f).getArmazenadosPrice();
            final Double v1 = f.getMPlayers().stream().map(mPlayer -> eco.getBalance(mPlayer.getName())).reduce(Double::sum).orElse(0.0);
            map.put(f.getTag(), new Worth(v1, g1, g2));
        }
        final List<Map.Entry<String, Worth>> entries = Lists.newArrayList(map.entrySet());
        entries.sort((o1, o2) -> o2.getValue().getTotal().compareTo(o1.getValue().getTotal()));
        setOrderlyList(entries);
        if (entries.isEmpty()) return;
        entries.stream().map(Map.Entry::getKey).collect(Collectors.toList()).forEach(s -> CompletableFuture.supplyAsync(() -> FactionColl.get().getByTag(s))
                .thenAccept(f -> {
                    if (f != null) f.setColor(ChatColor.GRAY);
                }));
        CompletableFuture.runAsync(() -> {
            entries.stream().map(Map.Entry::getKey).collect(Collectors.toList()).forEach(s -> {
                final Faction f = FactionColl.get().getByTag(s);
                if (f != null) f.setColor(ChatColor.GRAY);
            });
            try {
                setFirst(entries.get(0).getKey());
                FactionColl.get().getByTag(getFirst()).setColor(ChatColor.AQUA);
                setSecond(entries.get(1).getKey());
                FactionColl.get().getByTag(getSecond()).setColor(ChatColor.RED);
                setThird(entries.get(2).getKey());
                FactionColl.get().getByTag(getThird()).setColor(ChatColor.GREEN);
            } catch (Exception ignored) {
            }
            Bukkit.getScheduler().runTask(Main.get(), () -> NPCManager.get().update(this));
        });
    }

    public void remove(Faction faction) {
        if (has(faction)) {
            final ArrayList<Map.Entry<String, Worth>> map = Lists.newArrayList(getOrderlyList());
            map.remove(get(faction.getTag()));
            setOrderlyList(map);
        }
    }

    public boolean has(Faction faction) {
        return getOrderlyList().stream().anyMatch(faction1 -> faction1.getKey().equalsIgnoreCase(faction.getTag()));
    }

    public Item getItem(Faction faction) {
        final Economy eco = Main.getEconomy();
        final Worth supplier = getSupplier(faction);
        return new Item(Banners.getAlphabet(faction.getTag().substring(0, 1), DyeColor.BLACK, DyeColor.WHITE))
                .hideAttributes()
                .name("§f" + getPosition(faction) + "º. §8[" + faction.getTag() + "] " + faction.getName())
                .lore(
                        "§fValor total: §2R$ §7" + eco.format(supplier.getTotal()),
                        "§fValor em coins: §2R$ §7" + eco.format(supplier.getPlayersBalance()),
                        "§fValor em geradores: §2R$ §7 " + eco.format(supplier.getGeradoresBalance()),
                        "  §f§l• §fColocados: §2R$ §7" + eco.format(supplier.getPlacedTotal()),
                        "  §f§l• §fArmazenados: §2R$ §7" + eco.format(supplier.getStorageTotal())
                ).addLines(
                        supplier.getTotal() >= Main.getConfiguration().getDouble("Settings.valueToShowCoords") && !BoardColl.get().getChunks(faction).isEmpty()
                                ?
                                supplier.getLore(BoardColl.get().getChunks(faction).stream().findFirst().get().asBukkitChunk().getBlock(1, 100, 1).getLocation())
                                : null);
    }

    @Override
    public void openInventory(Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        final InventoryBuilder builder = new InventoryBuilder(Main.get(), 6, "§8Ranking Geral - Valor");
        final Faction faction = mPlayer.getFaction();
        builder.clear();
        builder.setCancellable(true);
        if (mPlayer.hasFaction() && has(faction))
            builder.setItem(7, getItem(faction));
        else {
            builder.setItem(7, new Item(Material.PAPER).name("§cSem facção.").lore("§7Ranking da sua facção não foi encontrado."));
        }
        builder.setItem(1, Item.green().name("§fValor").lore("§7Você está vendo esse ranking.").onClick(event -> Main.getWorthRanking().openInventory(p)));
        builder.setItem(2, Item.gray().name("§fKDR").lore("§7Clique para acessar o ranking por KDR.").onClick(event -> Main.getKdrRanking().openInventory(p)));
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


    @Data
    @RequiredArgsConstructor
    public static class Worth implements Comparable<Worth> {

        private Double playersBalance;
        private Double placedTotal;
        private Double storageTotal;

        public Worth(Double playersBalance, Double placedTotal, Double storageTotal) {
            this.playersBalance = playersBalance;
            this.placedTotal = placedTotal;
            this.storageTotal = storageTotal;
        }

        public Double getGeradoresBalance() {
            return getStorageTotal() + getPlacedTotal();
        }

        public Double getTotal() {
            return getPlayersBalance() + getGeradoresBalance();
        }

        public List<String> getLore(Location location) {
            final Configuration configuration = Main.getConfiguration();
            if (getTotal() < configuration.getDouble("valueToShowCoords")) return null;
            return Arrays.asList("", "§8Coordenadas:", "  §7X: " + location.getBlockX(), "  §7Z: " + location.getBlockZ());
        }

        @Override
        public int compareTo(Worth o) {
            return o.getTotal().compareTo(getTotal());
        }
    }
}
