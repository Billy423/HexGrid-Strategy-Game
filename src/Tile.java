import javax.swing.JButton;
import java.awt.Color;

public class Tile extends JButton {
    public int i, j;
    public boolean isBlocked = false, isCat = false;

    public Tile(int i, int j) {
        this.i = i;
        this.j = j;
        updateAppearance();
    }

    public void setBlocked(boolean b) {
        isBlocked = b;
        updateAppearance();
    }

    public void setCat(boolean c) {
        isCat = c;
        updateAppearance();
    }

    public void resetColor() {
        setBackground(null);
    }

    public void highlightExplored() {
        setBackground(Color.CYAN);
    }

    public void highlightPath() {
        setBackground(Color.YELLOW);
    }

    public void updateAppearance() {
        setText(isCat ? "😼" : isBlocked ? "🚧" : "");
        repaint();
    }
}