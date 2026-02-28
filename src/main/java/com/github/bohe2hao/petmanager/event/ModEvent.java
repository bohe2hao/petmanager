package com.github.bohe2hao.petmanager.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModEvent {

  public enum Event {
    PET_INFO_ARRIVED,
    SWITCH_UNCANCELED_PET,
    PLAYER_INFO_ARRIVED
  }

  static HashMap<Event, List<Runnable>> lib = new HashMap<>();

  public static void subscribe(Event event, Runnable runnable) {
    lib.get(event).add(runnable);
  }

  public static void unsubscribe(Event event, Runnable runnable) {
    lib.get(event).remove(runnable);
  }

  public static void publish(Event event) {
    for (Runnable runnable : lib.get(event)) {
      runnable.run();
    }
  }

  static {
    for (Event event : Event.values()) {
      lib.put(event, new ArrayList<>());
    }
  }
}
