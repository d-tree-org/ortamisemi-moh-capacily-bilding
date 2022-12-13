package org.smartregister.chw.util;

import android.view.View;
import android.widget.RelativeLayout;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.R;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;

import java.util.Set;

/**
 * @author issyzac 6/8/22
 */
public class TaskUtils {

    public static Set<Task> getClientReferralTasks(String baseEntityId){
        Set<Task> taskList;
        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(CoreConstants.REFERRAL_PLAN_ID, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);
        return taskList;   
    }

    public static boolean hasReferralDue(Set<Task> taskList) {
        boolean isReferralFollowDue = false;
        for (Task task : taskList) {
            int days = Math.abs(Days.daysBetween(task.getAuthoredOn(), DateTime.now()).getDays());
            if ((days >= 1 && task.getPriority() == Task.TaskPriority.ROUTINE) || days >= 3) {
                isReferralFollowDue = true;
                break;
            }
        }
        return isReferralFollowDue;
    }
    
}
