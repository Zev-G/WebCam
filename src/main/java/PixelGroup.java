import java.util.ArrayList;
import java.util.List;

public class PixelGroup {

    private double maxX = Integer.MIN_VALUE;
    private double minX = Integer.MAX_VALUE;
    private double maxY = Integer.MIN_VALUE;
    private double minY = Integer.MAX_VALUE;

    private final List<Pixel> pixels = new ArrayList<>();

    public PixelGroup(List<PixelGroup> groups) {
        for (PixelGroup group : groups) {
            maxX = Math.max(maxX, group.maxX);
            maxY = Math.max(maxY, group.maxY);
            minX = Math.min(minX, group.minX);
            minY = Math.min(minY, group.minY);
            pixels.addAll(group.pixels);
        }
    }
    public PixelGroup(Pixel original) {
        addPixel(original);
    }

    public void addPixel(Pixel pixel) {
        double x = pixel.getX();
        double y = pixel.getY();

        if (x > maxX) {
            maxX = x;
        }
        if (x < minX) {
            minX = x;
        }
        if (y > maxY) {
            maxY = y;
        }
        if (y < minY) {
            minY = y;
        }

        pixels.add(pixel);
    }

    private double distanceFromCenter(int x, int y) {
        double centerX = minX + (maxX - minX) / 2;
        double centerY = minY + (maxY - minY) / 2;

        return Math.sqrt(Math.pow(Math.abs(x - centerX), 2) + Math.pow(Math.abs(y - centerY), 2));
    }
    private boolean fallsWithinBoundingBox(int x, int y) {
        return  x > minX && x < maxX &&
                y > minY && y < maxY;
    }

    public boolean accepts(Pixel pixel, double maxLength) {
        double dx = Math.max(minX - pixel.getX(), Math.max(0, pixel.getX() - maxX));
        double dy = Math.max(minY - pixel.getY(), Math.max(0, pixel.getY() - maxY));
        return Math.sqrt(dx*dx + dy*dy) <= maxLength;
    }

    public List<Pixel> getPixels() {
        return pixels;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinY() {
        return minY;
    }

}
