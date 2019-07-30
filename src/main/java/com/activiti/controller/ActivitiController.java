package com.activiti.controller;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/activiti")
@Slf4j
public class ActivitiController {

//    @Autowired
//    private RuntimeService runtimeService;
//    @Autowired
//    private TaskService taskService;
//    @Autowired
//    private HistoryService historyService;

    @Autowired
    private ProcessEngine engineEngine;

    private static final String instanceKey = "myProcess_1";

    @GetMapping("/activities")
    public List<Map<String, String>> listTasks() {
        List<Task> taskList = engineEngine.getTaskService().createTaskQuery().list();
        List<Map<String, String>> resultList = new ArrayList<>();
        for(Task task : taskList) {
            Map<String, String> map = new HashMap<>();
            map.put("taskId", task.getId());
            map.put("name", task.getName());
            map.put("createTime", task.getCreateTime().toString());
            map.put("assignee", task.getAssignee());
            map.put("instanceId", task.getProcessInstanceId());
            map.put("executionId", task.getExecutionId());
            map.put("definitionId", task.getProcessDefinitionId());
            resultList.add(map);
        }
        return resultList;
    }

    @GetMapping("/start")
    public String startActivity(@RequestParam String taskId) {
        log.info("开启请假流程...");
        Map<String, Object> map = new HashMap<>();
        map.put("jobNumber","A1001");
        map.put("busData","bus data");
        ProcessInstance processInstance = engineEngine.getRuntimeService().startProcessInstanceByKey(instanceKey, map);
        log.info("启动流程实例成功:{}", processInstance);
        log.info("流程实例ID:{}", processInstance.getId());
        log.info("流程定义ID:{}", processInstance.getProcessDefinitionId());
        return processInstance.getId();
    }

    @GetMapping("/exist")
    public Map<String, Object> findTasksById(@RequestParam String taskId) {
//        Task task = engineEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
        Map<String, Object> paramMap = engineEngine.getTaskService().getVariables(taskId);
        log.info("task param:{}", paramMap);
        return paramMap;
    }

    @GetMapping("/approve")
    public void completeTask(@RequestParam String taskId) {
        Map<String,Object> vars = new HashMap<>();
        vars.put("sign", "true");
        engineEngine.getTaskService().complete(taskId, vars);
    }

    @GetMapping("/finished")
    public HistoricProcessInstanceQuery isFinished(@RequestParam String taskId) {
        return engineEngine.getHistoryService().createHistoricProcessInstanceQuery().finished().processInstanceId(taskId);
    }
}
