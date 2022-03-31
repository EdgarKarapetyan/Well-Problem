import model.House;
import model.Well;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainUI {
    private static final int SCALE = 5;
    private static final int MARGIN = 20;

    public static WellProblem wellProblem;

    public static void main(String[] args) {

        // Init the WellProblem class
        wellProblem = new WellProblem();
        List<Well> wells = Arrays.asList(wellProblem.wells);
        List<House> houses = Arrays.asList(wellProblem.houses);

        // Create UI components
        JFrame jFrame = new JFrame();
        ImagePanel background = new ImagePanel("img/bg.jpg", 0, 0, 550, 550);
        background.setLayout(null);
        jFrame.add(background);

        wells.stream()
                .map(well -> new ImagePanel("img/well.png", getFixed(well.getX()) - 16, getFixed(well.getY()) - 16, 32, 32))
                .forEach(background::add);

        houses.stream()
                .map(house -> new ImagePanel("img/house.png", getFixed(house.getX()) - 16, getFixed(house.getY()) - 16, 32, 32))
                .forEach(background::add);

        JButton executeBtn = new JButton("Execute Algorithm");
        executeBtn.setBounds(0, getFixed(100)+20, getFixed(100)+30, 50);
        executeBtn.addActionListener(e -> {
            wellProblem.execute();
            wellProblem.writeOutput();
            doDrawing(background.getGraphics());
        });
        jFrame.add(executeBtn);

        jFrame.setSize(getFixed(100) + 30, getFixed(100) + 100);
        jFrame.setLayout(null);//using no layout managers
        jFrame.setVisible(true);//making the frame visible

    }



    public static void doDrawing(Graphics g) {
        for (Well w : wellProblem.wells) {
            for (Integer i : w.getConnectedHouses()) {
                g.drawLine(getFixed(w.getX()), getFixed(w.getY()),
                        getFixed(wellProblem.houses[i].getX()),
                        getFixed(wellProblem.houses[i].getY()));
            }
        }
    }

    private static int getFixed(int number) {
        return MARGIN + number * SCALE;
    }

    private static class ImagePanel extends JPanel {

        private BufferedImage image;

        public ImagePanel(String filename, int x, int y, int width, int height) {
            try {
                image = ImageIO.read(new File(filename));
            } catch (IOException ex) {
                // handle exception...
            }
            setBounds(x, y, width, height);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this);
        }

    }
}
