package org.compmc.tracker.info;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.entity.Entity;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class EntityInfo extends OwnerInfoBase implements PhysicalInfo {

  private final String entityType;
  private final @Nullable
  String customName;

  public EntityInfo(
      String entityType, @Nullable String customName, @Nullable TrackedPlayer owner) {
    super(owner);
    this.entityType = checkNotNull(entityType);
    this.customName = customName;
  }

  public EntityInfo(String entityType, @Nullable String customName) {
    this(entityType, customName, null);
  }

  public EntityInfo(Entity entity, @Nullable TrackedPlayer owner) {
    this(entity.getSaveId(), entity.getNameTag(), owner);
  }

  public EntityInfo(Entity entity) {
    this(entity, null);
  }

  public String getEntityType() {
    return entityType;
  }

  public @Nullable
  String getCustomName() {
    return customName;
  }

  @Override
  public String getIdentifier() {
    return getEntityType();
  }

  @Override
  public String getName() {
    if (getCustomName() != null) {
      return getCustomName();
    } else {
      return getIdentifier();
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{entity="
        + getEntityType()
        + " name="
        + getCustomName()
        + " owner="
        + getOwner()
        + "}";
  }
}
