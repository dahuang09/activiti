package com.activiti.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ActivitiServiceImpl {
//    @Autowired
//    private RuntimeService runtimeService;
//    @Autowired
//    private TaskService taskService;

    public void startActivity() {
        String instanceKey = "leave";
        log.info("开启请假流程...");
        Map<String, Object> map = new HashMap<>();
//        map.put("");
    }
}
