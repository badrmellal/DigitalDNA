package event;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Digital DNA Model");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            DigitalDNA dna = new DigitalDNA();
            frame.add(dna);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}