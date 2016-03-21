/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.graph.renderer.CShape;

/**
 * 
 * @author
 *
 */
public class ShapeChooser extends JPanel{

	private static final long serialVersionUID = 8524711581874712962L;
	private Listener<CShape> listener;
	private List<JButton> buttons;

	/**
	 * listener for ShapeChooser buttons view
	 * @author wax
	 *
	 */
	private class ShapeChooserButtonListener implements ActionListener {

		private final CShape shape;

		public ShapeChooserButtonListener(CShape shape) {
			this.shape = shape;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (listener!=null) {
				listener.onAction(shape);
			}

			for (JButton button: buttons) {
				button.setSelected(false);
			}

			JButton sourceBtn = (JButton) e.getSource();
			sourceBtn.setSelected(true);
		}

	}

	public ShapeChooser() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new GridLayout(1, 4));
		JButton btn;

		buttons = new LinkedList<JButton>();

		for(final CShape shape : CShape.values()){
			btn = getButtonFor(shape.toString());
			btn.addActionListener(new ShapeChooserButtonListener(shape));
			add(btn);
			buttons.add(btn);
		}

	}

	/**
	 * 
	 * @param string
	 * @return
	 */
	private JButton getButtonFor(final String string) {
		final JButton btn = new JButton(getIconFor(string));
		btn.setPreferredSize(new Dimension(20,20));
		return btn;
	}

	/**
	 * 
	 * @param string
	 * @return
	 */
	private Icon getIconFor(final String string) {
		return ResourceLoader.getAndCacheIcon(string+".png");
	}

	/**
	 * 
	 * @param listener
	 */
	public void setOnChangeShapeListener(Listener<CShape> listener) {
		this.listener = listener;
	}
}
