/**
 * 
 */
package rails.ui.swing.gamespecific._1880;



import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import rails.ui.swing.GameUIManager;
import rails.common.LocalText;
import rails.game.PublicCompanyI;
import rails.game.TrainI;
import rails.game.action.PossibleAction;
import rails.game.action.PossibleORAction;
import rails.game.specific._1880.BuildingRights_1880;
import rails.game.specific._1880.CloseInvestor_1880;
import rails.game.specific._1880.OperatingRound_1880;
import rails.game.specific._1880.ParSlot_1880;
import rails.game.specific._1880.PublicCompany_1880;
import rails.game.specific._1880.SetupNewPublicDetails_1880;
import rails.game.specific._1880.StartCompany_1880;
import rails.ui.swing.elements.NonModalDialog;
import rails.ui.swing.elements.RadioButtonDialog;

/**
 * @author Martin Brumm
 * @date 5-2-2012
 * 
 */
public class GameUIManager_1880 extends GameUIManager {
    public static final String COMPANY_SELECT_BUILDING_RIGHT = "SelectBuildingRight";
    public static final String COMPANY_SELECT_PRESIDENT_SHARE_SIZE = "SelectPresidentShareSize";
    public static final String COMPANY_START_PRICE_DIALOG = "CompanyStartPrice";
    
    public static final String NEW_COMPANY_SELECT_BUILDING_RIGHT = "NewSelectBuildingRight";
    

    @Override
    public void dialogActionPerformed () {

        String key = "";
        String[] presidentShareSizes; 
        if (currentDialog instanceof NonModalDialog) key = ((NonModalDialog) currentDialog).getKey();

        // Check for the dialogs that are postprocessed in this class.
/*
 * The mechanismn for starting a company and getting the necessary decisions by a player 
 * is implemented with the following steps
 *                              Player chooses Startprice
 *                                        |
 *           Player chooses President share percentage (20, 30 or 40 percent share)
 *                                        |
 *           Player chooses Building Right based on percentage of president share
 *           
 *           - 20 percent share will allow to choose from all Building Rights (A+B+C, B+C+D and 2 Phase and single Phase rights)
 *           - 30 percent share will allow to choose from 2 Phase Building Rights (A+B, B+C, C+D and all single Phase rights)
 *           - 40 percent share will limit the player to a building right for one Phase (A, B, C, D) 
 */
        
        if (COMPANY_SELECT_PRESIDENT_SHARE_SIZE.equals(key)) {
        
            RadioButtonDialog dialog = (RadioButtonDialog) currentDialog;
            StartCompany_1880 action = (StartCompany_1880) currentDialogAction;
            String[] possibleBuildingRights;

            int index = dialog.getSelectedOption();
            if (index < 0) {
                currentDialogAction = null;
                return;
            }
            
            int shares = 0;
           
            if (index > 1) { // 40 Percent Share has been chosen
                shares = 4;
            } else if ( index == 1) {
                shares = 3;
            } else {  // 20 Percent Share chosen
                shares = 2;
            }
            
            action.setNumberBought(shares);
            possibleBuildingRights = BuildingRights_1880.getRightsForPresidentShareSize(shares);                

            dialog = new RadioButtonDialog (COMPANY_SELECT_BUILDING_RIGHT,
                    this,
                    statusWindow,
                    LocalText.getText("PleaseSelect"),
                    LocalText.getText(
                            "WhichBuildingRight",action.getPlayerName(),
                            action.getCompanyName()),
                            possibleBuildingRights, -1);
                setCurrentDialog(dialog, action);
                statusWindow.disableButtons();
                return;
            
        } else if (COMPANY_SELECT_BUILDING_RIGHT.equals(key)) {

            RadioButtonDialog dialog = (RadioButtonDialog) currentDialog;
            StartCompany_1880 action = (StartCompany_1880) currentDialogAction;

            String[] possibleBuildingRights = BuildingRights_1880.getRightsForPresidentShareSize(action.getNumberBought());                

            int index = dialog.getSelectedOption();
            if (index < 0) {
                currentDialogAction = null;
                return;
            }
            action.setBuildingRights(possibleBuildingRights[index]);
            
        } else if (COMPANY_START_PRICE_DIALOG.equals(key)
                && currentDialogAction instanceof StartCompany_1880) {

            // A start price has been selected (or not) for a starting major company.
            RadioButtonDialog dialog = (RadioButtonDialog) currentDialog;
            StartCompany_1880 action = (StartCompany_1880) currentDialogAction;

            int index = dialog.getSelectedOption();
            if (index < 0) {
                currentDialogAction = null;
                return;
            }
            
            List<ParSlot_1880> startParSlots = action.getStartParSlots();
            int price = startParSlots.get(index).getPrice();
            int parSlot = startParSlots.get(index).getIndex();
            
            action.setStartPrice(price);
            action.setParSlotIndex(parSlot);
            

            /* Set up another dialog for the next step
            *  need to setup Options based on the Presidents Certificate Size...
            *  The player should only get valid percentages presented to him for selection
            *  This leads to the check what amount of cash does the player have
            */
             
            int freePlayerCash = gameManager.getCurrentPlayer().getFreeCash();
            if (freePlayerCash >= (price*4)) { //enough Cash for 40 Percent 
                presidentShareSizes = new String[] {"20 Percent", "30 Percent", "40 Percent"};
            } else if (freePlayerCash >= (price*3)) { //enough Cash for 30 Percent 
                presidentShareSizes = new String[] {"20 Percent", "30 Percent"};
            } else  { //enough Cash only for 20 Percent 
                presidentShareSizes = new String[] {"20 Percent"};
            }
            dialog = new RadioButtonDialog (COMPANY_SELECT_PRESIDENT_SHARE_SIZE,
                    this,
                    statusWindow,
                    LocalText.getText("PleaseSelect"),
                    LocalText.getText(
                            "WhichPresidentShareSize",
                            action.getPlayerName(),
                            action.getCompanyName()),
                            presidentShareSizes, -1);
                setCurrentDialog(dialog, action);
                statusWindow.disableButtons();
                return;
        } else {
            // Current dialog not found yet, try the superclass.
            super.dialogActionPerformed(false);
            return;
        }

        // Dialog action found and processed, let the superclass initiate processing.
        super.dialogActionPerformed(true);
    
    }
    
    public void setupNewPublicDetails(SetupNewPublicDetails_1880 action) {
        // TODO: Check if this is the right first step
        RadioButtonDialog dialog;
        String[] rightsOptions = BuildingRights_1880.getRightsForPresidentShareSize(action.getShares());
        
        dialog = new RadioButtonDialog (NEW_COMPANY_SELECT_BUILDING_RIGHT,
                this,
                statusWindow,
                LocalText.getText("PleaseSelect"),
                LocalText.getText(
                        "WhichBuildingRight",action.getPlayerName(),
                        action.getCompanyName()),
                        rightsOptions, -1);
            setCurrentDialog(dialog, action);
            statusWindow.disableButtons();
        setCurrentDialog(dialog, action);
        statusWindow.disableButtons();
        return;
    }
    
    public void closeInvestor(CloseInvestor_1880 action) {
        String[] cashOptions = new String[2];
        cashOptions[0] = LocalText.getText("GiveToCompany", action.getInvestor().getCash(), action.getInvestor().getLinkedCompany().getName());
        cashOptions[1] = LocalText.getText("GiveToPresident", (action.getInvestor().getCash()/5), action.getInvestor().getPresident());
        
        String cashChoice =
                (String) JOptionPane.showInputDialog(orWindow,
                        LocalText.getText("FIClosingAskAboutTreasury", action.getInvestor().getName()),
                        LocalText.getText("TreasuryChoice"),
                        JOptionPane.QUESTION_MESSAGE, null,
                        cashOptions, cashOptions[0]);
        if (cashChoice == cashOptions[0]) {
            action.setTreasuryToLinkedCompany(true);
        } else {
            action.setTreasuryToLinkedCompany(false);
        }
        
        String[] tokenOptions = new String[2];
        tokenOptions[0] = LocalText.getText("ReplaceToken", action.getInvestor().getName(), action.getInvestor().getLinkedCompany().getName());
        tokenOptions[1] = LocalText.getText("DoNotReplaceToken", action.getInvestor().getName(), action.getInvestor().getLinkedCompany().getName());
        String tokenChoice =
                (String) JOptionPane.showInputDialog(orWindow,
                        LocalText.getText("FIClosingAskAboutToken"),
                        LocalText.getText("TokenChoice"),
                        JOptionPane.QUESTION_MESSAGE, null,
                        tokenOptions, tokenOptions[0]);
        if (tokenChoice == tokenOptions[0]) {
            action.setReplaceToken(true);
        } else {
            action.setReplaceToken(false);
        }

        orWindow.process(action);
    }
        
}

