package org.ginsim.epilog.gui.widgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import org.ginsim.epilog.common.Tuple2D;
import org.ginsim.epilog.core.EpitheliumComponentFeatures;
import org.ginsim.epilog.core.EpitheliumGrid;
import org.ginsim.epilog.gui.color.ColorUtils;

public class VisualGridSimulation extends VisualGrid {
	private static final long serialVersionUID = -3880244278613986980L;

	private EpitheliumComponentFeatures componentFeatures;
	private EpitheliumGrid epiGrid;
	private List<String> lCompON;
	private GridInformation valuePanel;
	private Tuple2D<Integer> lastPos;

	public VisualGridSimulation(EpitheliumGrid epiGrid,
			EpitheliumComponentFeatures componentFeatures,
			List<String> lCompON, GridInformation valuePanel) {
		super(epiGrid.getX(), epiGrid.getY(), epiGrid.getTopology());
		this.componentFeatures = componentFeatures;
		this.epiGrid = epiGrid;
		this.lCompON = lCompON;
		this.valuePanel = valuePanel;

		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				mousePosition2Grid(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mousePosition2Grid(e);
				updateComponentValues(mouseGrid);
				paintComponent(getGraphics());
			}
		});
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				updateComponentValues(mouseGrid);
				paintComponent(getGraphics());
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
	}

	@Override
	protected void applyDataAt(int x, int y) {
		// Does not need to apply data
	}

	private void updateComponentValues(Tuple2D<Integer> pos) {
		if (!isInGrid(pos))
			return;
		this.lastPos = pos;
		this.valuePanel.updateValues(pos.getX(), pos.getY(), this.epiGrid);
	}

	public void setEpitheliumGrid(EpitheliumGrid grid) {
		this.epiGrid = grid;
		this.updateComponentValues(this.lastPos);
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		this.radius = this.topology.computeBestRadius(this.gridX, this.gridY,
				this.getSize().width, this.getSize().height);

		for (int x = 0; x < this.gridX; x++) {
			for (int y = 0; y < this.gridY; y++) {
				BasicStroke stroke = this.strokeBasic;
				if (this.epiGrid.getPerturbation(x, y) != null) {
					stroke = this.strokePerturb;
				}
				List<Color> lColors = new ArrayList<Color>();
				for (String nodeID : this.lCompON) {

					Color cBase = this.componentFeatures.getNodeColor(nodeID);
					byte max = this.componentFeatures.getNodeInfo(nodeID)
							.getMax();

					int index = this.epiGrid.getNodeIndex(x, y, nodeID);
					if (index >= 0) { // if cell has nodeID
						byte value = this.epiGrid.getCellState(x, y)[index];
						if (value > 0) {
							lColors.add(ColorUtils.getColorAtValue(cBase, max,
									value));
						}
					}
				}
				Color cCombined = ColorUtils.combine(lColors);
				Tuple2D<Double> center = topology.getPolygonCenter(this.radius,
						x, y);
				Polygon polygon = topology
						.createNewPolygon(this.radius, center);
				this.paintPolygon(stroke, cCombined, polygon, g2);
				// Highlights the selected cell
				if (this.lastPos != null && this.lastPos.getX() == x
						&& this.lastPos.getY() == y) {
					center = topology.getPolygonCenter(this.radius, x, y);
					polygon = topology
							.createNewPolygon(this.radius / 3, center);
					this.paintPolygon(stroke, ColorUtils.LIGHT_RED, polygon, g2);
				}
			}
		}
	}
}
