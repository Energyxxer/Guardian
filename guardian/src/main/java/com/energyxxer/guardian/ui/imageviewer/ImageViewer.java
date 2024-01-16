package com.energyxxer.guardian.ui.imageviewer;

import com.energyxxer.guardian.global.Status;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.floatingcanvas.DrawMultipliedComposite;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.guardian.util.MathUtil;
import com.energyxxer.util.Disposable;
import com.energyxxer.util.ImageManager;
import com.energyxxer.util.StringUtil;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.ScalableGraphics2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;

/**
 * Created by User on 2/8/2017.
 */
public class ImageViewer extends JPanel implements DisplayModule, MouseWheelListener, MouseMotionListener, KeyListener, Disposable {

    private static final double MIN_SCALE = 0.25;
    private static final double MAX_SCALE = 50;
    private static final int CHECK_PATTERN_SIZE = 5;

    private File file;

    private BufferedImage img;
    private double scale = 1;

    private double offsetX = 0;
    private double offsetY = 0;
    private Point cursorPoint = new Point(0,0);
    private Point posOnImage = new Point(0,0);

    private Dimension imgSize;

    private boolean initialized = false;
    private boolean crosshairVisible = false;
    private boolean alphaEnabled = true;

    private Color crosshairColor = new Color(0, 0, 0, 64);

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private DrawMultipliedComposite composite = new DrawMultipliedComposite(Color.WHITE);

    public ImageViewer(File file) {
        this.file = file;
        updateImage();
        this.imgSize = new Dimension(img.getWidth(), img.getHeight());
        this.addMouseWheelListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        tlm.addThemeChangeListener(t -> {
            this.setBackground(t.getColor(Color.WHITE, "ImageViewer.background"));
            this.crosshairColor = t.getColor(new Color(0,0,0,64), "ImageViewer.crosshair");
        });
    }

    private void updateImage() {
        try {
            this.img = ImageIO.read(file);
        } catch(IOException x) {
            this.img = ImageManager.load("null");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(scale == 0) scale = 1;
        g.setColor(this.getBackground());
        g.fillRect(0,0,this.getWidth(), this.getHeight());

        if(!initialized) {
            this.adjustToMiddle();
            initialized = true;
        }

        Rectangle rect = centerDimension();
        Shape originalBounds = g.getClipBounds();

        drawBackground(g, rect);

        g.setClip(rect);
        drawImage(g, rect);

        g.setClip(originalBounds);

        drawOverlays(g, rect);
    }

    private void drawBackground(Graphics g, Rectangle rect) {
        if(alphaEnabled) {
            g.setClip(rect);
            g.setColor(new Color(0x999999));
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
            g.setColor(new Color(0x666666));

            for(int x = rect.x > 0 ? rect.x : (rect.x-CHECK_PATTERN_SIZE) % (CHECK_PATTERN_SIZE*2); x < rect.x + rect.width; x += CHECK_PATTERN_SIZE * 2) {
                for(int y = rect.y > 0 ? rect.y : (rect.y-CHECK_PATTERN_SIZE) % (CHECK_PATTERN_SIZE*2); y < rect.y + rect.height && y < this.getHeight(); y += CHECK_PATTERN_SIZE * 2) {
                    g.fillRect(x + CHECK_PATTERN_SIZE, y, CHECK_PATTERN_SIZE, CHECK_PATTERN_SIZE);
                    g.fillRect(x, y + CHECK_PATTERN_SIZE, CHECK_PATTERN_SIZE, CHECK_PATTERN_SIZE);
                }
            }
        } else {
            g.setColor(new Color(0xFF00FF));
            int borderThickness = (int)Math.round(2 * ScalableGraphics2D.SCALE_FACTOR);
            g.fillRect(rect.x - borderThickness, rect.y - borderThickness, rect.width + borderThickness*2, rect.height + borderThickness*2);
        }
    }

    private void drawImage(Graphics g, Rectangle rect) {
        g.setClip(rect);
        if(alphaEnabled) {
            g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
        } else {
            Composite oldComposite = ((Graphics2D) g).getComposite();
            ((Graphics2D) g).setComposite(composite.setIgnoreSourceAlpha(!alphaEnabled));
            g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
            ((Graphics2D) g).setComposite(oldComposite);
        }
    }

    private void drawOverlays(Graphics g, Rectangle rect) {
        if(crosshairVisible) {
            g.setColor(this.crosshairColor);

            Point pixelOnCursor = this.getPositionOnScreen(new Point.Double(posOnImage.x, posOnImage.y));
            g.fillRect(pixelOnCursor.x - 1, 0, 2, this.getHeight());
            g.fillRect(0, pixelOnCursor.y - 1, this.getWidth(), 2);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

        double accumulator = Math.abs(e.getPreciseWheelRotation());
        int direction = (int) Math.signum(e.getPreciseWheelRotation());

        while(accumulator > 0) {
            double amount = direction * Math.min(accumulator, 1);
            accumulator -= 1;

            double newScale = scale * Math.abs(amount) * ((amount < 0) ? 1.25 : 0.8);
            if(newScale >= MIN_SCALE && newScale <= MAX_SCALE) {

                Point.Double posOnImage = this.getPositionOnImage(cursorPoint, scale);
                Point.Double newPosOnImage = this.getPositionOnImage(cursorPoint, newScale);

                this.offsetX += (newPosOnImage.x - posOnImage.x) * newScale;
                this.offsetY += (newPosOnImage.y - posOnImage.y) * newScale;

                scale = newScale;
            }
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.offsetX += e.getX() - cursorPoint.getX();
        this.offsetY += e.getY() - cursorPoint.getY();
        this.cursorPoint = e.getPoint();
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.cursorPoint = e.getPoint();
        Point.Double fullPosOnImage = this.getPositionOnImage();
        this.posOnImage = new Point((int) Math.round(fullPosOnImage.x), (int) Math.round(fullPosOnImage.y));
        this.crosshairVisible = isPlatformControlDown(e);
        displayCaretInfo();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
            this.crosshairVisible = true;
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
            this.crosshairVisible = false;
            repaint();
        }

    }

    @Override
    public void displayCaretInfo() {
        if(Guardian.core.useJavaImageCoordinates()) {
            boolean animated = new File(file.getPath() + ".mcmeta").exists();

            if(imgSize.width == imgSize.height) {
                GuardianWindow.statusBar.setSelectionInfo("UV pos: " + StringUtil.stripDecimals(MathUtil.truncateDecimals((double) (posOnImage.x * 16) / imgSize.width, 4)) + ", " + StringUtil.stripDecimals(MathUtil.truncateDecimals(((double) (posOnImage.y  * 16) / imgSize.height),4)));
            } else if(imgSize.width % imgSize.height == 0 || imgSize.height % imgSize.width == 0) {
                String animationText = animated ? "Animated" : "Missing animation";
                if(imgSize.width > imgSize.height) {
                    GuardianWindow.statusBar.setSelectionInfo("UV pos: " + StringUtil.stripDecimals(MathUtil.truncateDecimals((double) (posOnImage.x % imgSize.height * 16) / imgSize.height, 4)) + ", " + StringUtil.stripDecimals(MathUtil.truncateDecimals(((double) (posOnImage.y * 16) / imgSize.height),4)) + "    " + animationText);
                } else {
                    GuardianWindow.statusBar.setSelectionInfo("UV pos: " + StringUtil.stripDecimals(MathUtil.truncateDecimals((double) (posOnImage.x * 16) / imgSize.width, 4)) + ", " + StringUtil.stripDecimals(MathUtil.truncateDecimals(((double) (posOnImage.y % imgSize.width * 16) / imgSize.width),4)) + "    " + animationText);
                }
            } else {
                GuardianWindow.statusBar.setSelectionInfo("Invalid aspect ratio");
            }
        } else {
            GuardianWindow.statusBar.setSelectionInfo("Pixel pos: " + posOnImage.x + ", " + posOnImage.y);
        }
        GuardianWindow.statusBar.setCaretInfo(imgSize.width + "x" + imgSize.height);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public Object save() {
        return null;
    }

    @Override
    public void focus() {
        this.requestFocus();
    }

    @Override
    public boolean moduleHasFocus() {
        return this.hasFocus();
    }

    private void adjustToMiddle() {
        Dimension middlePanelSize = new Dimension(this.getWidth()/2, this.getHeight()/2);
        Dimension middleImageSize = getScaledDimension(imgSize, middlePanelSize);
        if(middleImageSize.width == 0) {
            Debug.log("Image couldn't be adjusted.", Debug.MessageType.WARN);
            return;
        }

        scale = (double) middleImageSize.width / imgSize.width;
    }

    private Point.Double getPositionOnImage() {
        return getPositionOnImage(cursorPoint, scale);
    }

    private Point.Double getPositionOnImage(Point p, double forScale) {
        Point center = this.getCenterPoint();

        return new Point.Double(
                ((p.x - (center.x - ((imgSize.width * forScale) / 2)) - offsetX) / forScale),
                ((p.y - (center.y - ((imgSize.height * forScale) / 2)) - offsetY) / forScale)
        );
    }

    private Point getPositionOnScreen(Point.Double p) {
        Point center = this.getCenterPoint();

        return new Point(
                (int) ((p.x * scale) + (center.x - ((int) (imgSize.width * scale)/2)) + offsetX),
                (int) ((p.y * scale) + (center.y - ((int) (imgSize.height * scale)/2)) + offsetY)
        );
    }

    private Point getCenterPoint() {
        return new Point(this.getWidth()/2, this.getHeight()/2);
    }

    private Rectangle centerDimension() {
        Dimension bounds = new Dimension(this.getWidth(), this.getHeight());
        double x1 = ((bounds.width/2.0)-(imgSize.width*scale/2) + offsetX);
        double y1 = ((bounds.height/2.0)-(imgSize.height*scale/2) + offsetY);
        double x2 = ((bounds.width/2.0)+(imgSize.width*scale/2) + offsetX);
        double y2 = ((bounds.height/2.0)+(imgSize.height*scale/2) + offsetY);
        return new Rectangle((int)x1, (int)y1, (int)(x2 - x1), (int)(y2 - y1));
    }

    private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        float aspect_ratio = (float) new_width / new_height;
        float bound_ratio = (float) bound_width / bound_height;

        if(aspect_ratio > bound_ratio) {
            new_width = bound_width;
            new_height = (new_width * original_height) / original_width;
        } else {
            new_height = bound_height;
            new_width = (new_height * original_width) / original_height;
        }
        new_width = (int) Math.floor(new_width);
        new_height = (int) Math.floor(new_height);
        return new Dimension(new_width, new_height);
    }

    @Override
    public void performModuleAction(String key) {
        switch (key) {
            case "editor.find": {
                alphaEnabled = !alphaEnabled;
                repaint();
                GuardianWindow.statusBar.setStatus(new Status("Alpha rendering: " + (alphaEnabled ? "Enabled" : "Disabled")), 3000);
                break;
            }
            case "editor.reload": {
                updateImage();
                repaint();
                break;
            }
        }
    }

    @Override
    public boolean transform(ModuleToken newToken) {
        if(newToken instanceof FileModuleToken) {
            this.file = ((FileModuleToken) newToken).getFile();
        }
        return true;
    }

    @Override
    public void dispose() {
        tlm.dispose();
    }
}
