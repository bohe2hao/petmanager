package com.github.bohe2hao.petmanager.others;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ScheduledManager<T> {

    private final Set<scheduledTask<T>> ALL_TASK = new HashSet<>();

    public void add(float delay, int duration, Consumer<T> run) {
        scheduledTask<T> task = new scheduledTask<>(delay,
                                                    duration,
                                                    1, 0, run);
        ALL_TASK.add(task);
    }

    public void add(float delay, int duration, int times, float interval,
                    Consumer<T> run) {
        scheduledTask<T> task = new scheduledTask<>(delay,
                                                    duration,
                                                    times,
                                                    interval,
                                                    run);
        ALL_TASK.add(task);
    }

    public void tick(T any) {
        ALL_TASK.removeIf(s -> {
            s.tick(any);
            return s.isOver;
        });
    }

    public static class scheduledTask<T> {

    private final long interval; // 纳秒
    private final int totalTimes; // 总执行次数
    private final long delay; // 纳秒
    private final long durationEachTime; // 纳秒
    private boolean triggered = false;
    private boolean isOver = false;
    private final long startNanoTime; // 使用纳秒时间
    private long cycleStartNanoTime; // 周期开始时间（纳秒）
    private int executedCycles = 0; // 已执行的周期数
    private boolean hasExecutedInCurrentCycle = false; // 当前周期是否已执行

    private final Consumer<T> run;

    private scheduledTask(float delay, float durationEachTime, int times, float interval, Consumer<T> run) {

      this.delay = (long) (delay * 1_000_000_000L);
      this.interval = (long) (interval * 1_000_000_000L);
      this.durationEachTime = (long) (durationEachTime * 1_000_000_000L);
      this.totalTimes = times;
      this.run = run;
      this.startNanoTime = System.nanoTime();
      this.cycleStartNanoTime = this.startNanoTime;
    }

    private void tick(T t) {
      if (isOver) return;

      long currentNanoTime = System.nanoTime();

      if (!triggered) {

        if (currentNanoTime - startNanoTime >= delay) {
          triggered = true;
          cycleStartNanoTime = currentNanoTime; // 开始第一个周期
          executedCycles = 1;
          hasExecutedInCurrentCycle = false;
        } else {
          return;
        }
      }


      if (executedCycles > totalTimes) {
        isOver = true;
        return;
      }


      long elapsedInCycle = currentNanoTime - cycleStartNanoTime;

      if (durationEachTime == 0) {
        if (!hasExecutedInCurrentCycle) {
          run.accept(t);
          hasExecutedInCurrentCycle = true;

          // 检查间隔
          if (interval == 0) {
            executedCycles++;
            if (executedCycles <= totalTimes) {
              cycleStartNanoTime = currentNanoTime;
              hasExecutedInCurrentCycle = false;
            } else {
              isOver = true;
            }
          } else if (elapsedInCycle >= interval) {
            executedCycles++;
            if (executedCycles <= totalTimes) {
              cycleStartNanoTime = currentNanoTime;
              hasExecutedInCurrentCycle = false;
            } else {
              isOver = true;
            }
          }
        }
        return;
      }

      if (elapsedInCycle < durationEachTime) {
        run.accept(t);
      } else {
        long cycleTotalTime = durationEachTime + interval;

        if (elapsedInCycle >= cycleTotalTime) {
          executedCycles++;

          if (executedCycles <= totalTimes) {
            cycleStartNanoTime = currentNanoTime;
          } else {
            isOver = true;
          }
        }
      }
    }
  }
}
