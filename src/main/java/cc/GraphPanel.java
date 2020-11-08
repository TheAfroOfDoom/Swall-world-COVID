package cc;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;

public class GraphPanel extends JPanel {

    private static final double SPREAD_DEFAULT = 17.4;
    private static final double SPREAD_MASK = 3.1;
    private static final double SPREAD_DIST_SUB1M = 12.8;
    private static final double SPREAD_DIST_OVER1M = 2.6;

    ArrayList<Vertex> infectedVerts;
    ArrayList<Vertex> verts;
    int frameNumber;
    double maxRadius;
    int dimension;
    int nVerts;
    volatile double scale;
    HyperbolicRandomGraphGenerator hrgg;
    boolean toggleExposure;
    boolean toggleDistance;
    boolean toggleMask;

    volatile int mouseX1;
    volatile int mouseY1;
    volatile int panelX1;
    volatile int panelY1;
    volatile int offsetX;
    volatile int offsetY;

    public GraphPanel(int nVerts, double averageDegree) {
        super();
        frameNumber = 0;
        dimension = 800;
        setPreferredSize(new Dimension(dimension, dimension));
        setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 30));

        double curvature = -1;
        hrgg = new HyperbolicRandomGraphGenerator(nVerts, averageDegree, curvature);
        this.scale = (dimension / 2) / hrgg.getMaxRadius();

        maxRadius = hrgg.getMaxRadius();
        this.nVerts = nVerts;
        verts = new ArrayList<Vertex>(Arrays.asList(hrgg.getCopy()));
        infectedVerts = new ArrayList<>();
        update(true);

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                mouseX1 = e.getXOnScreen();
                mouseY1 = e.getYOnScreen();
                panelX1 = getX();
                panelY1 = getY();
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

        });
        addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
                offsetX = e.getXOnScreen() - mouseX1;
                offsetY = e.getYOnScreen() - mouseY1;
                setLocation(panelX1 + offsetX, panelY1 + offsetY);
            }

            public void mouseMoved(MouseEvent e) {
            }
        });
    }

    public void reset() {
        verts = new ArrayList<Vertex>(Arrays.asList(hrgg.getCopy()));
        infectedVerts = new ArrayList<>();
        frameNumber = 0;
        update(true);
    }

    public void paint(Graphics g) {
        Color prevColor = g.getColor();
        for (Vertex vert : this.verts) {
            vert.calcPos(scale);
        }
        for (Vertex vert : this.verts) {
            for (int index : vert.connections) {
                if (vert.active && verts.get(index).active) {
                    // red (spread)
                    g.setColor(new Color(255, 0, 0, 50));
                } else {
                    // gray
                    g.setColor(new Color(0, 0, 0, 50));
                }
                g.drawLine(vert.x, vert.y, verts.get(index).x, verts.get(index).y);
            }
        }
        for (Vertex vert : this.verts) {
            vert.draw(g, scale, toggleExposure);
        }
        int smr = (int) (scale * maxRadius);
        g.drawOval((dimension / 2) - smr, (dimension / 2) - smr, 2 * smr, 2 * smr);
        g.drawString("% inf:", 50, 720);
        g.drawString(String.valueOf(100 * infectedVerts.size() / verts.size()), 50, 730);
        g.drawString("frame:", 50, 740);
        g.drawString(String.valueOf(frameNumber), 50, 750);
        g.setColor(prevColor);
    }

    public void update(boolean init) {
        if (init) {
            int index = (int) (Math.random() * nVerts);
            verts.get(index).infected = true;
            infectedVerts.add(verts.get(index));
        } else {
            frameNumber++;
            ArrayList<Vertex> newInfected = new ArrayList<>();

            for (Vertex infVert : infectedVerts) {
                infVert.active = false;
                int randomConnect = infVert.connections.get((int) (Math.random() * infVert.connections.size()));
                Vertex vertExposed = verts.get(randomConnect);
                vertExposed.exposed++;
                if (Math.random() * 100 < calcRisk()) {
                    if (!vertExposed.infected && !newInfected.contains(vertExposed)) {
                        vertExposed.infected = true;
                        vertExposed.active = true;
                        infVert.active = true;
                        newInfected.add(vertExposed);
                    }
                }
            }
            for (Vertex newInf : newInfected) {
                infectedVerts.add(newInf);
            }
        }
    }

    public double calcRisk() {
        if (toggleMask) {
            return SPREAD_MASK;
        } else {
            return SPREAD_DEFAULT;
        }
    }
}
