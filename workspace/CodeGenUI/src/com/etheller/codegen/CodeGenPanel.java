package com.etheller.codegen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.etheller.codegen.model.Block;
import com.etheller.codegen.model.BlockConnection;
import com.etheller.codegen.model.BlockType;
import com.etheller.codegen.model.BlockTypeConnection;
import com.etheller.codegen.model.ConnectionType;

public class CodeGenPanel extends JPanel {
	private static final BasicStroke STROKE_SIZE_FOUR = new BasicStroke(4);
	private static final Dimension DEFAULT_BLOCK_DIMENSIONS = new Dimension(250, 75);
	private static final int BLOCK_HEADER_TITLE_HEIGHT = 25;
	private static final int BLOCK_CONNECTION_ROW_HEIGHT = 25;
	private static final int BLOCK_CONNECTION_CIRCLE_SIZE = 8;

	private final JPopupMenu rightClickMenu;
	private final List<GuiBlock> guiBlocks = new ArrayList<>();
	private final Map<Block, GuiBlock> blockToGui = new HashMap<>();
	private Point lastClickPoint;
	private Point lastDragPoint;
	private Point lastMouseDownPoint;
	private GuiBlock hoveringBlock;
	private int hoveringBlockConnection;
	private GuiBlock dragDestinationBlock;
	private int dragDestinationBlockConnection;
	private final Font boldFont;
	private final Font normalFont;

	public CodeGenPanel() {
		setPreferredSize(new Dimension(1920, 1080));
		setBackground(Color.DARK_GRAY);

		rightClickMenu = new JPopupMenu();
		final JMenu newBlock = new JMenu("New Block");
		rightClickMenu.add(newBlock);
		final BlockTypeListener blockTypeListener = blockType -> {
			createBlock(blockType, lastClickPoint);
			repaint();
		};
		newBlock.add(new NewBlockTypedMenuItem(new BlockType("Main", Arrays.asList(),
				Arrays.asList(new BlockTypeConnection("-", ConnectionType.CONTROL_FLOW))), blockTypeListener));
		newBlock.add(
				new NewBlockTypedMenuItem(
						new BlockType("If / Else",
								Arrays.asList(new BlockTypeConnection("-", ConnectionType.CONTROL_FLOW),
										new BlockTypeConnection("Condition", ConnectionType.BOOLEAN)),
								Arrays.asList(new BlockTypeConnection("Then", ConnectionType.CONTROL_FLOW),
										new BlockTypeConnection("Else", ConnectionType.CONTROL_FLOW))),
						blockTypeListener));
		newBlock.add(new NewBlockTypedMenuItem(
				new BlockType("If",
						Arrays.asList(new BlockTypeConnection("-", ConnectionType.CONTROL_FLOW),
								new BlockTypeConnection("Condition", ConnectionType.BOOLEAN)),
						Arrays.asList(new BlockTypeConnection("Then", ConnectionType.CONTROL_FLOW))),
				blockTypeListener));
		newBlock.add(
				new NewBlockTypedMenuItem(
						new BlockType("Print",
								Arrays.asList(new BlockTypeConnection("-", ConnectionType.CONTROL_FLOW),
										new BlockTypeConnection("Text", ConnectionType.STRING)),
								Arrays.asList(new BlockTypeConnection("-", ConnectionType.CONTROL_FLOW))),
						blockTypeListener));
		newBlock.add(new NewBlockTypedMenuItem(new BlockType("True", Arrays.asList(),
				Arrays.asList(new BlockTypeConnection("True", ConnectionType.BOOLEAN))), blockTypeListener));
		newBlock.add(new NewBlockTypedMenuItem(new BlockType("False", Arrays.asList(),
				Arrays.asList(new BlockTypeConnection("False", ConnectionType.BOOLEAN))), blockTypeListener));

		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (dragDestinationBlock != null) {
					hoveringBlock.setConnection(hoveringBlockConnection, new BlockConnection(dragDestinationBlock.block,
							dragDestinationBlock.fixIndex(dragDestinationBlockConnection)));
					dragDestinationBlock.setConnection(dragDestinationBlockConnection,
							new BlockConnection(hoveringBlock.block, hoveringBlock.fixIndex(hoveringBlockConnection)));
				}
				lastDragPoint = null;
				dragDestinationBlock = null;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				lastMouseDownPoint = e.getPoint();
			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					lastClickPoint = e.getPoint();
					rightClickMenu.show(CodeGenPanel.this, e.getX(), e.getY());
				}
			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				final Point hoverPoint = e.getPoint();
				GuiBlock newHoverBlock = null;
				int newHoverBlockConnection = -1;
				for (final GuiBlock guiBlock : guiBlocks) {
					final boolean hovering = guiBlock.bounds.contains(hoverPoint);
					if (hovering) {
						newHoverBlock = guiBlock;
						newHoverBlockConnection = guiBlock.checkConnectionCircleHover(hoverPoint);
					}
				}
				if (newHoverBlock != hoveringBlock) {
					hoveringBlock = newHoverBlock;
					repaint();
				}
				if (newHoverBlockConnection != hoveringBlockConnection) {
					hoveringBlockConnection = newHoverBlockConnection;
					repaint();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (hoveringBlock != null) {
					final Point newPoint = e.getPoint();
					if (lastDragPoint != null) {
						if (hoveringBlockConnection != -1) {
							GuiBlock newHoverBlock = null;
							int newHoverBlockConnection = -1;
							for (final GuiBlock guiBlock : guiBlocks) {
								final boolean hovering = guiBlock.bounds.contains(lastDragPoint);
								if (hovering) {
									newHoverBlock = guiBlock;
									newHoverBlockConnection = guiBlock.checkConnectionCircleHover(lastDragPoint);
								}
							}
							if (newHoverBlockConnection != -1) {
								final ConnectionDirection destinationDirection = newHoverBlock
										.getConnectionDirection(newHoverBlockConnection);
								final ConnectionDirection sourceDirection = hoveringBlock
										.getConnectionDirection(hoveringBlockConnection);
								final ConnectionType destinationType = newHoverBlock
										.getConnectionType(newHoverBlockConnection).getConnectionType();
								final ConnectionType sourceType = hoveringBlock
										.getConnectionType(hoveringBlockConnection).getConnectionType();
								if ((sourceDirection != null) && (destinationDirection != null)
										&& (sourceDirection != destinationDirection)
										&& (sourceType == destinationType)) {
									dragDestinationBlock = newHoverBlock;
									dragDestinationBlockConnection = newHoverBlockConnection;
								}
								else {
									dragDestinationBlock = null;
									dragDestinationBlockConnection = -1;
								}
							}
							else {
								dragDestinationBlock = null;
								dragDestinationBlockConnection = -1;
							}
						}
						else {
							hoveringBlock.move(newPoint.x - lastDragPoint.x, newPoint.y - lastDragPoint.y);
						}
						repaint();
					}
					lastDragPoint = newPoint;
				}
			}
		});
		normalFont = getFont();
		boldFont = normalFont.deriveFont(Font.BOLD);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		for (final GuiBlock guiBlock : guiBlocks) {
			guiBlock.drawBlock(g2, boldFont, normalFont, guiBlock == hoveringBlock, hoveringBlockConnection);
		}
		if ((hoveringBlock != null) && (hoveringBlockConnection != -1) && (lastDragPoint != null)) {
			final BlockTypeConnection connection = hoveringBlock.getConnectionType(hoveringBlockConnection);
			g.setColor(connection.getConnectionType().getColor());

			final Stroke oldStroke = g2.getStroke();
			final boolean destinationOk = (dragDestinationBlock != null) && (dragDestinationBlockConnection != -1);
			if (destinationOk) {
				g2.setStroke(STROKE_SIZE_FOUR);
			}
			drawSLine(g, lastMouseDownPoint.x, lastMouseDownPoint.y, lastDragPoint.x, lastDragPoint.y);
			if (destinationOk) {
				g2.setStroke(oldStroke);
			}
		}
	}

	private static void drawSLine(Graphics g, int x1, int y1, int x2, int y2) {
		final int xMidpoint = (x1 + x2) / 2;
		g.drawLine(x1, y1, xMidpoint, y1);
		g.drawLine(xMidpoint, y1, xMidpoint, y2);
		g.drawLine(xMidpoint, y2, x2, y2);
	}

	private void createBlock(BlockType blockType, Point blockPoint) {
		final Block block = new Block(blockType);
		final GuiBlock guiBlock = new GuiBlock(block, blockPoint);
		guiBlocks.add(guiBlock);
		blockToGui.put(block, guiBlock);
	}

	private static final class NewBlockTypedMenuItem extends JMenuItem {
		private final BlockType blockType;

		public NewBlockTypedMenuItem(BlockType blockType, BlockTypeListener blockTypeListener) {
			super(blockType.getName());
			this.blockType = blockType;
			addActionListener(e -> {
				blockTypeListener.newBlock(blockType);
			});
		}
	}

	private static interface BlockTypeListener {
		void newBlock(BlockType blockType);
	}

	private final class GuiBlock {
		private final Block block;
		private final Color color;
		private final Rectangle bounds;

		public GuiBlock(Block block, Point blockPoint) {
			this.block = block;
			final BlockType blockType = block.getBlockType();
			if (blockType.getInwardConnections().isEmpty()) {
				if ((blockType.getOutwardConnections().size() == 1)
						&& (blockType.getOutwardConnections().get(0).getConnectionType() == ConnectionType.BOOLEAN)) {
					color = Color.GREEN;
				}
				else {
					color = Color.RED;

				}
			}
			else {
				color = Color.BLUE.darker();
			}
			final Dimension dimension = new Dimension(DEFAULT_BLOCK_DIMENSIONS);

			final int projectedHeight = 26 + Math.max(blockType.getInwardConnections().size() * 25,
					blockType.getOutwardConnections().size() * 25);
			dimension.height = Math.max(projectedHeight, dimension.height);

			bounds = new Rectangle(blockPoint, dimension);
		}

		public void move(int x, int y) {
			bounds.translate(x, y);
		}

		public int checkConnectionCircleHover(Point mousePoint) {
			final int inwardConnectionCount = block.getInwardConnections().length;
			final int outwardConnectionCount = block.getOutwardConnections().length;
			for (int i = 0; i < inwardConnectionCount; i++) {
				final BlockConnection blockConnection = block.getInwardConnections()[i];
				final BlockTypeConnection blockTypeConnection = block.getBlockType().getInwardConnections().get(i);
				final int inwardRowY = bounds.y + BLOCK_HEADER_TITLE_HEIGHT + (BLOCK_CONNECTION_ROW_HEIGHT * i);
				final Rectangle circleBounds = new Rectangle(bounds.x + (BLOCK_CONNECTION_CIRCLE_SIZE / 2),
						inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT - BLOCK_CONNECTION_CIRCLE_SIZE) / 2),
						BLOCK_CONNECTION_CIRCLE_SIZE, BLOCK_CONNECTION_CIRCLE_SIZE);
				if (circleBounds.contains(mousePoint)) {
					return i;
				}
			}
			for (int i = 0; i < outwardConnectionCount; i++) {
				final BlockConnection blockConnection = block.getOutwardConnections()[i];
				final BlockTypeConnection blockTypeConnection = block.getBlockType().getOutwardConnections().get(i);
				final int inwardRowY = bounds.y + BLOCK_HEADER_TITLE_HEIGHT + (BLOCK_CONNECTION_ROW_HEIGHT * i);
				final Rectangle circleBounds = new Rectangle(
						(bounds.x + bounds.width) - ((BLOCK_CONNECTION_CIRCLE_SIZE * 3) / 2),
						inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT - BLOCK_CONNECTION_CIRCLE_SIZE) / 2),
						BLOCK_CONNECTION_CIRCLE_SIZE, BLOCK_CONNECTION_CIRCLE_SIZE);
				if (circleBounds.contains(mousePoint)) {
					return i + inwardConnectionCount;
				}
			}
			return -1;
		}

		public BlockConnection getConnection(int i) {
			final int inwardConnectionCount = block.getInwardConnections().length;
			final int outwardConnectionCount = block.getOutwardConnections().length;
			if (i < inwardConnectionCount) {
				return block.getInwardConnections()[i];
			}
			else if ((i - inwardConnectionCount) < outwardConnectionCount) {
				return block.getOutwardConnections()[i - inwardConnectionCount];
			}
			return null;
		}

		public void setConnection(int i, BlockConnection blockConnection) {
			final int inwardConnectionCount = block.getInwardConnections().length;
			final int outwardConnectionCount = block.getOutwardConnections().length;
			if (i < inwardConnectionCount) {
				block.getInwardConnections()[i] = blockConnection;
			}
			else if ((i - inwardConnectionCount) < outwardConnectionCount) {
				block.getOutwardConnections()[i - inwardConnectionCount] = blockConnection;
			}
		}

		public BlockTypeConnection getConnectionType(int i) {
			final int inwardConnectionCount = block.getBlockType().getInwardConnections().size();
			final int outwardConnectionCount = block.getBlockType().getOutwardConnections().size();
			if (i < inwardConnectionCount) {
				return block.getBlockType().getInwardConnections().get(i);
			}
			else if ((i - inwardConnectionCount) < outwardConnectionCount) {
				return block.getBlockType().getOutwardConnections().get(i - inwardConnectionCount);
			}
			return null;
		}

		public Point getInwardConnectionPoint(int i) {
			final int inwardRowY = bounds.y + BLOCK_HEADER_TITLE_HEIGHT + (BLOCK_CONNECTION_ROW_HEIGHT * i);
			return new Point(bounds.x + (BLOCK_CONNECTION_CIRCLE_SIZE / 2),
					inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT) / 2));
		}

		public Point getOutwardConnectionPoint(int i) {
			final int inwardRowY = bounds.y + BLOCK_HEADER_TITLE_HEIGHT + (BLOCK_CONNECTION_ROW_HEIGHT * i);
			return new Point((bounds.x + bounds.width) - ((BLOCK_CONNECTION_CIRCLE_SIZE) / 2),
					inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT) / 2));
		}

		public ConnectionDirection getConnectionDirection(int i) {
			final int inwardConnectionCount = block.getBlockType().getInwardConnections().size();
			final int outwardConnectionCount = block.getBlockType().getOutwardConnections().size();
			if (i < inwardConnectionCount) {
				return ConnectionDirection.INWARD;
			}
			else if ((i - inwardConnectionCount) < outwardConnectionCount) {
				return ConnectionDirection.OUTWARD;
			}
			return null;
		}

		public int fixIndex(int i) {
			final int inwardConnectionCount = block.getBlockType().getInwardConnections().size();
			final int outwardConnectionCount = block.getBlockType().getOutwardConnections().size();
			if (i < inwardConnectionCount) {
				return i;
			}
			else if ((i - inwardConnectionCount) < outwardConnectionCount) {
				return i - inwardConnectionCount;
			}
			return -1;
		}

		public void drawBlock(Graphics2D g, Font boldFont, Font normalFont, boolean highlight,
				int hoveringBlockConnection) {
			g.setColor(Color.BLACK);
			g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
			g.setColor(color.darker().darker());
			g.fillRoundRect(bounds.x, bounds.y, bounds.width, BLOCK_HEADER_TITLE_HEIGHT, 14, 14);
			g.setColor((highlight && (hoveringBlockConnection == -1)) ? Color.CYAN : color);
			g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
			g.drawLine(bounds.x, bounds.y + BLOCK_HEADER_TITLE_HEIGHT, bounds.x + bounds.width,
					bounds.y + BLOCK_HEADER_TITLE_HEIGHT);
			g.setColor(Color.WHITE);
			g.setFont(boldFont);
			final FontMetrics boldFontMetrics = g.getFontMetrics();
			final String name = block.getBlockType().getName();
			final int boldFontHeight = boldFontMetrics.getHeight();
			g.drawString(name, bounds.x + ((bounds.width - boldFontMetrics.stringWidth(name)) / 2),
					bounds.y + boldFontHeight);

			g.setFont(normalFont);
			final FontMetrics fontMetrics = g.getFontMetrics();
			final int fontHeight = fontMetrics.getAscent();
			final int inwardConnectionCount = block.getInwardConnections().length;
			final int outwardConnectionCount = block.getOutwardConnections().length;
			for (int i = 0; i < inwardConnectionCount; i++) {
				final BlockConnection blockConnection = block.getInwardConnections()[i];
				final BlockTypeConnection blockTypeConnection = block.getBlockType().getInwardConnections().get(i);
				final int inwardRowY = bounds.y + BLOCK_HEADER_TITLE_HEIGHT + (BLOCK_CONNECTION_ROW_HEIGHT * i);
				if (blockConnection != null) {
					g.setColor(blockTypeConnection.getConnectionType().getColor().darker());
					g.fillOval(bounds.x + (BLOCK_CONNECTION_CIRCLE_SIZE / 2),
							inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT - BLOCK_CONNECTION_CIRCLE_SIZE) / 2),
							BLOCK_CONNECTION_CIRCLE_SIZE, BLOCK_CONNECTION_CIRCLE_SIZE);
				}
				g.setColor(blockTypeConnection.getConnectionType().getColor());
				g.drawOval(bounds.x + (BLOCK_CONNECTION_CIRCLE_SIZE / 2),
						inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT - BLOCK_CONNECTION_CIRCLE_SIZE) / 2),
						BLOCK_CONNECTION_CIRCLE_SIZE, BLOCK_CONNECTION_CIRCLE_SIZE);
				if (hoveringBlockConnection == i) {
					g.setColor(Color.CYAN);
					g.drawOval(bounds.x + (BLOCK_CONNECTION_CIRCLE_SIZE / 2) + 1,
							inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT - BLOCK_CONNECTION_CIRCLE_SIZE) / 2) + 1,
							BLOCK_CONNECTION_CIRCLE_SIZE - 2, BLOCK_CONNECTION_CIRCLE_SIZE - 2);
				}
				g.setColor(Color.WHITE);
				g.drawString(blockTypeConnection.getName(),
						bounds.x + (BLOCK_CONNECTION_CIRCLE_SIZE) + BLOCK_CONNECTION_CIRCLE_SIZE,
						inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT + fontHeight) / 2));
			}
			for (int i = 0; i < outwardConnectionCount; i++) {
				final BlockConnection blockConnection = block.getOutwardConnections()[i];
				final BlockTypeConnection blockTypeConnection = block.getBlockType().getOutwardConnections().get(i);
				final int inwardRowY = bounds.y + BLOCK_HEADER_TITLE_HEIGHT + (BLOCK_CONNECTION_ROW_HEIGHT * i);
				if (blockConnection != null) {
					g.setColor(blockTypeConnection.getConnectionType().getColor().darker());
					g.fillOval((bounds.x + bounds.width) - ((BLOCK_CONNECTION_CIRCLE_SIZE * 3) / 2),
							inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT - BLOCK_CONNECTION_CIRCLE_SIZE) / 2),
							BLOCK_CONNECTION_CIRCLE_SIZE, BLOCK_CONNECTION_CIRCLE_SIZE);
				}
				g.setColor(blockTypeConnection.getConnectionType().getColor());
				g.drawOval((bounds.x + bounds.width) - ((BLOCK_CONNECTION_CIRCLE_SIZE * 3) / 2),
						inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT - BLOCK_CONNECTION_CIRCLE_SIZE) / 2),
						BLOCK_CONNECTION_CIRCLE_SIZE, BLOCK_CONNECTION_CIRCLE_SIZE);
				if (hoveringBlockConnection == (i + inwardConnectionCount)) {
					g.setColor(Color.CYAN);
					g.drawOval(((bounds.x + bounds.width) - ((BLOCK_CONNECTION_CIRCLE_SIZE * 3) / 2)) + 1,
							inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT - BLOCK_CONNECTION_CIRCLE_SIZE) / 2) + 1,
							BLOCK_CONNECTION_CIRCLE_SIZE - 2, BLOCK_CONNECTION_CIRCLE_SIZE - 2);
				}
				g.setColor(Color.WHITE);
				g.drawString(blockTypeConnection.getName(),
						(bounds.x + bounds.width) - (BLOCK_CONNECTION_CIRCLE_SIZE) - BLOCK_CONNECTION_CIRCLE_SIZE
								- fontMetrics.stringWidth(blockTypeConnection.getName()),
						inwardRowY + ((BLOCK_CONNECTION_ROW_HEIGHT + fontHeight) / 2));
				if (blockConnection != null) {
					g.setColor(blockTypeConnection.getConnectionType().getColor());
					final Point outwardConnectionPoint = getOutwardConnectionPoint(i);
					final GuiBlock targetGui = blockToGui.get(blockConnection.getTarget());
					final Point inwardConnectionPoint = targetGui
							.getInwardConnectionPoint(blockConnection.getConnectionIndex());
					drawSLine(g, outwardConnectionPoint.x, outwardConnectionPoint.y, inwardConnectionPoint.x,
							inwardConnectionPoint.y);
				}
			}
		}
	}

	private static enum ConnectionDirection {
		INWARD, OUTWARD;
	}
}
