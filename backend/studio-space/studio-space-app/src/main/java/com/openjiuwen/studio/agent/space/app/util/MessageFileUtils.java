package com.openjiuwen.studio.agent.space.app.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openjiuwen.studio.agent.space.app.enums.AgentBuilderStorageType;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.CollectionUtil;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderFileMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderFileEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderTaskEntity;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class MessageFileUtils {
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private AgentBuilderFileMapper fileMapper;

    @Resource
    private FileTransferUtils fileTransferUtil;

    public List<String> tempFileBindTask(AgentBuilderTaskEntity task, List<String> fileList) {
        List<AgentBuilderFileEntity> fileEntities = fileMapper.selectList(
            new LambdaQueryWrapper<AgentBuilderFileEntity>().in(AgentBuilderFileEntity::getId, fileList)
                .eq(AgentBuilderFileEntity::getDomainId, AuthorizationContextHolder.domainId())
                .eq(AgentBuilderFileEntity::getCreatedByUserId, AuthorizationContextHolder.userId())
                .eq(AgentBuilderFileEntity::getDeleted, false));
        if (ObjectUtils.isEmpty(fileEntities)) {
            log.error("[chat] validate files fail, files not exist, fileIds:{}", fileList);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR, "files is not exist");
        } else if (fileEntities.size() < fileList.size()) {
            List<String> existFileIdList = fileEntities.stream().map(AgentBuilderFileEntity::getId).toList();
            List<String> notExistFileIdList = fileList.stream().filter(id -> !existFileIdList.contains(id)).toList();
            log.error("[chat] validate files fail, some files not exist, fileIds:{}", notExistFileIdList);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR,
                "some files is not exist");
        } else {
            // 校验是否有其他任务上传的文件
            List<AgentBuilderFileEntity> otherTaskFiles = fileEntities.stream()
                .filter(file -> StringUtils.isNotEmpty(file.getTaskId()) && !file.getTaskId().equals(task.getId()))
                .toList();
            if (ObjectUtils.isNotEmpty(otherTaskFiles)) {
                List<String> otherTaskFileIdList = otherTaskFiles.stream().map(AgentBuilderFileEntity::getId).toList();
                log.error("[chat] validate files fail, some files is used by other task, fileIds:{}",
                    otherTaskFileIdList);
                throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR,
                    "some files is used by other task");
            }
            log.info("[tempFileBindTask] validate files success.");
            // 处理消息引用的文件
            return handleMessageFile(fileEntities, task);
        }
    }

    private List<String> handleMessageFile(List<AgentBuilderFileEntity> fileEntities, AgentBuilderTaskEntity task) {
        // 过滤出已经引用过的文件，生成新的记录
        List<AgentBuilderFileEntity> usedTaskFiles = fileEntities.stream()
            .filter(file -> StringUtils.isNotEmpty(file.getTaskId()) && file.getTaskId().equals(task.getId()))
            .toList();
        List<AgentBuilderFileEntity> newFileRecords = new ArrayList<>();
        usedTaskFiles.forEach(file -> {
            AgentBuilderFileEntity newFileRecord = new AgentBuilderFileEntity();
            BeanUtils.copyProperties(file, newFileRecord);
            newFileRecord.setId(AppCommonUtils.getUUID());
            newFileRecord.setType(3);
            newFileRecord.setSourceId(file.getId());
            newFileRecord.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            newFileRecord.setCreatedByUserId(AuthorizationContextHolder.userId());
            newFileRecord.setDomainId(AuthorizationContextHolder.domainId());
            newFileRecord.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
            newFileRecord.setLastUpdatedByUserId(AuthorizationContextHolder.userId());
            AppCommonUtils.setCommonCreateProperties(newFileRecord);
            newFileRecords.add(newFileRecord);
        });

        // 第一次引用的文件
        List<AgentBuilderFileEntity> unusedTaskFiles = fileEntities.stream()
            .filter(file -> StringUtils.isEmpty(file.getTaskId()))
            .toList();
        unusedTaskFiles.forEach(file -> {
            if (file.getStorageType() == AgentBuilderStorageType.OBS.getType()) {
                FileTransferUtils.FileTransferResult bindResult = fileTransferUtil.userFileBinding2Task(file.getUri(),
                    task.getId());
                if (bindResult.isSuccess()) {
                    file.setUri(bindResult.getFileKey());
                }
            }
            file.setTaskId(task.getId());
            AppCommonUtils.setCommonUpdateProperties(file);
        });

        // 批量操作数据库，为了在后面异步逻辑中能够查询到新数据，需要将这个操作放在transaction之外
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            AgentBuilderFileMapper mapper = sqlSession.getMapper(AgentBuilderFileMapper.class);
            if (CollectionUtil.isNotEmpty(unusedTaskFiles)) {
                for (AgentBuilderFileEntity file : unusedTaskFiles) {
                    mapper.updateById(file);
                }
            }
            if (CollectionUtil.isNotEmpty(newFileRecords)) {
                mapper.insertBatchSomeColumn(newFileRecords);
            }
            sqlSession.commit();
        }

        return Stream.concat(unusedTaskFiles.stream().map(AgentBuilderFileEntity::getId),
            newFileRecords.stream().map(AgentBuilderFileEntity::getId)).toList();
    }
}
