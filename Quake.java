import processing.core.PImage;

import java.util.List;

public class Quake implements ActivityEntity, AnimationEntity{

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int resourceLimit;
    private int resourceCount;
    private int actionPeriod;
    private int animationPeriod;

    private static final String QUAKE_ID = "quake";
    private static final int QUAKE_ACTION_PERIOD = 1100;
    private static final int QUAKE_ANIMATION_PERIOD = 100;

    public Quake(Point position, List<PImage> images)
    {
        this.id = QUAKE_ID;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = 0;
        this.resourceCount = 0;
        this.actionPeriod = QUAKE_ACTION_PERIOD;
        this.animationPeriod = QUAKE_ANIMATION_PERIOD;

    }

    public int getAnimationPeriod() {
        return this.animationPeriod;
    }

    public int getActionPeriod() {
        return actionPeriod;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public Point getPosition() {
        return position;

    }
    public void setPosition(Point position) {
        this.position = position;
    }

    public List<PImage> getImages() { return images; }
    public int getImageIndex() { return imageIndex; }
    public int getResourceLimit() { return resourceLimit; }

    public String getId() { return id; }

    public void nextImage()
    {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public PImage getCurrentImage()
    {
        return getImages().get(getImageIndex());
    }

    public void removeEntity(WorldModel world, Entity entity)
    {
        removeEntityAt(world, entity.getPosition());
    }

    public void removeEntityAt(WorldModel world, Point pos)
    {
        if (world.withinBounds(pos)
                && world.getOccupancyCell(pos) != null)
        {
            Entity entity = world.getOccupancyCell(pos);

         /* this moves the entity just outside of the grid for
            debugging purposes */
            this.position = new Point(-1, -1);
            world.getEntities().remove(this);
            world.setOccupancyCell( pos, null);
        }
    }
    public void executeActivity(WorldModel world,
                                     ImageStore imageStore, EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(this);
        this.removeEntity(world, this);
    }
}
