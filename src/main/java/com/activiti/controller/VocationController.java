package com.activiti.controller;

import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@RestController
@RequestMapping("/vocation")
@Slf4j
public class VocationController {

    @Autowired
    private ProcessEngine engineEngine;

    private static final String instanceKey = "myProcess_1";


    @GetMapping("/start")
    public String startActivity(@RequestParam String assignee, @RequestParam String day) {
        //启动流程实例，字符串"vacation"是BPMN模型文件里process元素的id
        ProcessInstance processInstance = engineEngine.getRuntimeService().startProcessInstanceByKey("vocation", new HashMap<>());
        //流程实例启动后，流程会跳转到请假申请节点
        Task task = engineEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        //设置请假申请任务的执行人
        engineEngine.getTaskService().setAssignee(task.getId(), assignee);
        //设置流程参数：请假天数和表单ID
        //流程引擎会根据请假天数days>3判断流程走向
        //formId是用来将流程数据和表单数据关联起来
        Map<String, Object> args = new HashMap<>();
        args.put("day", day);
        args.put("reason", "请假");
        engineEngine.getTaskService().complete(task.getId(), args);
        return task.getId();
    }

    @GetMapping("/vocation")
    public List<String> findTasksById(@RequestParam String userGroup) {
        List<Task> taskList = engineEngine.getTaskService().createTaskQuery().processDefinitionKey("vocation").taskCandidateOrAssigned(userGroup).list();
        List<String> taskInfos = new ArrayList<>();
        for (Task task : taskList) {
            Map<String, Object> variables = task.getProcessVariables();
            String argsInfo = StringUtils.EMPTY;
            for (Map.Entry<String, Object> arg : variables.entrySet()) {
                argsInfo = arg.getKey() + "[" + arg.getValue() + "]";
            }
            taskInfos.add(task.getId() + ", " + task.getAssignee() + ", " + argsInfo);
        }
        return taskInfos;
    }

    @GetMapping("/approve")
    public void approve(@RequestParam String taskId, @RequestParam String assignee) {
        Map<String,Object> vars = new HashMap<>();
        vars.put("sign", "true");
        //设置审批任务的执行人
        engineEngine.getTaskService().claim(taskId, assignee);
        engineEngine.getTaskService().complete(taskId, vars);
    }

    @GetMapping("/reject")
    public String reject(@RequestParam String taskId) {
        Task task = engineEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
        engineEngine.getRuntimeService().deleteProcessInstance(task.getProcessInstanceId(), task.getId());
        return task.getProcessInstanceId() + "流程结束";
    }

    @GetMapping(value = "/image")
    @ResponseBody
    public void image(HttpServletResponse response, @RequestParam String taskId) {
        try {
            Task task = engineEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
            InputStream is = getDiagram(task.getProcessInstanceId());
            if (is == null)
                return;

            response.setContentType("image/png");

            BufferedImage image = ImageIO.read(is);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "png", out);

            is.close();
            out.close();
        } catch (Exception ex) {
            log.error("查看流程图失败", ex);
        }
    }

    public InputStream getDiagram(String processInstanceId) {
        //获得流程实例
        ProcessInstance processInstance = engineEngine.getRuntimeService().createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = StringUtils.EMPTY;
        if (processInstance == null) {
            //查询已经结束的流程实例
            HistoricProcessInstance processInstanceHistory =
                    engineEngine.getHistoryService().createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId).singleResult();
            if (processInstanceHistory == null)
                return null;
            else
                processDefinitionId = processInstanceHistory.getProcessDefinitionId();
        } else {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }

        //使用宋体
        String fontName = "宋体";
        //获取BPMN模型对象
        BpmnModel model = engineEngine.getRepositoryService().getBpmnModel(processDefinitionId);
        //获取流程实例当前的节点，需要高亮显示
        List<String> currentActs = Collections.EMPTY_LIST;
        if (processInstance != null)
            currentActs = engineEngine.getRuntimeService().getActiveActivityIds(processInstance.getId());

        return new DefaultProcessDiagramGenerator().generateDiagram(model, "png", currentActs, new ArrayList<String>(),
                        fontName, fontName, null, 1.0d);
    }
}
