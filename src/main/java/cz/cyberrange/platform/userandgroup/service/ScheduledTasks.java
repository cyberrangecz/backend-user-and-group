package cz.cyberrange.platform.userandgroup.service;

import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Here is the scheduler for deleting expired groups. This scheduler is started once a day at midnight and deletes the groups which expiration date is in the past.
 */
@Component
@Transactional
public class ScheduledTasks {

    private final IDMGroupRepository groupRepository;

    @Autowired
    public ScheduledTasks(IDMGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    /**
     * Valid formats for cron expressions - https://stackoverflow.com/a/45126855
     * "0 0 0 * * *" - means every day at midnight
     **/
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void removeExpiredGroups() {
        groupRepository.deleteExpiredIDMGroups();
    }


    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    @CacheEvict(value = "users", allEntries = true)
    public void clearUsersCache() {
        // clear cache
    }

}
