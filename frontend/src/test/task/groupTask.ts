import {taskGroupModel} from "../utils/util";
import {handleTime} from "@routes/agent-center/utils";

function groupTasks (task)  {
    let newTaskGroup = JSON.parse(JSON.stringify(taskGroupModel));
    if (task.display_info.is_pin) {
        newTaskGroup[0].tasks.push(task);
    } else {
        let res = handleTime(task.create_time);
        if (res === 'today') {
            newTaskGroup[1].tasks.push(task);
        } else if (res === '1day') {
            newTaskGroup[2].tasks.push(task);
        } else if (res === '1week') {
            newTaskGroup[3].tasks.push(task);
        } else if (res === '1month') {
            newTaskGroup[4].tasks.push(task);
        } else if (res === '3month') {
            newTaskGroup[5].tasks.push(task);
        } else if (res === '1year') {
            newTaskGroup[6].tasks.push(task);
        }
    }
    return newTaskGroup
};


module.exports = groupTasks
