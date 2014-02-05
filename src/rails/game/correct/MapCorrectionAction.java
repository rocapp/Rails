package rails.game.correct;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

import rails.game.correct.MapCorrectionManager.*;
import rails.game.BaseToken;
import rails.game.Station;
import rails.game.MapHex;
import rails.game.MapManager;
import rails.game.Tile;
import rails.game.TileManager;
import rails.game.action.PossibleAction;
import rails.util.Util;

public class MapCorrectionAction extends CorrectionAction {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1L;

    /** Sequence: Indicates the enrichment of the action */
    transient private ActionStep step = null;
    private String stepName;

    transient private ActionStep nextStep = null;
    private String nextStepName;
    
    /* Conditions */
    
    /** Location: where to lay the tile */
    transient private MapHex location = null;
    private String locationCoordinates;
    
    /** Tiles: which tile(s) to lay */
    transient private List<Tile> tiles = null;
    private String[] sTileIds;
    // FIXME: Rewrite this with Rails1.x version flag
    private int[] tileIds;
    
    /** Orientation: how to lay the tile */
    private int orientation;
    
    /** RelayBaseTokens: how to relay the base tokens */
    transient private List<BaseToken> tokensToRelay;
    //private String[]tokensToRelayOwner;
    transient private List<Station> stationsForRelay;
    //private int[] stationForRelayId;
    transient private Collection<Station> possibleStations;
    //private int[] possibleStationsId;
                
    /**
     * Instantiates a new map tile correction action.
     * start with select hex
     */
    public MapCorrectionAction() {
        setStep(ActionStep.SELECT_HEX);
        setNextStep(null);
        setCorrectionType(CorrectionType.CORRECT_MAP);
    }
    
    public MapHex getLocation() {
        return location;
    }
    
    private void setLocation(MapHex location) {
        this.location = location;
        locationCoordinates = location.getId(); 
    }
    
    public List<Tile> getTiles() {
        return tiles;
    }
    
    public Tile getChosenTile() {
        if (nextStep.ordinal() > ActionStep.SELECT_TILE.ordinal())
            return tiles.get(0);
        else
            return null;
    }
    
    void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
        this.sTileIds = new String[tiles.size()];
        for (int i = 0; i < tiles.size(); i++)
            sTileIds[i] = tiles.get(i).getId();
    }
    
    public List<Station> getStationsForRelay() {
        return stationsForRelay;
    }
    
    private void setStationsForRelay(List<Station> stations) {
        this.stationsForRelay = stations;
    }

    public List<BaseToken> getTokensToRelay() {
        return tokensToRelay;
    }
    
    void setTokensToRelay(List<BaseToken> tokens) {
        this.tokensToRelay = tokens;
    }
    
    public Collection<Station> getPossibleStations() {
        return possibleStations;
    }
    
    void setPossibleStations(Collection<Station> possibleStations) {
        this.possibleStations = possibleStations;
    }
    
    public int getOrientation(){
        return orientation;
    }
    
    private void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public ActionStep getNextStep() {
        return nextStep;
    }
    
    void setNextStep(ActionStep step) {
        this.nextStep = step;
        if (step == null)
            nextStepName = null;
        else
            nextStepName = step.name();
    }
    
    public ActionStep getStep() {
        return step;
    }
    
    private void setStep(ActionStep step) {
        this.step = step;
        stepName = step.name();
    }
    
    public void selectHex(MapHex chosenHex) {
        setLocation(chosenHex);
        setNextStep(ActionStep.SELECT_TILE);
    }
    
    public void selectTile(Tile chosenTile) {
        List<Tile> tiles = new ArrayList<Tile>();
        tiles.add(chosenTile);
        setTiles(tiles);
        setNextStep(ActionStep.SELECT_ORIENTATION);
    }

    public void selectConfirmed() {
        setNextStep(ActionStep.RELAY_BASETOKENS);
    }
    
    public void selectOrientation(int orientation) {
        setOrientation(orientation);
        setNextStep(ActionStep.RELAY_BASETOKENS);
    }
    
    public void selectRelayBaseTokens(List<Station> chosenStations) {
        setStationsForRelay(chosenStations);
        setNextStep(ActionStep.FINISHED);
    }
    
    public void selectCancel() {
        setNextStep(ActionStep.CANCELLED);
    }
    
    public ActionStep moveToNextStep() {
        setStep(nextStep);
        setNextStep(null);
        if (step != ActionStep.FINISHED)
            this.acted = false;
        return step;
    }
    
    @Override
    public boolean equalsAsOption(PossibleAction action) {
        if (!(action instanceof MapCorrectionAction)) return false;
        // anything is allowed, no restriction
        return true;
    }

    @Override
    public boolean equalsAsAction(PossibleAction action) {
        if (!(action instanceof MapCorrectionAction)) return false;
        MapCorrectionAction a = (MapCorrectionAction) action;
        return (a.step == this.step
                && a.location == location
                && a.tiles.get(0) == tiles.get(0))
                && a.orientation == orientation;
    }

    @Override
    public String toString(){
        StringBuffer b = new StringBuffer("MapCorrectionAction");
        if (acted) {
            b.append(" (acted)");
        } else {
            b.append(" (not acted)");
        }
        b.append(" Step=" + step);
        ActionStep executedStep;
        if (nextStep != null) { 
            b.append(" NextStep=" + nextStep);
            executedStep = nextStep;
        } else {
            executedStep = step;
        }
        if (step.ordinal() > ActionStep.SELECT_HEX.ordinal()) 
            b.append(" Hex=" + location.getId());
        if (step == ActionStep.SELECT_TILE)
            b.append(" Possible tiles=" + tiles);
        if (executedStep.ordinal() >= ActionStep.SELECT_TILE.ordinal())
            b.append(" Chosen tile=" + tiles);
        if (executedStep.ordinal() >= ActionStep.SELECT_ORIENTATION.ordinal())
            b.append(" Orientation=" + orientation);
        if (step.ordinal() >= ActionStep.RELAY_BASETOKENS.ordinal())
            b.append(" Tokens to relay=" + tokensToRelay);
        if (step.ordinal() == ActionStep.RELAY_BASETOKENS.ordinal())
            b.append(" Possible Stations=" + possibleStations);
        if (executedStep.ordinal() >= ActionStep.RELAY_BASETOKENS.ordinal())
            b.append(" Selected stations for relay=" + stationsForRelay);
        return b.toString();
    }
    
    /** Deserialize */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        
        if (Util.hasValue(correctionName))
            correctionType = CorrectionType.valueOf(correctionName);

        if (Util.hasValue(stepName))
            step = ActionStep.valueOf(stepName);

        if (Util.hasValue(nextStepName))
            nextStep = ActionStep.valueOf(nextStepName);
            
        MapManager mmgr = getRoot().getMapManager();
        if (Util.hasValue(locationCoordinates))
            location = mmgr.getHex(locationCoordinates);

        TileManager tmgr = getRoot().getTileManager();
        if (sTileIds != null && sTileIds.length > 0) {
            tiles = new ArrayList<Tile>();
            for (int i = 0; i < sTileIds.length; i++) {
                tiles.add(tmgr.getTile(sTileIds[i]));
            }
        }
        // FIXME: Rewrite this with Rails1.x version flag
        if (tileIds != null && tileIds.length > 0) {
            tiles = new ArrayList<Tile>();
            for (int i = 0; i < tileIds.length; i++) {
                tiles.add(tmgr.getTile(String.valueOf(tileIds[i])));
            }
        }
    }
}