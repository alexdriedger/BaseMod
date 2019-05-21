package basemod.patches.com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import javassist.CtBehavior;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class CustomActionHooks {
    
    @SuppressWarnings("unused")
    @SpirePatch(clz = GridCardSelectScreen.class, method = "update")
    public static class updatePatch {
        
        @SpireInsertPatch(
                locator = updateLocator.class
        )
        public static void Insert(GridCardSelectScreen __instance) {
            if (GridCardSelectScreenFields.forCustomReward.get(__instance)) {
                ArrayList<AbstractCard> selectedCards = __instance.selectedCards;
                selectedCards.forEach(AbstractCard::stopGlowing);
                
                if (GridCardSelectScreenFields.customCallback.get(__instance) == null) {
                    BaseMod.logger.error("GridCardSelectScreen is being called for a custom reward without a callback");
                }
                
                // Callback that was passed in from `open` is executed here
                GridCardSelectScreenFields.customCallback.get(__instance).callback(selectedCards);
                
                // Reset screen
                __instance.selectedCards.clear();
                GridCardSelectScreenFields.resetFields(__instance);
            }
        }
    }
    
    private static class updateLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(GridCardSelectScreen.class, "forUpgrade");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
    
    @SuppressWarnings("unused")
    @SpirePatch(clz = GridCardSelectScreen.class, method = "reopen")
    public static class reopenPatch {
        public static void Postfix(GridCardSelectScreen __instance) {
            if (GridCardSelectScreenFields.forCustomReward.get(__instance)) {
                AbstractDungeon.overlayMenu.proceedButton.hide();
            }
        }
    }
    
    @SuppressWarnings("unused")
    @SpirePatch(clz = GridCardSelectScreen.class, method = "callOnOpen")
    public static class resetFlagsOnOpen {
        public static void Prefix(GridCardSelectScreen __instance) {
            GridCardSelectScreenFields.resetFields(__instance);
        }
    }
    
    @SuppressWarnings("unused")
    @SpirePatch(clz = AbstractDungeon.class, method = "closeCurrentScreen")
    public static class resetFlagsOnClose {
        @SpireInsertPatch(
                locator = closeCurrentScreenLocator.class
        )
        public static void Insert() {
            if (GridCardSelectScreenFields.forCustomReward.get(AbstractDungeon.gridSelectScreen)) {
                AbstractDungeon.gridSelectScreen.selectedCards.clear();
            }
        }
    }
    
    private static class closeCurrentScreenLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractDungeon.class, "genericScreenOverlayReset");
            return new int[]{LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher)[8]};
        }
    }
}
