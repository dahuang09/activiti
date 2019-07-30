package com.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LeaveProcessTests {
    @Resource
    private ProcessEngine engineEngine ;//注入流程引擎

    @Test
    public void testDeploy(){

        // 部署流程文件 act_re_deployment
        DeploymentBuilder builder = engineEngine.getRepositoryService().createDeployment();
        Deployment deploy = builder.addClasspathResource("processes/leave.bpmn").deploy();
        System.out.println("部署完成\n"+deploy.getId());
        System.out.println("----------------");
        // 启动流程
    }

    /*启动实例*/
    @Test
    public void testStartProcess(){
        //去数据库表名为act_re_procdef 取出主键 启动流程实例
        String definitionId = "myProcess_1:2:2503" ;
        ProcessInstance processInstance = engineEngine.getRuntimeService().startProcessInstanceById(definitionId);
        System.out.println(processInstance.getId());
        //去act_run_task表中，根据myProcess_1:2:2503 查看任务运行状态
    }

    @Test
    public void  testGetTask(){
        TaskQuery query = engineEngine.getTaskService().createTaskQuery();
        String assignee = "" ;
        query.taskAssignee(assignee) ;
        query.list().forEach(s-> System.out.println(s.getId()+"-----------\n"+s.getName()));
    }

    /*办理任务*/
    @Test
    public void  testApprove(){
        String taskId = "7503" ;
        engineEngine.getTaskService().complete(taskId);
    }

    @Test
    public void testStartByBiz() {
        ProcessInstance processInstance = engineEngine.getRuntimeService().startProcessInstanceById("myProcess_1", "leave001");
        System.out.println("流程启动成功，流程id:" + processInstance.getId());
    }
}
