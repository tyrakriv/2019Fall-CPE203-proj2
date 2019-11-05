import java.util.*;

/*
EventScheduler: ideally our way of controlling what happens in our virtual world
 */

final class EventScheduler
{
   private PriorityQueue<Event> eventQueue;
   private Map<Entity, List<Event>> pendingEvents;
   private double timeScale;

   public static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;
   public static final int ATLANTIS_ANIMATION_REPEAT_COUNT = 7;

   public EventScheduler(double timeScale)
   {
      this.eventQueue = new PriorityQueue<>(new EventComparator());
      this.pendingEvents = new HashMap<>();
      this.timeScale = timeScale;
   }

   public void scheduleEvent(Entity entity, Action action, long afterPeriod)
   {
      long time = System.currentTimeMillis() +
              (long)(afterPeriod * this.timeScale);
      Event event = new Event(action, time, entity);

      this.eventQueue.add(event);

      // update list of pending events for the given entity
      List<Event> pending = this.pendingEvents.getOrDefault(entity,
              new LinkedList<>());
      pending.add(event);
      this.pendingEvents.put(entity, pending);
   }

   public void unscheduleAllEvents(Entity entity)
   {
      List<Event> pending = this.pendingEvents.remove(entity);

      if (pending != null)
      {
         for (Event event : pending)
         {
            this.eventQueue.remove(event);
         }
      }
   }

   public void removePendingEvent(Event event)
   {
      List<Event> pending = this.pendingEvents.get(event.entity);

      if (pending != null)
      {
         pending.remove(event);
      }
   }

   public void updateOnTime(long time)
   {
      while (!eventQueue.isEmpty() &&
              eventQueue.peek().time < time)
      {
         Event next = eventQueue.poll();

         removePendingEvent(next);

         next.action.executeAction(this);
      }
   }

   public void scheduleActions(Entity entity, WorldModel world, ImageStore imageStore)
   {

      if (entity instanceof OctoFull) {
         scheduleEvent(entity, new ActivityAction((OctoFull)entity, world, imageStore), entity.getActionPeriod());
         scheduleEvent(entity, new AnimationAction(entity, 0), ((OctoFull)entity).getAnimationPeriod());
      }

      if (entity instanceof OctoNotFull) {
         scheduleEvent(entity, new ActivityAction((OctoNotFull)entity, world, imageStore), entity.getActionPeriod());
         scheduleEvent(entity, new AnimationAction((OctoNotFull)entity, 0), ((OctoNotFull)entity).getAnimationPeriod());
      }

      if (entity instanceof Fish) {
         scheduleEvent(entity,
                new ActivityAction((Fish)entity, world, imageStore),
                 entity.getActionPeriod());
      }

      if (entity instanceof Crab) {
         scheduleEvent(entity,
                 new ActivityAction((Crab)entity, world, imageStore),
                 entity.getActionPeriod());
         scheduleEvent(entity,
                 new AnimationAction(entity, 0), ((Crab)entity).getAnimationPeriod());
      }

      if (entity instanceof Quake) {
         scheduleEvent(entity,
                 new ActivityAction((Quake)entity, world, imageStore),
                 entity.getActionPeriod());
         scheduleEvent(entity,
                 new AnimationAction((Quake)entity, QUAKE_ANIMATION_REPEAT_COUNT),
                 ((Quake)entity).getAnimationPeriod());
      }

      if (entity instanceof Sgrass) {
         scheduleEvent(entity,
                 new ActivityAction((Sgrass)entity, world, imageStore),
                 entity.getActionPeriod());
      }

      if (entity instanceof Atlantis) {
         scheduleEvent(entity,
                 new AnimationAction((Atlantis)entity, ATLANTIS_ANIMATION_REPEAT_COUNT),
                 ((Atlantis)entity).getAnimationPeriod());
      }

//find crab

   }

   public void scheduleActions(WorldModel world, ImageStore imageStore)
   {
      for (Entity entity : world.getEntities())
      {
         //Only start actions for entities that include action (not those with just animations)
         if (entity.getActionPeriod() > 0)
            this.scheduleActions(entity, world, imageStore);
      }
   }












}