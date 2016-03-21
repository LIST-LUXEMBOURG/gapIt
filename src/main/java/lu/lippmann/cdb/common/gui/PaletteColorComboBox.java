/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JList;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.renderer.DefaultListRenderer;

public class PaletteColorComboBox extends JXComboBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8686565328790462014L;

	private ColorListCellRenderer renderer = new ColorListCellRenderer();

	private final String palettesName[];
	private final List<Color[]> palettesColors = new ArrayList<Color[]>();

	public PaletteColorComboBox(Map<String, Color[]> palette) {
		palettesName = palette.keySet().toArray(new String[0]);
		Arrays.sort(palettesName);
		int index = 0;
		for (String name : palettesName) {
			palettesColors.add(palette.get(name));
			insertItemAt(name, index++);
		}

		renderer.setPaletteColorComboBox(this);
		setRenderer(renderer);
		repaint();

		setSelectedIndex(0);
	}

	public Color[] getSelectedColors() {
		return getPaletteColors(getSelectedIndex());
	}

	public String getPaletteName(int index) {
		return palettesName[index];
	}

	public Color[] getPaletteColors(int index) {
		return palettesColors.get(index);
	}

	private static class ColorListCellRenderer extends DefaultListRenderer {

		private static final long serialVersionUID = -8551373597631414252L;

		private final ColorComboPanel panel = new ColorComboPanel();

		private PaletteColorComboBox paletteColorComboBox;

		public void setPaletteColorComboBox(
				PaletteColorComboBox paletteColorComboBox) {
			this.paletteColorComboBox = paletteColorComboBox;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (paletteColorComboBox != null) {
				panel.setName(paletteColorComboBox.getPaletteName(index));
				panel.setColors(paletteColorComboBox.getPaletteColors(index));
			}
			return panel;
		}

	};

	private static class ColorComboPanel extends JXPanel {

		private static final long serialVersionUID = 5974421000847099752L;
		private static final int WIDTH = 80;
		private static final int HEIGHT = 40;

		private String name;
		private Color[] colors;

		public ColorComboPanel() {
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			int x = 10, y = 10;

			g.setColor(Color.BLACK);
			g.drawString(name, x, y);
			y += 5;

			for (int i = 0; i < WIDTH; i++) {
				g.setColor(colors[(int) (1.0 * i * colors.length / WIDTH)]);
				g.fillRect(x + i, y, 1, 20);
			}
		}

		public void setColors(Color[] colors) {
			this.colors = colors;
		}

		public void setName(String name) {
			this.name = name;
		}

	};
}
