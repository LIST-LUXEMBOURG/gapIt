/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.commandlist;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Enum representing commonly used buttons in applications. Button order is
 * inspired by <a>http://msdn.microsoft.com/en-us/library/aa511268.aspx</a>
 * 
 * @author heinesch
 * 
 */
public enum CommandButton 
{
	
	OK("Ok", -1000, -1000), APPLY("Apply", 0, 0), CANCEL("Cancel", 1000, 1000), CLOSE("Close", 2000, 2000),

	YES("Yes", -900, -900), NO("No", 900, 900),

	STOP("Stop", 0, 0), DETAILS("Details",500,500),

	TRYAGAIN("Retry", -500, -500), SKIP("Skip", 0, 0),

	ADD("Add", -500, -500), EDIT("Edit", -200, 200), REMOVE("Remover", 500, 500),

	OPEN("Open", -500, -500), SAVE("Save", 0, 0),PREVIEW("Preview", 0, 0),

	DELETE("Delete", 0, 0), PRINT("Print", 0, 0), CONTINUE("Continue", -400,
			-400),

	PREVIOUS("<< Previous", -500, -500), NEXT("Next >>", 0, 0), FINISH("Finish", 500,
			500);
	
	/**
	 * Label of the button
	 */
	private String label;
	/**
	 * Float that determines the position of the button relative to the other
	 * buttons in this enum. A smaller value means that the button will more
	 * likely be left in horizontal (or on top in vertical) layout.
	 */
	private float position;
	/**
	 * This value determines which button gets the default focus. A smaller
	 * value is more likely to get it.
	 */
	private float selected;
	
	/**
	 * Default enum constructor.
	 * 
	 * @param label
	 * @param position
	 * @param selected
	 */
	private CommandButton(String label, float position, float selected) {
		this.label = label;
		this.position = position;
		this.selected = selected;
	}
	
	/**
	 * Returns the label of the button
	 * 
	 * @return label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Returns the position value.
	 * 
	 * @return
	 */
	public float getPosition() {
		return position;
	}
	
	/**
	 * Returns the selection value
	 * 
	 * @return
	 */
	public float getSelected() {
		return selected;
	}
	
	/**
	 * Comparator used to determine the positions of the buttons relative to
	 * each other.
	 * 
	 * @author heinesch
	 * 
	 */
	public static final class CommandButtomPositionComparator implements
			Comparator<CommandButton>, Serializable {
		
		private static final long serialVersionUID = -344583637287793620L;
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(CommandButton o1, CommandButton o2) {
			return (int) (o1.getPosition() - o2.getPosition());
		}
		
	}
	
	/**
	 * Comparator used to determine the focused button.
	 * 
	 * @author heinesch
	 * 
	 */
	public static final class CommandButtomSelectionComparator implements
			Comparator<CommandButton>, Serializable {
		
		private static final long serialVersionUID = -3311435799635213559L;
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(CommandButton o1, CommandButton o2) {
			return (int) (o1.getSelected() - o2.getSelected());
		}
		
	}
	
}
