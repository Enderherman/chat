package top.enderherman.easychat.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.common.ResponseCodeEnum;
import top.enderherman.easychat.component.RedisComponent;
import top.enderherman.easychat.config.AppConfig;
import top.enderherman.easychat.constants.Constants;
import top.enderherman.easychat.entity.dto.MessageSendDTO;
import top.enderherman.easychat.entity.dto.SysSettingDto;
import top.enderherman.easychat.entity.dto.TokenUserInfoDto;
import top.enderherman.easychat.entity.enums.*;
import top.enderherman.easychat.entity.po.ChatSession;
import top.enderherman.easychat.entity.po.UserContact;
import top.enderherman.easychat.entity.query.ChatMessageQuery;
import top.enderherman.easychat.entity.po.ChatMessage;
import top.enderherman.easychat.entity.query.ChatSessionQuery;
import top.enderherman.easychat.entity.query.UserContactQuery;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.entity.query.SimplePage;
import top.enderherman.easychat.exception.BusinessException;
import top.enderherman.easychat.mappers.ChatMessageMapper;
import top.enderherman.easychat.mappers.ChatSessionMapper;
import top.enderherman.easychat.mappers.UserContactMapper;
import top.enderherman.easychat.service.ChatMessageService;
import top.enderherman.easychat.utils.CopyUtils;
import top.enderherman.easychat.utils.DateUtils;
import top.enderherman.easychat.utils.StringUtils;
import top.enderherman.easychat.webSocket.MessageHandler;


/**
 * 聊天消息表 业务接口实现
 */
@Slf4j
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private AppConfig appConfig;


    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatMessage> findListByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ChatMessage> list = this.findListByParam(param);
        PaginationResultVO<ChatMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatMessage bean) {
        return this.chatMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(ChatMessage bean, ChatMessageQuery param) {
        StringUtils.checkParam(param);
        return this.chatMessageMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(ChatMessageQuery param) {
        StringUtils.checkParam(param);
        return this.chatMessageMapper.deleteByParam(param);
    }

    /**
     * 根据MessageId获取对象
     */
    @Override
    public ChatMessage getChatMessageByMessageId(Integer messageId) {
        return this.chatMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId修改
     */
    @Override
    public Integer updateChatMessageByMessageId(ChatMessage bean, Integer messageId) {
        return this.chatMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    @Override
    public Integer deleteChatMessageByMessageId(Integer messageId) {
        return this.chatMessageMapper.deleteByMessageId(messageId);
    }

    /**
     * 发送消息
     */
    @Override
    public MessageSendDTO<?> saveMessage(ChatMessage chatMessage, TokenUserInfoDto userInfoDto) {
        //校验好友关系
        if (!Constants.ROBOT_UID.equals(userInfoDto.getUserId())) {
            List<String> userContactList = redisComponent.getUserContactList(userInfoDto.getUserId());
            if (!userContactList.contains(chatMessage.getContactId())) {
                UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (contactTypeEnum == UserContactTypeEnum.USER) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        //1.会话信息
        String sessionId = null;
        String sendUserId = userInfoDto.getUserId();
        String contactId = chatMessage.getContactId();
        Long curTime = System.currentTimeMillis();
        //处理内容
        String messageContent = StringUtils.cleanHtmlTag(chatMessage.getMessageContent());
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (UserContactTypeEnum.USER == contactTypeEnum) {
            sessionId = StringUtils.getChatSessionId4User(new String[]{sendUserId, contactId});
        } else {
            sessionId = StringUtils.getChatSessionId4Group(contactId);
        }
        //发送状态
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENT.getStatus();


        //更新会话
        ChatSession chatSession = new ChatSession();
        if (UserContactTypeEnum.USER != contactTypeEnum) {
            chatSession.setLastMessage(userInfoDto.getNickName() + ": " + messageContent);
        } else {
            chatSession.setLastMessage(messageContent);
        }
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(curTime);
        chatSessionMapper.updateBySessionId(chatSession, sessionId);

        //记录消息消息表
        chatMessage.setSendTime(curTime);
        chatMessage.setSessionId(sessionId);
        chatMessage.setStatus(status);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(userInfoDto.getNickName());
        chatMessage.setContactType(contactTypeEnum.getType());
        chatMessageMapper.insert(chatMessage);

        //发送ws消息
        MessageSendDTO<?> messageSendDTO = CopyUtils.copy(chatMessage, MessageSendDTO.class);
        if (Constants.ROBOT_UID.equals(contactId)) {
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            TokenUserInfoDto robot = new TokenUserInfoDto();
            robot.setUserId(sysSettingDto.getRobotUid());
            robot.setNickName(sysSettingDto.getRobotNickName());
            ChatMessage robotMessage = new ChatMessage();
            robotMessage.setContactId(sendUserId);
            //TODO 接入大模型
            robotMessage.setMessageContent("催催魏大仓让他赶紧更新jdk17再引入springAi");
            robotMessage.setMessageType(MessageTypeEnum.CHAT.getType());
            saveMessage(robotMessage, robot);
        } else {
            messageHandler.sendMessage(messageSendDTO);
        }


        return messageSendDTO;
    }

    /**
     * 上传文件
     */
    @Override
    public void saveMessageFile(String userId, Integer messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        String fileSuffix = StringUtils.getFileSuffix(file.getOriginalFilename());
        //校验文件夹大小
        if (!StringUtils.isEmpty(fileSuffix) &&
                ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase()) &&
                file.getSize() > Constants.FILE_SIZE_MB * sysSettingDto.getMaxImageSize()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        } else if (!StringUtils.isEmpty(fileSuffix) &&
                ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase()) &&
                file.getSize() > Constants.FILE_SIZE_MB * sysSettingDto.getMaxVideoSize()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        } else if (!StringUtils.isEmpty(fileSuffix) &&
                !ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase()) &&
                !ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase()) &&
                file.getSize() > Constants.FILE_SIZE_MB * sysSettingDto.getMaxFileSize()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }


        String fileName = file.getOriginalFilename();
        String fileExtName = StringUtils.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYY_MM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File uploadFile = new File(folder.getPath() + "/" + fileRealName);
        try {
            file.transferTo(uploadFile);
            if (cover != null) {
                cover.transferTo(new File(uploadFile.getPath() + Constants.COVER_IMAGE_SUFFIX));
            }
        } catch (Exception e) {
            log.error("上传文件失败，", e);
            throw new BusinessException("文件上传失败");
        }
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        chatMessageMapper.updateByMessageId(chatMessage, messageId);

        MessageSendDTO<?> messageSendDTO = new MessageSendDTO<>();
        messageSendDTO.setStatus(MessageStatusEnum.SENT.getStatus());
        messageSendDTO.setMessageId(messageId);
        messageSendDTO.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDTO.setContactId(chatMessage.getContactId());
        messageHandler.sendMessage(messageSendDTO);
    }

    /**
     * 文件下载
     */
    @Override
    public File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long messageId, Boolean showCover) {
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(messageId.intValue());
        String contactId = chatMessage.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        List<String> userContactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
        if (UserContactTypeEnum.USER.equals(contactTypeEnum) && (
                !userContactList.contains(contactId) &&
                        !tokenUserInfoDto.getUserId().equals(contactId)
        )) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (UserContactTypeEnum.GROUP.equals(contactTypeEnum)) {
            UserContactQuery query = new UserContactQuery();
            query.setUserId(tokenUserInfoDto.getUserId());
            query.setContactId(chatMessage.getContactId());
            query.setContactType(UserContactTypeEnum.GROUP.getType());
            query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer contactCount = userContactMapper.selectCount(query);
            if (contactCount == 0) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYY_MM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = chatMessage.getFileName();
        String fileExtName = StringUtils.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;

        if (showCover != null && showCover) {
            fileRealName = fileRealName + Constants.COVER_IMAGE_SUFFIX;
        }
        File file = new File(folder.getPath() + "/" + fileRealName);
        if (!file.exists()) {
            log.info("文件不存在");
            throw new BusinessException(new BusinessException(ResponseCodeEnum.CODE_600));
        }
        return file;
    }
}