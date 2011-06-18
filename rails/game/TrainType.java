/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/TrainType.java,v 1.32 2010/05/11 21:47:21 stefanfrey Exp $ */
package rails.game;

import java.util.*;

import org.apache.log4j.Logger;

import rails.game.move.ObjectMove;
import rails.game.state.BooleanState;
import rails.game.state.IntegerState;
import rails.util.*;

public class TrainType implements TrainTypeI {

    public final static int TOWN_COUNT_MAJOR = 2;
    public final static int TOWN_COUNT_MINOR = 1;
    public final static int NO_TOWN_COUNT = 0;

    protected String trainClassName = "rails.game.Train";
    protected Class<? extends Train> trainClass;

    protected String name;
    protected int quantity;
    protected boolean infiniteQuantity = false;

    private String reachBasis = "stops";
    protected boolean countHexes = false;

    private String countTowns = "major";
    protected int townCountIndicator = TOWN_COUNT_MAJOR;

    private String scoreTowns = "yes";
    protected int townScoreFactor = 1;

    private String scoreCities = "single";
    protected int cityScoreFactor = 1;

    protected boolean canBeExchanged = false;
    protected IntegerState numberBoughtFromIPO;

    protected boolean obsoleting = false;

    protected boolean permanent = true;

    private boolean real; // Only to determine if top-level attributes must be
    // read.

    protected int cost;
    protected int majorStops;
    protected int minorStops;
    protected int exchangeCost;

    protected String startedPhaseName = null;
    // Phase startedPhase;

    private Map<Integer, String> rustedTrainTypeNames = null;
    protected Map<Integer, TrainTypeI> rustedTrainType = null;

    private String releasedTrainTypeNames = null;
    protected List<TrainTypeI> releasedTrainTypes = null;

    protected int lastIndex = 0;

    protected BooleanState available;
    protected BooleanState rusted;

    protected TrainManager trainManager;

    /** In some cases, trains start their life in the Pool */
    protected String initialPortfolio = "IPO";

    protected static Logger log =
        Logger.getLogger(TrainType.class.getPackage().getName());

    /**
     * @param real False for the default type, else real. The default type does
     * not have top-level attributes.
     */
    public TrainType(boolean real) {
        this.real = real;
    }

    /**
     * @see rails.game.ConfigurableComponentI#configureFromXML(org.w3c.dom.Element)
     */
    public void configureFromXML(Tag tag) throws ConfigurationException {
        trainClassName = tag.getAttributeAsString("class", trainClassName);
        try {
            trainClass = Class.forName(trainClassName).asSubclass(Train.class);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Class " + trainClassName
                    + "not found", e);
        }

        if (real) {
            // Name
            name = tag.getAttributeAsString("name");
            if (name == null) {
                throw new ConfigurationException(
                        LocalText.getText("NoNameSpecified"));
            }

            // Cost
            cost = tag.getAttributeAsInteger("cost");
            if (cost == 0) {
                throw new ConfigurationException(
                        LocalText.getText("InvalidCost"));
            }

            // Amount
            quantity = tag.getAttributeAsInteger("quantity");
            if (quantity == -1) {
                infiniteQuantity = true;
            } else if (quantity <= 0) {
                throw new ConfigurationException(
                        LocalText.getText("InvalidQuantity", String.valueOf(quantity)));
            } else {
                quantity += tag.getAttributeAsInteger("quantityIncrement", 0);
            }

            // Major stops
            majorStops = tag.getAttributeAsInteger("majorStops");
            if (majorStops == 0) {
                throw new ConfigurationException(
                        LocalText.getText("InvalidStops"));
            }

            // Minor stops
            minorStops = tag.getAttributeAsInteger("minorStops");

            // Phase started
            startedPhaseName = tag.getAttributeAsString("startPhase", "");

            // Train type rusted
            String rustedTrainTypeName1 = tag.getAttributeAsString("rustedTrain");
            if (Util.hasValue(rustedTrainTypeName1)) {
                rustedTrainTypeNames = new HashMap<Integer, String>();
                rustedTrainTypeNames.put(1, rustedTrainTypeName1);
            }

            // Other train type released for buying
            releasedTrainTypeNames = tag.getAttributeAsString("releasedTrain");

            // Can run as obsolete train
            obsoleting = tag.getAttributeAsBoolean("obsoleting");

            // From where is this type initially available
            initialPortfolio =
                tag.getAttributeAsString("initialPortfolio",
                        initialPortfolio);
            
            // Configure any actions on other than the first train of a type
            List<Tag> subs =  tag.getChildren("Sub");
            if (subs != null) {
                for (Tag sub : tag.getChildren("Sub")) {
                    int index = sub.getAttributeAsInteger("index");
                    rustedTrainTypeName1 = sub.getAttributeAsString("rustedTrain");
                    if (rustedTrainTypeNames == null) {
                        rustedTrainTypeNames = new HashMap<Integer, String>();
                    }
                    rustedTrainTypeNames.put(index, rustedTrainTypeName1);
                }
            }
        } else {
            name = "";
            quantity = 0;
        }

        // Reach
        Tag reachTag = tag.getChild("Reach");
        if (reachTag != null) {
            // Reach basis
            reachBasis = reachTag.getAttributeAsString("base", reachBasis);

            // Are towns counted (only relevant is reachBasis = "stops")
            countTowns =
                reachTag.getAttributeAsString("countTowns", countTowns);
        }

        // Score
        Tag scoreTag = tag.getChild("Score");
        if (scoreTag != null) {
            // Reach basis
            scoreTowns =
                scoreTag.getAttributeAsString("scoreTowns", scoreTowns);

            // Are towns counted (only relevant is reachBasis = "stops")
            scoreCities =
                scoreTag.getAttributeAsString("scoreCities", scoreCities);
        }

        // Exchangeable
        Tag swapTag = tag.getChild("Exchange");
        if (swapTag != null) {
            exchangeCost = swapTag.getAttributeAsInteger("cost", 0);
            canBeExchanged = (exchangeCost > 0);
        }

        if (real) {

            // Check the reach and score values
            countHexes = reachBasis.equals("hexes");
            townCountIndicator =
                countTowns.equals("no") ? NO_TOWN_COUNT : minorStops > 0
                        ? TOWN_COUNT_MINOR : TOWN_COUNT_MAJOR;
            cityScoreFactor = scoreCities.equals("double") ? 2 : 1;
            townScoreFactor = scoreTowns.equals("yes") ? 1 : 0;
            // Actually we should meticulously check all values....

        }

        // Final initialisations
        numberBoughtFromIPO = new IntegerState(name + "-trains_Bought", 0);
        available = new BooleanState(name + "-trains_Available", false);
        rusted = new BooleanState(name + "-trains_Rusted", false);
    }

    public void finishConfiguration (GameManagerI gameManager) {

        trainManager = gameManager.getTrainManager();
     }

    public TrainI createTrain () throws ConfigurationException {

        TrainI train;
        try {
            train = trainClass.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException(
                    "Cannot instantiate class " + trainClassName, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Cannot access class "
                    + trainClassName
                    + "constructor", e);
        }
        return train;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean hasInfiniteQuantity() {
        return infiniteQuantity;
    }

    /**
     * @return Returns the cityScoreFactor.
     */
    public int getCityScoreFactor() {
        return cityScoreFactor;
    }

    /**
     * @return Returns the cost.
     */
    public int getCost() {
        return cost;
    }

    /**
     * @return Returns the countHexes.
     */
    public boolean countsHexes() {
        return countHexes;
    }

    /**
     * @return Returns the firstExchange.
     */
    public boolean nextCanBeExchanged() {
        return canBeExchanged/* && numberBoughtFromIPO.intValue() == 0*/;
    }

    public void addToBoughtFromIPO() {
        numberBoughtFromIPO.add(1);
    }

    public int getNumberBoughtFromIPO() {
        return numberBoughtFromIPO.intValue();
    }

    /**
     * @return Returns the firstExchangeCost.
     */
    public int getExchangeCost() {
        return exchangeCost;
    }

    /**
     * @return Returns the majorStops.
     */
    public int getMajorStops() {
        return majorStops;
    }

    /**
     * @return Returns the minorStops.
     */
    public int getMinorStops() {
        return minorStops;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the releasedTrainTypes.
     */
    public List<TrainTypeI> getReleasedTrainTypes() {
        return releasedTrainTypes;
    }

    /**
     * @return Returns the rustedTrainType.
     */
    public TrainTypeI getRustedTrainType(int index) {
        if (rustedTrainType == null) return null;
        return rustedTrainType.get(index);
    }

    /**
     * @return Returns the startedPhaseName.
     */
    public String getStartedPhaseName() {
        return startedPhaseName;
    }

    /**
     * @return Returns the townCountIndicator.
     */
    public int getTownCountIndicator() {
        return townCountIndicator;
    }

    /**
     * @return Returns the townScoreFactor.
     */
    public int getTownScoreFactor() {
        return townScoreFactor;
    }

    /**
     * @return Returns the releasedTrainTypeName.
     */
    public String getReleasedTrainTypeNames() {
        return releasedTrainTypeNames;
    }

    /**
     * @return Returns the rustedTrainTypeName.
     */
    public Map<Integer,String> getRustedTrainTypeNames() {
        return rustedTrainTypeNames;
    }

    public boolean isObsoleting() {
        return obsoleting;
    }

    /**
     * @param releasedTrainType The releasedTrainType to set.
     */
    public void setReleasedTrainTypes(List<TrainTypeI> releasedTrainTypes) {
        this.releasedTrainTypes = releasedTrainTypes;
    }

    /**
     * @param rustedTrainType The rustedTrainType to set.
     */
    public void setRustedTrainType(int index, TrainTypeI rustedTrainType) {
        if (this.rustedTrainType == null) {
            this.rustedTrainType = new HashMap<Integer, TrainTypeI>();
        }
        this.rustedTrainType.put(index, rustedTrainType);
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    /**
     * @return Returns the available.
     */
    public boolean isAvailable() {
        return available.booleanValue();
    }

    /**
     * Make a train type available for buying by public companies.
     */
    public void setAvailable(Bank bank) {
        available.set(true);

        Portfolio to =
            (initialPortfolio.equalsIgnoreCase("Pool") ? bank.getPool()
                    : bank.getIpo());

        for (TrainI train : trainManager.getTrainsOfType(this)) {
            new ObjectMove(train, bank.getUnavailable(), to);
        }
    }

    public void setRusted(Portfolio lastBuyingCompany) {
        rusted.set(true);
        for (TrainI train : trainManager.getTrainsOfType(this)) {
            Portfolio holder = train.getHolder();
            if (obsoleting && holder.getOwner() instanceof PublicCompanyI
                    && holder != lastBuyingCompany) {
                log.debug("Train " + train.getUniqueId() + " (owned by "
                        + holder.getName() + ") obsoleted");
                train.setObsolete();
                holder.getTrainsModel().update();
            } else {
                log.debug("Train " + train.getUniqueId() + " (owned by "
                        + holder.getName() + ") rusted");
                train.setRusted();
            }
        }
    }

    public boolean hasRusted() {
        return rusted.booleanValue();
    }

    @Override
    public Object clone() {

        Object clone = null;
        try {
            clone = super.clone();
            ((TrainType) clone).real = true;
        } catch (CloneNotSupportedException e) {
            log.fatal("Cannot clone traintype " + name, e);
            return null;
        }

        return clone;
    }

    public TrainManager getTrainManager() {
        return trainManager;
    }

    public String getInfo() {
        StringBuilder b = new StringBuilder ("<html>");
        b.append(LocalText.getText("TrainInfo", name, Bank.format(cost), quantity));
        if (Util.hasValue(startedPhaseName)) {
            appendInfoText(b, LocalText.getText("StartsPhase", startedPhaseName));
        }
        if (rustedTrainTypeNames != null) {
            appendInfoText(b, LocalText.getText("RustsTrains", rustedTrainTypeNames.get(1)));
            // Ignore any 'Sub' cases for now
        }
        if (releasedTrainTypeNames != null) {
            appendInfoText(b, LocalText.getText("ReleasesTrains", releasedTrainTypeNames));
        }
        if (b.length() == 6) b.append(LocalText.getText("None"));

        return b.toString();
    }

    private void appendInfoText (StringBuilder b, String text) {
        if (text == null || text.length() == 0) return;
        if (b.length() > 6) b.append("<br>");
        b.append(text);
    }

    @Override
    public String toString() {
        return name;
    }
}
