import processing.core.PImage;

import java.util.List;

public interface Entity {

    public int getActionPeriod();
    public int getResourceCount();
    public void setResourceCount(int resourceCount);
    public Point getPosition();
    public void setPosition(Point position);
    public List<PImage> getImages();
    public int getImageIndex();
    public int getResourceLimit();
    public String getId();
    public void nextImage();
    public PImage getCurrentImage();
    public void removeEntity(WorldModel world, Entity entity);
    public void removeEntityAt(WorldModel world, Point pos);

}
