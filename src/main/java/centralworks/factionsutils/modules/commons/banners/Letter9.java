// 
// Decompiled by Procyon v0.5.30
// 

package centralworks.factionsutils.modules.commons.banners;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Letter9 extends Letter {
    public Letter9() {
        super(new char[]{'9'});
    }

    @Override
    public ItemStack getBanner(final DyeColor foreGroundColor, final DyeColor backGroundColor, final boolean border) {
        final ItemStack banner = this.getEmptyNamedBanner(this.getLetters()[0]);
        BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();
        bannerMeta.setBaseColor(foreGroundColor);
        bannerMeta.addPattern(new Pattern(backGroundColor, PatternType.HALF_HORIZONTAL));
        bannerMeta.addPattern(new Pattern(foreGroundColor, PatternType.STRIPE_LEFT));
        bannerMeta.addPattern(new Pattern(backGroundColor, PatternType.STRIPE_BOTTOM));
        bannerMeta.addPattern(new Pattern(foreGroundColor, PatternType.STRIPE_TOP));
        bannerMeta.addPattern(new Pattern(foreGroundColor, PatternType.STRIPE_RIGHT));
        if (border) {
            bannerMeta = this.addBorders(bannerMeta, backGroundColor);
        }
        banner.setItemMeta((ItemMeta) bannerMeta);
        return banner;
    }
}
