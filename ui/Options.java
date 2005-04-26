/*
 * Rails: an 18xx game system. Copyright (C) 2005 Brett Lentz
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.util.*;
import game.*;

public class Options extends JFrame implements MouseListener
{
   GridBagConstraints gc;
   JPanel optionsPane, playersPane, buttonPane;
   JButton newButton, loadButton, quitButton;
   JComboBox[] playerBoxes;
   JTextField[] playerNameFields;
   JTextField gameName;
   BasicComboBoxRenderer renderer;
   Dimension size;
   
   private void initialize()
   {
      gc = new GridBagConstraints();
      optionsPane = new JPanel();
      playersPane = new JPanel();
      buttonPane = new JPanel();
      newButton = new JButton("New Game");
      loadButton = new JButton("Load Game");
      quitButton = new JButton("Quit");
      renderer = new BasicComboBoxRenderer();
      size = new Dimension(50,25);
      gameName = new JTextField();
      
      playerBoxes = new JComboBox[Player.MAX_PLAYERS];
      playerNameFields = new JTextField[Player.MAX_PLAYERS];
      
      this.setLayout(new GridBagLayout());
      this.setTitle("Rails: New Game");
      this.setPreferredSize(new Dimension(400,500));
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      renderer.setPreferredSize(size);
      
      playersPane.add(new JLabel("Players:"));
      playersPane.add(new JLabel(""));
      playersPane.setLayout(new GridLayout(Player.MAX_PLAYERS+1,0));
      playersPane.setBorder(BorderFactory.createLoweredBevelBorder());
      
      for(int i=0; i < playerBoxes.length; i++)
      {
         playerBoxes[i] = new JComboBox();
         playerBoxes[i].setRenderer(renderer);
         playerBoxes[i].addItem("None");
         playerBoxes[i].addItem("Human");
         playerBoxes[i].addItem("AI Not Yet Developed.");                 
         playersPane.add(playerBoxes[i]);
         
         playerNameFields[i] = new JTextField();
         playerNameFields[i].setPreferredSize(size);
         playersPane.add(playerNameFields[i]);
      }

      gameName.setText("1830");
      
      optionsPane.add(new JLabel("Options"));
      optionsPane.add(new JLabel(""));
      optionsPane.add(new JLabel("Game:"));
      optionsPane.add(gameName);
      optionsPane.setLayout(new GridLayout(5,2));
      optionsPane.setBorder(BorderFactory.createLoweredBevelBorder());
      
      newButton.addMouseListener(this);
      quitButton.addMouseListener(this);
      
      buttonPane.add(newButton);
      buttonPane.add(loadButton);
      buttonPane.add(quitButton);
      buttonPane.setBorder(BorderFactory.createLoweredBevelBorder());
   }
  
   private void populateGridBag()
   {
      gc.gridx = 0;
      gc.gridy = 0;
      gc.weightx = 1.0;
      gc.weighty = 1.0;
      gc.gridwidth = 2;
      gc.fill = GridBagConstraints.BOTH;
      this.add(playersPane, gc);

      gc.gridx = 1;
      gc.gridy = 1;
      gc.fill = 0;
      gc.weightx = 0.5;
      gc.weighty = 0.5;
      gc.gridwidth = 1;
      gc.ipadx = 400;
      gc.ipady = 50;
      this.add(optionsPane, gc);

      gc.gridx = 0;
      gc.gridy = 2;
      gc.weightx = 0.0;
      gc.weighty = 0.0;
      gc.gridwidth = 2;
      gc.ipady = 0;
      gc.fill = GridBagConstraints.HORIZONTAL;
      this.add(buttonPane, gc);
   }
   
   public Options()
   {
      super();
      
      initialize();
      populateGridBag();
      
      this.pack();
      this.setVisible(true);
   }
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
    */
   public void mouseClicked(MouseEvent arg0)
   {
      if (arg0.getSource().equals(newButton))
      {
         ArrayList playerNames = new ArrayList();
         
         for(int i=0;i < playerBoxes.length; i++)
         {
            if(playerBoxes[i].getSelectedItem().toString().equalsIgnoreCase("Human"))
            {
               playerNames.add(playerNameFields[i].getText());
            }
         }
         
         try
         {
            GameLoader.NewGame(gameName.getText(), playerNames);
         }
         catch(NullPointerException e)
         {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load selected game.");            
         }
      }
      else
      {
         System.exit(0);
      }
      
   }
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
    */
   public void mouseEntered(MouseEvent arg0)
   {
   }
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
    */
   public void mouseExited(MouseEvent arg0)
   {
   }
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
    */
   public void mousePressed(MouseEvent arg0)
   {
   }
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
    */
   public void mouseReleased(MouseEvent arg0)
   {
   }
}
