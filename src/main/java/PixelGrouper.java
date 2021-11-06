import java.util.ArrayList;
import java.util.List;

public class PixelGrouper {

    public static List<PixelGroup> findPixelGroups(Pixel[] pixels, double maxLength) {
        List<PixelGroup> groups = new ArrayList<>();

        for (Pixel pixel : pixels) {
            if (pixel != null) {
                List<PixelGroup> validGroups = null;
                for (PixelGroup group : groups) {
                    if (group.accepts(pixel, maxLength)) {
                        if (validGroups == null) validGroups = new ArrayList<>();
                        validGroups.add(group);
                    }
                }
                if (validGroups == null) {
                    groups.add(new PixelGroup(pixel));
                } else {
                    if (groups.size() > 1) {
                        groups.removeAll(validGroups);
                        validGroups.add(new PixelGroup(pixel));
                        groups.add(new PixelGroup(validGroups));
                    } else {
                        groups.get(0).addPixel(pixel);
                    }
                }
            }
        }

        return groups;
    }

}
