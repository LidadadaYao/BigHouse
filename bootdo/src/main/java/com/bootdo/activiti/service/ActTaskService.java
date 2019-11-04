package com.bootdo.activiti.service;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.io.InputStream;
import java.util.Map;

/**
 */
public interface ActTaskService {

    void complete(String taskId, String procInsId, String comment, String title, Map<String, Object> vars);

    /**
     * 任务节点的完成
     * @param taskId
     * @param vars
     */
    void complete(String taskId,Map<String,Object> vars);

    /**
     * 流程开启
     * @param procDefKey
     * @param businessTable
     * @param businessId
     * @param title
     * @param vars
     * @return
     */
    ProcessInstance startProcess(String procDefKey, String businessTable, String businessId, String title, Map<String, Object> vars);

    /**
     * 获取表单
     * @param procDefId
     * @param taskDefKey
     * @return
     */
    String getFormKey(String procDefId, String taskDefKey);

    /**
     * 流程图查看
     * @param processDefinitionId
     * @param executionId
     * @return
     */
    InputStream tracePhoto(String processDefinitionId, String executionId);

    /**
     * 流程单个节点跳转
     * @param taskKey
     * @param taskId
     */
    void jump2TargetFlowNode(String taskKey,String taskId);

    /**
     * 根据taskId 获取流程定义对象
     * @param taskId
     * @return
     */
    public ProcessDefinitionEntity getProcessByTaskId(String taskId);

    /**
     * 根据id 获取task 对象
     * @param taskId
     * @return
     */
    public Task getSingleTask(String taskId);

    /**
     * 流程挂起
     * @param processInstanceId
     * @return
     */
    Boolean suspendProcessInstance(String processInstanceId);

    /**
     * 流程激活
     * @param processInstanceId
     * @return
     */
    Boolean activeProcessInstance(String processInstanceId);

    /**
     * 流程中 单任务转交
     * @param taskId
     * @param userCode
     */
    void transferAssignee(String taskId,String userCode);
}
