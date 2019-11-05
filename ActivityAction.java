public class ActivityAction implements Action{

    private ActivityEntity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    public ActivityAction(ActivityEntity entity, WorldModel world,
                  ImageStore imageStore)
    {
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = 0;
    }

    public void executeAction(EventScheduler scheduler){
        entity.executeActivity(this.world,
                    this.imageStore,scheduler);
    }
}
