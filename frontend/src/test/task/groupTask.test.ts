const groupTasks = require('./groupTask')
let todayTask = {
}

let isPinTask = {

}

let weekTask = {

}
describe('groupTasks function test', () => {
    it('groupTasks today', () => {
        expect(groupTasks(todayTask)).toEqual([
            {
                "name": "置顶",
                "tasks": []
            },
            {
                "name": "今天",
                "tasks": [
                ]
            },
            {
                "name": "昨天",
                "tasks": []
            },
            {
                "name": "过去一周",
                "tasks": []
            },
            {
                "name": "过去一个月",
                "tasks": []
            },
            {
                "name": "过去三个月",
                "tasks": []
            },
            {
                "name": "过去一年",
                "tasks": []
            }
        ]);
    });
    it('groupTasks isPin', () => {
        expect(groupTasks(isPinTask)).toEqual([]);
    });
    it('groupTasks weekTask', () => {
        expect(groupTasks(weekTask)).toEqual([]);
    });


})

