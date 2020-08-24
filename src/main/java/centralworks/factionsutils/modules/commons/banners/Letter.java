package centralworks.factionsutils.modules.commons.banners;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Letter {
    private static AtomicInteger counter;

    static {
        Letter.counter = new AtomicInteger();
    }

    private char[] letters;

    public Letter(final char[] letters) {
        if (letters[0] == '\0') {
            throw new IllegalArgumentException("letters[0] is 0 in " + this.getClass().getSimpleName() + "!");
        }
        this.letters = letters;
    }

    public static AtomicInteger getCounter() {
        return Letter.counter;
    }

    public char[] getLetters() {
        return this.letters;
    }

    protected ItemStack getEmptyBanner() {
        final ItemStack banner = new ItemStack(Material.BANNER);
        return banner;
    }

    protected ItemStack getEmptyNamedBanner(final char letter) {
        final ItemStack banner = this.getEmptyBanner();
        return banner;
    }

    protected BannerMeta addBorders(final BannerMeta bannerMeta, final DyeColor backGroundColor) {
        bannerMeta.addPattern(new Pattern(backGroundColor, PatternType.BORDER));
        return bannerMeta;
    }

    public abstract ItemStack getBanner(final DyeColor p0, final DyeColor p1, final boolean p2);
}
