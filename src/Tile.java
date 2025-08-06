import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;

public class Tile extends JButton {
    public int i, j;
    public boolean isBlocked = false, isCat = false;
    private static final Font TILE_FONT = new Font("Arial", Font.BOLD, 20);

    public Tile(int i, int j) {
        this.i = i;
        this.j = j;
        setFont(TILE_FONT);
        setFocusPainted(false);
        updateAppearance();
    }

    public void setBlocked(boolean b) {
        isBlocked = b;
        isCat = false;
        updateAppearance();
    }

    public void setCat(boolean c) {
        isCat = c;
        isBlocked = false;
        updateAppearance();
    }

    public void resetColor() {
        setBackground(null);
        setOpaque(false);
        setBorderPainted(true);
        updateAppearance();
    }

    public void highlightExplored() {
        setBackground(new Color(173, 216, 230)); // Light blue
        setOpaque(true);
        setBorderPainted(false);
    }

    public void highlightPath() {
        setBackground(new Color(255, 255, 153)); // Light yellow
        setOpaque(true);
        setBorderPainted(false);
    }

    public void updateAppearance() {
        if (isCat) {
            setText("😼");
            setForeground(Color.BLACK);
        } else if (isBlocked) {
            setText("🚧");
            setForeground(Color.RED);
        } else {
            setText("");
        }
        repaint();
    }
}