import processing.core.PImage;

import java.util.*;

/*
WorldModel ideally keeps track of the actual size of our grid world and what is in that world
in terms of entities and background elements
 */

final class WorldModel{
   private int numRows;
   private int numCols;
   private Background background[][];
   public Entity occupancy[][];
   private Set<Entity> entities;
   private final int FISH_REACH = 1;

   public WorldModel(int numRows, int numCols, Background defaultBackground) {
      this.numRows = numRows;
      this.numCols = numCols;
      this.background = new Background[numRows][numCols];
      this.occupancy = new Entity[numRows][numCols];
      this.entities = new HashSet<>();

      for (int row = 0; row < numRows; row++) {
         Arrays.fill(this.background[row], defaultBackground);
      }
   }

   public Set<Entity> getEntities() {
      return entities;
   }

   public int getNumCols() {
      return numCols;
   }

   public int getNumRows() {
      return numRows;
   }

   public Optional<Point> findOpenAround(Point pos) {
      for (int dy = -FISH_REACH; dy <= FISH_REACH; dy++) {
         for (int dx = -FISH_REACH; dx <= FISH_REACH; dx++) {
            Point newPt = new Point(pos.x + dx, pos.y + dy);
            if (withinBounds(newPt) &&
                    !isOccupied(newPt)) {
               return Optional.of(newPt);
            }
         }
      }
      return Optional.empty();
   }

   public boolean withinBounds(Point pos) {
      return pos.y >= 0 && pos.y < this.numRows &&
              pos.x >= 0 && pos.x < this.numCols;
   }

   public boolean isOccupied(Point pos) {
      return withinBounds(pos) &&
              getOccupancyCell(pos) != null;
   }

   public static Optional<Entity> nearestEntity(List<Entity> entities,
                                                Point pos) {
      if (entities.isEmpty()) {
         return Optional.empty();
      } else {
         Entity nearest = entities.get(0);
         int nearestDistance =distanceSquared(nearest.getPosition(), pos);

         for (Entity other : entities) {
            int otherDistance = distanceSquared(other.getPosition(), pos);

            if (otherDistance < nearestDistance) {
               nearest = other;
               nearestDistance = otherDistance;
            }
         }

         return Optional.of(nearest);
      }
   }

   public Optional<PImage> getBackgroundImage(Point pos) {
      if (withinBounds(pos)) {
         return Optional.of(getBackgroundCell(pos).getCurrentImage());
      } else {
         return Optional.empty();
      }
   }

   public void setBackground(Point pos, Background background) {
      if (withinBounds(pos)) {
         setBackgroundCell(pos, background);
      }
   }

   public Optional<Entity> getOccupant(Point pos) {
      if (isOccupied(pos)) {
         return Optional.of(getOccupancyCell(pos));
      } else {
         return Optional.empty();
      }
   }

   private Background getBackgroundCell(Point pos) {
      return this.background[pos.y][pos.x];
   }

   private void setBackgroundCell(Point pos, Background background) {
      this.background[pos.y][pos.x] = background;
   }

   public Entity getOccupancyCell(Point pos) {
      return this.occupancy[pos.y][pos.x];
   }

   public void setOccupancyCell(Point pos, Entity entity) {
      this.occupancy[pos.y][pos.x] = entity;
   }

   public Optional<Entity> findNearest(Point pos, Class c) {
      List<Entity> ofType = new LinkedList<>();
      for (Entity entity : this.entities) {
         if (entity.getClass() == c) {
            ofType.add(entity);
         }
      }
      return nearestEntity(ofType, pos);
   }

   /*
      Assumes that there is no entity currently occupying the
      intended destination cell.
   */
   public void addEntity(Entity entity) {
      if (withinBounds(entity.getPosition())) {
         setOccupancyCell(entity.getPosition(), entity);
         this.entities.add(entity);
      }
   }

   private void moveEntity(Entity entity, Point pos) {
      Point oldPos = entity.getPosition();
      if (withinBounds(pos) && !pos.equals(oldPos)) {
         setOccupancyCell(oldPos, null);
         entity.removeEntityAt(this, pos);
         setOccupancyCell(pos, entity);
         entity.setPosition(pos);
      }
   }

   public boolean moveToFull(Entity octo, Entity target, EventScheduler scheduler) {
      if (octo.getPosition().adjacent(target.getPosition())) {
         return true;
      } else {
         if (octo instanceof OctoFull) {     //OctoNotFull?
            Point nextPos = ((OctoFull)octo).nextPositionOcto(this, target.getPosition());

            if (!octo.getPosition().equals(nextPos)) {
               Optional<Entity> occupant = getOccupant(nextPos);
               if (occupant.isPresent()) {
                  scheduler.unscheduleAllEvents(occupant.get());
               }

               moveEntity(octo, nextPos);
            }
         }
         return false;
      }
   }

   public boolean moveToNotFull(Entity octo, Entity target, EventScheduler scheduler) {
      if (octo.getPosition().adjacent(target.getPosition())) {
         octo.setResourceCount(octo.getResourceCount() + 1);
         target.removeEntity(this, target);
         scheduler.unscheduleAllEvents(target);

         return true;
      } else {
         if (octo instanceof OctoNotFull) {
            Point nextPos = ((OctoNotFull)octo).nextPositionOcto(this, target.getPosition());

            if (!octo.getPosition().equals(nextPos)) {
               Optional<Entity> occupant = getOccupant(nextPos);
               if (occupant.isPresent()) {
                  scheduler.unscheduleAllEvents(occupant.get());
               }

               moveEntity(octo, nextPos);
            }
         }
         return false;
      }
   }

   public boolean moveToCrab(Crab crab, Entity target, EventScheduler scheduler) {
      if (crab.getPosition().adjacent(target.getPosition())) {
         target.removeEntity(this, target);
         scheduler.unscheduleAllEvents(target);
         return true;
      } else {
         Point nextPos = crab.nextPositionCrab(this, target.getPosition());

         if (!crab.getPosition().equals(nextPos)) {
            Optional<Entity> occupant = getOccupant(nextPos);
            if (occupant.isPresent()) {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            moveEntity(crab, nextPos);
         }
         return false;
      }
   }

   public void tryAddEntity(Entity entity) {
      if (isOccupied(entity.getPosition())) {
         // arguably the wrong type of exception, but we are not
         // defining our own exceptions yet
         throw new IllegalArgumentException("position occupied");
      }
      addEntity(entity);
   }


   public void transformFull(Entity entity, EventScheduler scheduler, ImageStore imageStore) {
      OctoNotFull octo = new OctoNotFull(entity.getId(), entity.getResourceLimit(),
              entity.getPosition(), entity.getActionPeriod(), ((AnimationEntity)entity).getAnimationPeriod(),
              entity.getImages());

      entity.removeEntity(this, entity);
      scheduler.unscheduleAllEvents(entity);

      addEntity(octo);
      scheduler.scheduleActions(octo, this, imageStore);
   }

   public boolean transformNotFull(Entity entity, EventScheduler scheduler, ImageStore imageStore) {
      if (entity.getResourceCount() >= entity.getResourceLimit()) {
         OctoFull octo = new OctoFull(entity.getId(), entity.getPosition(), entity.getImages(),
                 entity.getResourceLimit(), entity.getResourceLimit(), entity.getActionPeriod(), ((AnimationEntity)entity).getAnimationPeriod()); // two resource limits?

         entity.removeEntity(this, entity);
         scheduler.unscheduleAllEvents(entity);

         addEntity(octo);
         scheduler.scheduleActions(octo, this, imageStore);

         return true;
      }

      return false;
   }
   public static int distanceSquared(Point p1, Point p2) {
      int deltaX = p1.x - p2.x;
      int deltaY = p1.y - p2.y;

      return deltaX * deltaX + deltaY * deltaY;
   }
//   public void load(Scanner in,ImageStore imageStore)
//   {
//      int lineNumber = 0;
//      while (in.hasNextLine())
//      {
//         try
//         {
//            if (!processLine(in.nextLine(), imageStore))
//            {
//               System.err.println(String.format("invalid entry on line %d",
//                       lineNumber));
//            }
//         }
//         catch (NumberFormatException e)
//         {
//            System.err.println(String.format("invalid entry on line %d",
//                    lineNumber));
//         }
//         catch (IllegalArgumentException e)
//         {
//            System.err.println(String.format("issue on line %d: %s",
//                    lineNumber, e.getMessage()));
//         }
//         lineNumber++;
//      }
//   }
//   public boolean processLine(String line, ImageStore imageStore)
//   {
//      String[] properties = line.split("\\s");
//      if (properties.length > 0)
//      {
//         switch (properties[PROPERTY_KEY])
//         {
//            case BGND_KEY:
//               return parseBackground(properties, imageStore);
//            case OCTO_KEY:
//               return parseOcto(properties, imageStore);
//            case OBSTACLE_KEY:
//               return parseObstacle(properties, imageStore);
//            case FISH_KEY:
//               return parseFish(properties, imageStore);
//            case ATLANTIS_KEY:
//               return parseAtlantis(properties, imageStore);
//            case SGRASS_KEY:
//               return parseSgrass(properties, imageStore);
//         }
//      }
//      return false;
//   }
//   private boolean parseOcto(String [] properties, ImageStore imageStore)
//   {
//      if (properties.length == OCTO_NUM_PROPERTIES)
//      {
//         Point pt = new Point(Integer.parseInt(properties[OCTO_COL]),
//                 Integer.parseInt(properties[OCTO_ROW]));
//         Entity entity = new OctoNotFull(properties[OCTO_ID],
//                 Integer.parseInt(properties[OCTO_LIMIT]),
//                 pt,
//                 Integer.parseInt(properties[OCTO_ACTION_PERIOD]),
//                 Integer.parseInt(properties[OCTO_ANIMATION_PERIOD]),
//                 imageStore.getImageList(OCTO_KEY));
//         this.tryAddEntity(entity);
//      }
//      return properties.length == OCTO_NUM_PROPERTIES;
//   }
//
//   private boolean parseObstacle(String [] properties, ImageStore imageStore)
//   {
//      if (properties.length == OBSTACLE_NUM_PROPERTIES)
//      {
//         Point pt = new Point(
//                 Integer.parseInt(properties[OBSTACLE_COL]),
//                 Integer.parseInt(properties[OBSTACLE_ROW]));
//         Entity entity = new Obstacle(properties[OBSTACLE_ID],
//                 pt, imageStore.getImageList(OBSTACLE_KEY));
//         this.tryAddEntity(entity);
//      }
//      return properties.length == OBSTACLE_NUM_PROPERTIES;
//   }
//
//   private boolean parseFish(String [] properties, ImageStore imageStore)
//   {
//      if (properties.length == FISH_NUM_PROPERTIES)
//      {
//         Point pt = new Point(Integer.parseInt(properties[FISH_COL]),
//                 Integer.parseInt(properties[FISH_ROW]));
//         Entity entity = new Fish(properties[FISH_ID], pt,
//                 Integer.parseInt(properties[FISH_ACTION_PERIOD]),
//                 imageStore.getImageList(FISH_KEY));
//         this.tryAddEntity(entity);
//      }
//      return properties.length == FISH_NUM_PROPERTIES;
//   }
//
//   private boolean parseBackground(String [] properties, ImageStore imageStore)
//   {
//      if (properties.length == BGND_NUM_PROPERTIES) {
//         Point pt = new Point(Integer.parseInt(properties[BGND_COL]), Integer.parseInt(properties[BGND_ROW]));
//         String id = properties[BGND_ID];
//         this.setBackground(pt, new Background(id, imageStore.getImageList(id)));
//      }
//      return properties.length == BGND_NUM_PROPERTIES;
//   }
//
//   private boolean parseAtlantis(String [] properties, ImageStore imageStore)
//   {
//      if (properties.length == ATLANTIS_NUM_PROPERTIES)
//      {
//         Point pt = new Point(Integer.parseInt(properties[ATLANTIS_COL]),
//                 Integer.parseInt(properties[ATLANTIS_ROW]));
//         Entity entity = new Atlantis(properties[ATLANTIS_ID],
//                 pt, imageStore.getImageList(ATLANTIS_KEY));
//         this.tryAddEntity(entity);
//      }
//      return properties.length == ATLANTIS_NUM_PROPERTIES;
//   }
//
//   private boolean parseSgrass(String [] properties, ImageStore imageStore)
//   {
//      if (properties.length == SGRASS_NUM_PROPERTIES)
//      {
//         Point pt = new Point(Integer.parseInt(properties[SGRASS_COL]),
//                 Integer.parseInt(properties[SGRASS_ROW]));
//         Entity entity =  new Sgrass(properties[SGRASS_ID],
//                 pt, imageStore.getImageList(SGRASS_KEY),
//                 Integer.parseInt(properties[SGRASS_ACTION_PERIOD]));
//         this.tryAddEntity(entity);
//      }
//      return properties.length == SGRASS_NUM_PROPERTIES;
//   }
}