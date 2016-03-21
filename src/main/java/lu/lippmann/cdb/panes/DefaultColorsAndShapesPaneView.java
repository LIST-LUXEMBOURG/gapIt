/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import lu.lippmann.cdb.common.gui.*;
import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.graph.renderer.CShape;

import org.jdesktop.swingx.JXTaskPane;

//import com.jgoodies.forms.layout.*;


/**
 * DefaultColorsAndShapesPaneView.
 * 
 * @author the ACORA team
 */
public class DefaultColorsAndShapesPaneView extends JXTaskPane implements Display 
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=123806L;
		
	
	//
	// Instance methods
	//
	
	private Listener<CShape> onShapeSelectListener;
	private Listener<Color> onColorSelectListener;

	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public DefaultColorsAndShapesPaneView() 
	{
		super();
		setTitle("Default colors and shapes");
		setOpaque(false);
		setCollapsed(false);
		initComponent();		
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * 
	 */
	public final void initComponent() 
	{
		/*setLayout(new FormLayout("left:100px, fill:100px",
				"center:20px, center:20px"));*/


		final JColorButton colorBtn = new JColorButton("");
		final ShapeChooser shapeChoose = new ShapeChooser();
		colorBtn.setPreferredSize(new Dimension(50,50));

		//final CellConstraints c = new CellConstraints();

		/*add(new JLabel("Shape"),c.xy(1,1));
		add(shapeChoose,c.xy(2,1));

		add(new JLabel("Color"),c.xy(1,2));
		add(colorBtn,c.xy(2,2));*/


		final JPanel maelPane = new JPanel();
		maelPane.setLayout(new GridBagLayout());
		maelPane.setBackground(getBackground());

		
		setBorder(BorderFactory.createLineBorder(getBackground().darker()));
		
		//bind events
		colorBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				final Color color =JColorChooser.showDialog(
						//GraphViewImpl.this,
						null,
						"Choose vertex background color",
						Color.WHITE);

				if(color!=null){
					if (onColorSelectListener!=null) {
						onColorSelectListener.onAction(color);
					}
					//cadralGraphMouse.setColor(color);
					colorBtn.setColor(color);
					colorBtn.updateUI();
				}
			}
		});
		
		shapeChoose.setOnChangeShapeListener(new Listener<CShape>() {
			@Override
			public void onAction(CShape parameter) {
				if (onShapeSelectListener!=null) {
					onShapeSelectListener.onAction(parameter);
				}
			}
		});
		
		updateUI();
				
	}

	@Override
	public Component asComponent() {
		return this;
	}

	/**
	 * @param onShapeSelectListener the onShapeSelectListener to set
	 */
	public void setOnShapeSelectListener(Listener<CShape> onShapeSelectListener) {
		this.onShapeSelectListener = onShapeSelectListener;
	}


	/**
	 * @param onColorSelectListener the onColorSelectListener to set
	 */
	public void setOnColorSelectListener(Listener<Color> onColorSelectListener) {
		this.onColorSelectListener = onColorSelectListener;
	}

}
