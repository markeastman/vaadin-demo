package uk.me.eastmans.backgroundjobs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobManager {
    final private List<JobWrapper> jobs = new ArrayList<>();

    final private SAPProcess sapService;

    public JobManager(SAPProcess sapService) {
        this.sapService = sapService;
    }

    public JobWrapper createJobWrapper(String description) {
        JobWrapper jobWrapper = new JobWrapper(sapService,description);
        jobs.add(jobWrapper);
        return jobWrapper;
    }

    public List<JobWrapper> getJobs() {
        return jobs;
    }

    public void startBackgroundJob(UI ui, Grid<JobWrapper> attachedGrid) {
        JobWrapper watcher = createJobWrapper("User initiated job");
        watcher.startBackgroundJob(ui, attachedGrid);
    }
}