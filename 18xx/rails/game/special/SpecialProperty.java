/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/special/SpecialProperty.java,v 1.25 2010/02/28 21:38:05 evos Exp $ */
package rails.game.special;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rails.game.*;
import rails.game.move.MoveableHolder;
import rails.game.move.ObjectMove;
import rails.game.state.BooleanState;
import rails.util.*;

public abstract class SpecialProperty implements SpecialPropertyI {

    protected CompanyI originalCompany;
    protected MoveableHolder holder = null;
    protected int closingValue = 0;
    protected BooleanState exercised;
    
    /* Usability conditions. Not all of these are already being used. */
    protected boolean usableIfOwnedByPlayer = false;
    protected boolean usableIfOwnedByCompany = false;
    protected boolean usableDuringSR = false;
    protected boolean usableDuringOR = false;
    protected boolean usableDuringTileLayingStep = false;
    protected boolean usableDuringTokenLayingStep = false;

    protected String conditionText = "";
    protected String whenText = "";
    protected String transferText = "";
    protected boolean permanent = false;
    protected boolean isORProperty = false;
    protected boolean isSRProperty = false;

    /** Optional descriptive text, for display in menus and info text.
     * Subclasses may put real text in it.
     */
    protected String description = "";

    protected int uniqueId;

    /** To give subclasses access to the various 'managers' */
    protected GameManagerI gameManager;

    protected static Map<Integer, SpecialPropertyI> spMap =
            new HashMap<Integer, SpecialPropertyI>();
    protected static int lastIndex = 0;

    protected static Logger log =
        Logger.getLogger(SpecialProperty.class.getPackage().getName());

    public SpecialProperty() {
        uniqueId = ++lastIndex;
        spMap.put(uniqueId, this);
        gameManager = GameManager.getInstance();
    }

    public void configureFromXML(Tag tag) throws ConfigurationException {

        conditionText = tag.getAttributeAsString("condition");
        if (!Util.hasValue(conditionText))
            throw new ConfigurationException(
                    "Missing condition in private special property");
        setUsableIfOwnedByPlayer(conditionText.matches("(?i).*ifOwnedByPlayer.*"));
        setUsableIfOwnedByCompany(conditionText.matches("(?i).*ifOwnedByCompany.*"));

        whenText = tag.getAttributeAsString("when");
        if (!Util.hasValue(whenText))
            throw new ConfigurationException(
                    "Missing condition in private special property");
        setUsableDuringSR(whenText.equalsIgnoreCase("anyTurn") 
                || whenText.equalsIgnoreCase("srTurn"));
        setUsableDuringOR(whenText.equalsIgnoreCase("anyTurn") 
                || whenText.equalsIgnoreCase("orTurn"));
        setUsableDuringTileLayingStep(whenText.equalsIgnoreCase("tileLayingStep"));
        setUsableDuringTokenLayingStep(whenText.equalsIgnoreCase("tokenLayingStep"));

        transferText = tag.getAttributeAsString("transfer", "");
        
        // sfy 1889
        permanent = tag.getAttributeAsBoolean("permanent", false);  
    }

    public void finishConfiguration (GameManagerI gameManager)
    throws ConfigurationException {

    }

    public int getUniqueId() {
        return uniqueId;
    }

    public static SpecialPropertyI getByUniqueId(int i) {
        return spMap.get(i);
    }

    public void setCompany(CompanyI company) {
        originalCompany = company;
        holder = company;
        exercised =
                new BooleanState(company.getName() + "_SP_" + uniqueId
                                 + "_Exercised", false);
    }

    public CompanyI getOriginalCompany() {
        return originalCompany;
    }

    public MoveableHolder getHolder() {
        return holder;
    }

    /**
     * @return Returns the usableIfOwnedByCompany.
     */
    public boolean isUsableIfOwnedByCompany() {
        return usableIfOwnedByCompany;
    }

    /**
     * @param usableIfOwnedByCompany The usableIfOwnedByCompany to set.
     */
    public void setUsableIfOwnedByCompany(boolean usableIfOwnedByCompany) {
        this.usableIfOwnedByCompany = usableIfOwnedByCompany;
    }

    /**
     * @return Returns the usableIfOwnedByPlayer.
     */
    public boolean isUsableIfOwnedByPlayer() {
        return usableIfOwnedByPlayer;
    }

    /**
     * @param usableIfOwnedByPlayer The usableIfOwnedByPlayer to set.
     */
    public void setUsableIfOwnedByPlayer(boolean usableIfOwnedByPlayer) {
        this.usableIfOwnedByPlayer = usableIfOwnedByPlayer;
    }
    

    public boolean isUsableDuringOR() {
        return usableDuringOR;
    }

    public void setUsableDuringOR(boolean usableDuringOR) {
        this.usableDuringOR = usableDuringOR;
    }

    public boolean isUsableDuringSR() {
        return usableDuringSR;
    }

    public void setUsableDuringSR(boolean usableDuringSR) {
        this.usableDuringSR = usableDuringSR;
    }

    public boolean isUsableDuringTileLayingStep() {
        return usableDuringTileLayingStep;
    }

    public void setUsableDuringTileLayingStep(boolean usableDuringTileLayingStep) {
        this.usableDuringTileLayingStep = usableDuringTileLayingStep;
    }

    public boolean isUsableDuringTokenLayingStep() {
        return usableDuringTokenLayingStep;
    }

    public void setUsableDuringTokenLayingStep(boolean usableDuringTokenLayingStep) {
        this.usableDuringTokenLayingStep = usableDuringTokenLayingStep;
    }

    public void setExercised() {
        setExercised(true);
    }

    public void setExercised (boolean value) {
        if (permanent) return; // sfy 1889 
        exercised.set(value);
        if (value && originalCompany instanceof PrivateCompanyI) {
            ((PrivateCompanyI)originalCompany).checkClosingIfExercised(false);
        }
    }

    public boolean isExercised() {
        return exercised.booleanValue();
    }

    public int getClosingValue() {
        return closingValue;
    }

    public boolean isSRProperty() {
        return isSRProperty;
    }

    public boolean isORProperty() {
        return isORProperty;
    }

    public String getTransferText() {
        return transferText;
    }

    /**
     * Move the special property to another holder.
     * Only to be used for special properties that have the "transfer" attribute.
     */
    public void moveTo(MoveableHolder newHolder) {
        if (transferText.equals("")) return;
        //if (newHolder instanceof Portfolio) {
            new ObjectMove(this, holder, newHolder);
        //}
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " of private "
               + originalCompany.getName();
    }

    /**
     * Default menu item text, should be by all special properties that can
     * appear as a menu item
     */
    public String toMenu() {
        return toString();
    }

    /** Default Info text. To be overridden where useful. */
    public String getInfo() {
        return toString();
    }

    /** Default Help text: "You can " + the menu description */
    public String getHelp() {
        return LocalText.getText ("YouCan", Util.lowerCaseFirst(toMenu()));

    }
}