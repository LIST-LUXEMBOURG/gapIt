/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.awt.*;

import javax.swing.JButton;

/**
 * 
 * @author didry
 *
 */
public class JColorButton extends JButton {
	
		private Color color = Color.WHITE;
		private static final long serialVersionUID = -9195190773578548437L;

		/**
		 * 
		 * @param text
		 */
		public JColorButton(String text) {
			super(text);
		}

		/**
		 * @{inheritDoc}
		 */
		@Override
		public void paint(Graphics g) {
			final Graphics2D g2d = (Graphics2D)g;  
			g2d.setColor(color);
			g2d.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
			g2d.setColor(Color.BLACK);
			g2d.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);  
		}

		/**
		 * 
		 * @param color
		 */
		public void setColor(Color color) {
			this.color = color;
		}
	}