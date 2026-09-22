package cz.cyberrange.platform.userandgroup.service;

import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Runs the periodic maintenance job that removes expired groups. */
@Component
@Transactional
public class ScheduledTasks {

  private final IDMGroupRepository groupRepository;

  @Autowired
  public ScheduledTasks(IDMGroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  /**
   * Deletes every group whose expiration date is at or before the current time. Runs once a day, at
   * midnight UTC.
   */
  @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
  public void removeExpiredGroups() {
    groupRepository.deleteExpiredIDMGroups();
  }
}
