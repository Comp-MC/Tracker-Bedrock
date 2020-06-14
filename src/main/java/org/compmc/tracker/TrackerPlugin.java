package org.compmc.tracker;

import cn.nukkit.plugin.PluginBase;
import org.compmc.tracker.listeners.MiscListener;

public class TrackerPlugin extends PluginBase {

  public static TrackerPlugin INSTANCE;

  @Override
  public void onLoad() {
    INSTANCE = this;
  }

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(new MiscListener(), this);
  }
}
