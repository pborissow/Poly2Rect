package javaxt.utils;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

//******************************************************************************
//**  Main
//******************************************************************************
/**
 *   Simple UI used to test the Poly2Rect class
 *
 ******************************************************************************/

public class Main {

    private JLabel label;
    private JButton b1;
    private JButton b2;
    private JButton b3;

    private Canvas canvas;
    private int status;


    private ArrayList<Point> points; //user defined points
    private Polygon p;
    private ArrayList<Rectangle> rectangles; //overlapping rectangles
    private Polygon hull; //convex hull
    private Rectangle r; //inscribed rectangle
    private Polygon test;
    private Point currPos;


    private boolean showHull = false;
    private boolean showInnerRect = false;
    private boolean showOuterRects = false;


  //**************************************************************************
  //** main
  //**************************************************************************
    public static void main(String[] args) {
        JFrame f = new JFrame("Polygon Decomposition");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Main controller = new Main();
        controller.buildUI(f.getContentPane());
        f.pack();
        f.setVisible(true);
        f.setFocusable(true);
    }


  //**************************************************************************
  //** buildUI
  //**************************************************************************
    private void buildUI(Container container) {
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        label = new JLabel("Click within the framed area");
        container.add(label);


        points = new ArrayList<>();
        rectangles = new ArrayList<>();

        AtomicBoolean shift = new AtomicBoolean(false);

        canvas = new Canvas();
        canvas.addMouseListener(new MouseInputAdapter(){
            public void mousePressed(MouseEvent e) {
                if (p!=null) return;

                int x = currPos.x;
                int y = currPos.y;

                Point pt = new Point(x,y);
                boolean addPoint = true;


                Point firstPoint = null;
                if (!points.isEmpty()) firstPoint = points.get(0);
                if (firstPoint!=null){
                    double d = calculateDistanceBetweenPoints(firstPoint.x, firstPoint.y, pt.x, pt.y);
                    if (d<10 && points.size()>2){
                        pt = new Point(firstPoint.x, firstPoint.y);
                        points.add(pt);
                        p = createPolygon(points);
                        Poly2Rect pd = new Poly2Rect(p);
                        rectangles = pd.getOverlappingRectangles();
                        hull = pd.getConvexHull();
                        r = pd.getInscribedRectangle();

                        b2.setEnabled(true);
                        b3.setEnabled(true);
                        addPoint = false;
                        currPos = null;
                    }
                }

                if (addPoint){
                    points.add(pt);
                    status = 1;
                }
                canvas.repaint();
            }
        });
        canvas.addMouseMotionListener(new MouseMotionListener(){
            public void mouseMoved(MouseEvent e) {
                if (p!=null) return;

                int x = e.getX();
                int y = e.getY();
                if (x<5) x = 5;
                if (y<5) y = 5;
                //if (x>canvas.getWidth()) x = canvas.getWidth();
                //if (y>canvas.getHeight()) y = canvas.getHeight();
                currPos = new Point(x, y);


                if (!points.isEmpty()){
                    if (shift.get()){
                        updateCurrPosition();
                    }
                    canvas.repaint();
                }
            }
            public void mouseDragged(MouseEvent e) {}
        });



        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher( new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {

                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode() == 16) {
                        shift.set(true);
                        if (currPos!=null){
                            if (!points.isEmpty()){
                                updateCurrPosition();
                                canvas.repaint();
                            }
                        }
                    }
                }
                else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    if (e.getKeyCode()==16) {
                        shift.set(false);
                    }
                    else if (e.getKeyCode()==8 || e.getKeyCode()==127) { //delete/backspace
                        if (!points.isEmpty() && p==null){
                            points.remove(points.size()-1);
                            canvas.repaint();
                        }
                    }
                    else{
                        System.out.println(e.getKeyCode());
                    }
                }
                return false;
            }
        });



        container.add(canvas);



        b1 = new JButton("Reset");
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                points = new ArrayList<>();
                rectangles = new ArrayList<>();
                hull = null;
                r = null;
                p = null;
                b2.setEnabled(false);
                b3.setEnabled(false);
                showHull = false;
                showInnerRect = false;
                showOuterRects = false;
                canvas.repaint();
                updateLabel(0);
                shift.set(false);
                currPos = null;
                status = 0;
            }
        });


        b2 = new JButton("Show Hull");
        b2.setEnabled(false);
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHull = true;
                status = 16;
                canvas.repaint();
            }
        });


        b3 = new JButton("Show Rectangles");
        b3.setEnabled(false);
        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showOuterRects = true;
                status = 16;
                rectangles = Poly2Rect.divideRectangles(rectangles, p, 400);
                canvas.repaint();
            }
        });



        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        buttonPanel.add(b1);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(b2);
        buttonPanel.add(b3);

        container.add(buttonPanel);

        //Align the left edges of the components
        canvas.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    }


  //**************************************************************************
  //** Canvas
  //**************************************************************************
  /** Panel used to render points and shapes
   */
    private class Canvas extends JPanel {

        private Dimension preferredSize = new Dimension(600,450);

        public Canvas() {
            Border raisedBevel = BorderFactory.createRaisedBevelBorder();
            Border loweredBevel = BorderFactory.createLoweredBevelBorder();
            Border compound = BorderFactory.createCompoundBorder(raisedBevel, loweredBevel);
            setBorder(compound);
        }

        public Dimension getPreferredSize() {
            return preferredSize;
        }

        public void paintComponent(Graphics g) {

          //Paint background
            super.paintComponent(g);


          //Render points and edges
            Point prevPoint = null;
            for (Point point : points){

                g.setColor(Color.black);
                g.fillOval(point.x-2, point.y-2, 5, 5);

                if (prevPoint!=null){
                    g.fillOval(prevPoint.x-2, prevPoint.y-2,5,5);
                    g.drawLine(point.x, point.y, prevPoint.x, prevPoint.y);
                }

                prevPoint = point;
            }


          //Render line extending from last point to current mouse position
            if (currPos!=null){
                prevPoint = points.get(points.size()-1);
                g.fillOval(currPos.x-2, currPos.y-2,5,5);
                g.drawLine(currPos.x, currPos.y, prevPoint.x, prevPoint.y);
            }


          //Render convex hull as needed
            if (hull!=null && showHull){
                g.setColor(Color.green);
                g.drawPolygon(hull);
            }

          //Render inscribed rectangle as needed
            if (r!=null && showInnerRect){
                g.setColor(Color.red);
                g.drawRect(r.x, r.y, r.width, r.height);
                g.fillRect(r.x, r.y, r.width, r.height);
            }

          //Render rectangles
            if (!rectangles.isEmpty() && showOuterRects){
                g.setColor(Color.red);
                for (Rectangle r : rectangles){
                    g.drawRect(r.x, r.y, r.width, r.height);
                }
            }


          //Render test/debug polygon
            if (test!=null){
                g.setColor(Color.MAGENTA);
                g.drawPolygon(test);
            }

          //Update status label
            updateLabel(status);

        }
    }


  //**************************************************************************
  //** updateCurrPosition
  //**************************************************************************
    public void updateCurrPosition(){
        int x = currPos.x;
        int y = currPos.y;
        Point prevPoint = points.get(points.size()-1);
        double dx = diff(prevPoint.x, x);
        double dy = diff(prevPoint.y, y);
        if (dx>dy) y = prevPoint.y;
        else x = prevPoint.x;
        currPos = new Point(x, y);
    }


  //**************************************************************************
  //** updateLabel
  //**************************************************************************
  /** display a predefined message for a given msg state */
    private void updateLabel(int msg) {
        if (msg == 0){
            label.setText("Click within the framed area to add points");
        }
        else if (msg == 1){
            label.setText("Point added, click to add more points");
        }
        else if (msg == 2){
            label.setText("point added to polygon");
        }
        else if (msg == 3){
            label.setText("point inside not added");
        }
        else if (msg == 10){
            label.setText("Largest rectangle with corners on A and C only");
        }
        else if (msg == 11){
            label.setText("Largest rectangle with corners on B and D only");
        }
        else if (msg == 12){
            label.setText("Largest rectangle with corners on A,B and C");
        }
        else if (msg == 13){
            label.setText("Largest rectangle with corners on A,B and D");
        }
        else if (msg == 14){
            label.setText("Largest rectangle with corners on A,C and D");
        }
        else if (msg == 15){
            label.setText("Largest rectangle with corners on B,C and D");
        }
        else if (msg == 16){
            label.setText("Found " + rectangles.size() + " rectangles");
        }
        else {
            label.setText("Click to add points");
        }

    }




  //**************************************************************************
  //** diff
  //**************************************************************************
    public static double diff(double a, double b){
        double x = a-b;
        if (x<0) x = -x;
        return x;
    }


  //**************************************************************************
  //** calculateDistanceBetweenPoints
  //**************************************************************************
    public double calculateDistanceBetweenPoints(
      double x1, double y1, double x2, double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }



  //**************************************************************************
  //** createPolygon
  //**************************************************************************
    public static Polygon createPolygon(ArrayList<Point> points){
        Polygon polygon = new Polygon();
        for (Point point : points){
            polygon.addPoint(point.x, point.y);
        }
        return polygon;
    }


  //**************************************************************************
  //** createPolygon
  //**************************************************************************
    public static Polygon createPolygon(Rectangle rect) {
        int[] xpoints = {rect.x, rect.x + rect.width, rect.x + rect.width, rect.x};
        int[] ypoints = {rect.y, rect.y, rect.y + rect.height, rect.y + rect.height};
        return new Polygon(xpoints, ypoints, 4);
    }

}