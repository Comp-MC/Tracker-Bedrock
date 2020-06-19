package org.compmc.tracker.listeners;


import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.vehicle.VehicleDestroyEvent;
import org.compmc.tracker.events.world.EntityChangeEvent;
import org.compmc.tracker.events.world.EntityChangeEvent.Action;

/**
 * Listener which wraps multiple events into one {@link EntityChangeEvent} type.
 */
@SuppressWarnings("JavaDoc")
public class EntityChangeListener implements Listener {

  private boolean callEntityChange(Entity whoChanged, Entity entity, Event cause, Action action) {
    EntityChangeEvent toCall = new EntityChangeEvent<>(whoChanged, entity, cause, action);
    Server.getInstance().getPluginManager().callEvent(toCall);
    return toCall.isCancelled();
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onVehicleDestroy(VehicleDestroyEvent event) {
    boolean cancel = callEntityChange(event.getAttacker(), event.getVehicle(), event, Action.BREAK);
    event.setCancelled(cancel);
  }
}
