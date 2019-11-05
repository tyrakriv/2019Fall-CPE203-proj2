public class AnimationAction implements Action{

    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    public AnimationAction (Entity entity, int repeatCount)
    {
        this.world = null;
        this.entity = entity;
        this.imageStore = null;
        this.repeatCount = repeatCount;
    }

    public void executeAction(EventScheduler scheduler)
    {
        entity.nextImage();

        if (repeatCount != 1)
        {
            scheduler.scheduleEvent(entity, new AnimationAction(entity, Math.max(repeatCount - 1, 0)),
                    ((AnimationEntity)entity).getAnimationPeriod());
        }
    }
}
