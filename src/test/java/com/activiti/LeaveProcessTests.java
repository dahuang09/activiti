package com.activiti;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class LeaveProcessTests {
    @Resource
    private ProcessEngine engineEngine ;//注入流程引擎

    private static final String instanceKey = "myProcess";

    @Test
    public void testDeploy(){

        // 部署流程文件 act_re_deployment
        DeploymentBuilder builder = engineEngine.getRepositoryService().createDeployment();
        Deployment deploy = builder.addClasspathResource("processes/vocation7.bpmn").deploy();
        System.out.println("部署完成\n"+deploy.getId());
        System.out.println("----------------");
        // 启动流程
    }

    /*启动实例*/
    @Test
    public void testStartProcess(){
        //启动流程实例，字符串"vacation"是BPMN模型文件里process元素的id
        Map<String, Object> map = new HashMap<>();
        map.put("assignee", "damon");
        ProcessInstance processInstance = engineEngine.getRuntimeService().startProcessInstanceByKey(instanceKey, map);
        //流程实例启动后，流程会跳转到请假申请节点
        Task task = engineEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        task.setAssignee("damon");
        //设置流程参数：请假天数和表单ID
        //流程引擎会根据请假天数days>3判断流程走向
        //formId是用来将流程数据和表单数据关联起来
        Map<String, Object> args = new HashMap<>();
        args.put("day", 2);
        args.put("reason", "请假");
        args.put("assignee", "jacob");
        engineEngine.getTaskService().complete(task.getId(), args);
    }

    @Test
    public void  testGetTask(){
        List<Task> taskList = engineEngine.getTaskService().createTaskQuery()
                .taskCandidateGroup("Manager").list();
        List<String> taskInfos = new ArrayList<>();
        for (Task task : taskList) {
            Map<String, Object> variables = task.getTaskLocalVariables();
            String argsInfo = StringUtils.EMPTY;
            for (Map.Entry<String, Object> arg : variables.entrySet()) {
                argsInfo = arg.getKey() + "[" + arg.getValue() + "]";
            }
            taskInfos.add(task.getId() + ", " + task.getAssignee() + ", " + argsInfo);
        }
        taskInfos.forEach(s-> System.out.println(s));
    }

    /*办理任务*/
    @Test
    public void  testApprove(){
        String taskId = "185002" ;
        engineEngine.getTaskService().complete(taskId);
    }
}
