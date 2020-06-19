package org.compmc.tracker.info;

import cn.nukkit.level.Location;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.FallInfo;
import org.compmc.tracker.api.info.OwnerInfo;
import org.compmc.tracker.api.info.TrackerInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class FallState implements FallInfo {

  // A player must leave the ground within this many ticks of being attacked for
  // the fall to be caused by knockback from that attack
  public static final int MAX_KNOCKBACK_TICKS = 1000;

  // A player's fall is cancelled if they are on the ground continuously for more than this many
  // ticks
  public static final int MAX_ON_GROUND_TICKS = 50;

  // A player's fall is cancelled if they touch the ground more than this many times
  public static final int MAX_GROUND_TOUCHES = 100;

  // A player's fall is cancelled if they are in water for more than this many ticks
  public static final int MAX_SWIMMING_TICKS = 1000;

  // A player's fall is cancelled if they are climbing something for more than this many ticks
  public static final int MAX_CLIMBING_TICKS = 50;

  public final TrackedPlayer victim;
  public final Location origin;

  // The kind of attack that initiated the fall
  public final From from;
  public final TrackerInfo cause;

  public final long startTime;

  // Where they land.. this is set when the fall ends
  public To to;

  // If the player is on the ground when attacked, this is initially set false and later set true
  // when they leave
  // the ground within the allowed time window. If the player is already in the air when attacked,
  // this is set true.
  // This is used to distinguish the initial knockback/spleef from ground touches that occur during
  // the fall.
  public boolean isStarted;

  // Set true when the fall is over and no further processing should be done
  public boolean isEnded;

  // Time the player last transitioned from off-ground to on-ground
  public long onGroundTick;

  // The player's most recent swimming state and the time it was last set true
  public boolean isSwimming;
  public long swimmingTick;

  // The player's most recent climbing state and the time it was last set true
  public boolean isClimbing;
  public long climbingTick;

  // The player's most recent in-lava state and the time it was last set true
  public boolean isInLava;
  public long inLavaTick;

  // The number of times the player has touched the ground during since isFalling was set true
  public int groundTouchCount;

  public FallState(TrackedPlayer victim, From from, TrackerInfo cause) {
    this.victim = victim;
    this.from = from;
    this.cause = cause;
    this.startTime = System.currentTimeMillis();
    this.origin = victim.getBase().getLocation();
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    if (cause instanceof OwnerInfo) {
      return ((OwnerInfo) cause).getOwner();
    } else if (cause instanceof DamageInfo) {
      return ((DamageInfo) cause).getAttacker();
    } else {
      return null;
    }
  }

  @Override
  public Location getOrigin() {
    return origin;
  }

  @Override
  public From getFrom() {
    return from;
  }

  @Override
  public To getTo() {
    return to;
  }

  @Override
  public TrackerInfo getCause() {
    return cause;
  }

  /**
   * Check if the victim of this fall is current supported by any solid blocks, water, or ladders
   */
  public boolean isSupported() {
    return this.isClimbing || this.isSwimming || victim.getBase().isOnGround();
  }

  /**
   * Check if the victim has failed to become unsupported quickly enough after the fall began
   */
  public boolean isExpired(long now) {
    return this.isSupported() && now - startTime > MAX_KNOCKBACK_TICKS;
  }

  /**
   * Check if this fall has ended safely, which is true if the victim is not in lava and any of the
   * following are true:
   *
   * <p>- victim has been on the ground for MAX_ON_GROUND_TICKS - victim has touched the ground
   * MAX_GROUND_TOUCHES times - victim has been in water for MAX_SWIMMING_TICKS - victim has been on
   * a ladder for MAX_CLIMBING_TICKS
   */
  public boolean isEndedSafely(long now) {
    return !this.isInLava
        && ((victim.getBase().isOnGround()
        && (now - onGroundTick > MAX_ON_GROUND_TICKS
        || groundTouchCount > MAX_GROUND_TOUCHES))
        || (isSwimming && now - swimmingTick > MAX_SWIMMING_TICKS)
        || (isClimbing && now - climbingTick > MAX_CLIMBING_TICKS));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "victim="
        + victim
        + " origin="
        + origin
        + " from="
        + from
        + " cause="
        + cause
        + " startTime="
        + startTime
        + " to="
        + to
        + " isStarted="
        + isStarted
        + " isEnded="
        + isEnded
        + " onGroundTick="
        + onGroundTick
        + " isSwimming="
        + isSwimming
        + " swimmingTick="
        + swimmingTick
        + " isClimbing="
        + isClimbing
        + " climbingTick="
        + climbingTick
        + " isInLava="
        + isInLava
        + " inLavaTick="
        + inLavaTick
        + " groundTouchCount="
        + groundTouchCount
        + '}';
  }
}
